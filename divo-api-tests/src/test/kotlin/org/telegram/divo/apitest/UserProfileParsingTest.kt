package org.telegram.divo.apitest

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.telegram.divo.dal.dto.user.UserInfoResponse
import org.telegram.divo.dal.dto.user.toEntity
import org.telegram.divo.entity.RoleType
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 1: `GET /user/{id}` and `/user/info` bodies -> UserInfoResponse -> UserInfo, the exact path
 * the app takes. Gson fills Kotlin non-null fields with null when the JSON has null, so a field the
 * backend may omit must be nullable in the DTO or the mapping crashes (the blocked-user bug).
 */
class UserProfileParsingTest {

    private fun parse(json: String) = appGson.fromJson(json, UserInfoResponse::class.java).toEntity()

    @Test
    fun `full model profile maps all key fields`() {
        val user = parse(fixture("user_model.json"))
        assertEquals(32003, user.id)
        assertEquals("Test Model", user.fullName)
        assertEquals(RoleType.MODEL, user.role)
        assertEquals("https://cdn.example.com/files/photo_1280.png", user.photoUrl)
        assertEquals("https://cdn.example.com/files/avatar_288.png", user.avatarUrl)
        assertEquals("avatar-uuid", user.avatarUuid)
        assertEquals(61947L, user.avatarId, "avatar photo_id drives face search")
        assertEquals(64, user.statistic.viewsCount)
        assertEquals(1, user.statistic.followersCount)
        assertTrue(user.isFollowed)
        assertEquals(1000000001L, user.telegramId)
    }

    @Test
    fun `blocked user profile with null statistic and social networks does not crash`() {
        val user = parse(fixture("user_blocked.json"))
        assertEquals(4242, user.id)
        assertEquals(0, user.statistic.followersCount)
        assertTrue(user.userSocialNetworks.isEmpty())
        assertEquals("", user.avatarUrl)
    }

    /** Every top-level field the backend sends, set to null one at a time, must still map. */
    @TestFactory
    fun `each field set to null still maps`(): List<DynamicTest> {
        val base = fixtureJson("user_model.json")
        return base.getAsJsonObject("data").keySet().map { field ->
            DynamicTest.dynamicTest("data.$field = null") {
                parse(base.withDataField(field, NULL).toString())
            }
        }
    }

    /** Same, with the field missing from the response altogether. */
    @TestFactory
    fun `each field missing still maps`(): List<DynamicTest> {
        val base = fixtureJson("user_model.json")
        return base.getAsJsonObject("data").keySet().filter { it != "id" }.map { field ->
            DynamicTest.dynamicTest("data.$field missing") {
                parse(base.withDataField(field, null).toString())
            }
        }
    }

    @Test
    fun `unknown role maps to UNKNOWN instead of failing`() {
        val json = fixtureJson("user_model.json").withDataField("role", JsonPrimitive("photographer_v2"))
        assertEquals(RoleType.UNKNOWN, parse(json.toString()).role)
    }

    @Test
    fun `AI creator is recognised from additionalInfo subRole`() {
        fun withSubRole(subRole: String?) = fixtureJson("user_model.json")
            .withDataField("role", JsonPrimitive("new_face"))
            .withDataField("additionalInfo", JsonObject().apply { subRole?.let { addProperty("subRole", it) } })
            .toString()
        assertTrue(parse(withSubRole("dancer")).isAiCreator)
        assertTrue(parse(withSubRole("DANCER")).isAiCreator)
        assertFalse(parse(withSubRole("new_talent")).isAiCreator)
        assertFalse(parse(withSubRole(null)).isAiCreator)
        assertFalse(parse(fixtureJson("user_model.json").withDataField("additionalInfo", null).toString()).isAiCreator)
        // The backend now stores the sub-role in `subrole`
        val withSubrole = fixtureJson("user_model.json")
            .withDataField("role", JsonPrimitive("new_face"))
            .withDataField("subrole", JsonPrimitive("ai_creator"))
            .withDataField("additionalInfo", null)
        assertTrue(parse(withSubrole.toString()).isAiCreator)
    }

    @Test
    fun `unicode, emoji and very long names survive`() {
        val name = "Анна-Мария 🦋 O'Connor «Студия» " + "x".repeat(5_000)
        val json = fixtureJson("user_model.json").withDataField("fullName", JsonPrimitive(name))
        assertEquals(name, parse(json.toString()).fullName)
    }

    @Test
    fun `empty nested objects map to defaults`() {
        val json = fixtureJson("user_model.json")
            .withDataField("statistic", JsonObject())
            .withDataField("photo", JsonObject())
            .withDataField("userSocialNetworks", JsonArray())
        val user = parse(json.toString())
        assertEquals(0, user.statistic.likesCount)
        assertEquals("", user.photoUrl)
    }

    @Test
    fun `numeric fields sent as strings are accepted`() {
        // Some PHP serializers emit numbers as strings; Gson coerces "64" to Int
        val stat = fixtureJson("user_model.json").getAsJsonObject("data").getAsJsonObject("statistic")
        stat.addProperty("viewsCount", "64")
        val json = fixtureJson("user_model.json").withDataField("statistic", stat)
        assertEquals(64, parse(json.toString()).statistic.viewsCount)
    }
}

package org.telegram.divo.apitest

import com.google.gson.JsonParser
import org.junit.jupiter.api.Test
import org.telegram.divo.dal.dto.user.toBlockedUsers
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Level 1: `POST /user/blocked` only declares a JSend `data` object, so every plausible shape must parse. */
class BlockedUsersParsingTest {

    private fun parse(json: String, offset: Int = 0, limit: Int = 20) =
        JsonParser.parseString(json).asJsonObject.toBlockedUsers(offset, limit)

    @Test
    fun `flat items with pagination meta`() {
        val page = parse(
            """{"status":"success","data":{"items":[
                 {"id":1,"fullName":"A","role":"model","roleLabel":"Модель","avatar":{"fullUrl":"https://x/a.png"}},
                 {"id":2,"fullName":"B","role":"agency","photo":{"fullUrl":"https://x/b.png"}}],
               "pagination":{"meta":{"totalCount":5}}}}"""
        )
        assertEquals(listOf(1, 2), page.items.map { it.id })
        assertEquals("https://x/a.png", page.items[0].avatarUrl)
        assertEquals("https://x/b.png", page.items[1].avatarUrl, "falls back to photo")
        assertEquals(5, page.totalCount)
    }

    @Test
    fun `items nested under user or blockedUser`() {
        val page = parse(
            """{"data":{"items":[
                 {"id":100,"user":{"id":7,"fullName":"Nested"}},
                 {"blockedUser":{"id":8,"full_name":"Snake"}}]}}"""
        )
        assertEquals(listOf(7, 8), page.items.map { it.id }, "the user's id, not the block record's")
        assertEquals("Snake", page.items[1].fullName)
    }

    @Test
    fun `data as a bare array and avatar as a plain string`() {
        val page = parse("""{"data":[{"userId":3,"name":"C","avatar":"https://x/c.png"}]}""")
        assertEquals(3, page.items.single().id)
        assertEquals("https://x/c.png", page.items.single().avatarUrl)
    }

    @Test
    fun `items without any id are skipped, nulls are tolerated`() {
        val page = parse("""{"data":{"items":[{"fullName":"no id"},null,{"id":null},{"id":9,"fullName":null,"avatar":null}]}}""")
        assertEquals(listOf(9), page.items.map { it.id })
        assertEquals("", page.items.single().fullName)
    }

    @Test
    fun `empty, null or missing data is an empty page`() {
        for (json in listOf("""{"data":{"items":[]}}""", """{"data":null}""", """{}""", """{"data":{}}""")) {
            val page = parse(json)
            assertTrue(page.items.isEmpty(), json)
            assertEquals(0, page.totalCount, json)
        }
    }

    @Test
    fun `without a total a full page means there may be more, a short one means the end`() {
        val full = parse("""{"data":{"items":[${(1..20).joinToString { """{"id":$it}""" }}]}}""", offset = 0, limit = 20)
        assertTrue(full.totalCount > 20, "full page keeps paging")
        val short = parse("""{"data":{"items":[{"id":21},{"id":22}]}}""", offset = 20, limit = 20)
        assertEquals(22, short.totalCount, "short page ends paging")
    }
}

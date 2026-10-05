package org.telegram.divo.dal.dto.user

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import org.telegram.divo.common.arch.PaginatedResult
import org.telegram.divo.entity.SavedProfile

/** Body of POST /user/block and POST /user/unblock. */
class UserBlockRequest(
    @SerializedName("userId") val userId: Int
)

/** Body of POST /user/blocked. */
class UserBlockedListRequest(
    @SerializedName("offset") val offset: Int,
    @SerializedName("limit") val limit: Int
)

/**
 * POST /user/blocked only declares a JSend `data` object, so the list is read leniently:
 * `data.items` (or `data` itself as an array), a user either flat or nested under `user`/`blockedUser`,
 * and the total from the usual pagination shapes.
 */
fun JsonObject.toBlockedUsers(offset: Int, limit: Int): PaginatedResult<SavedProfile> {
    val data = get("data")
    val dataObject = data?.takeIf { it.isJsonObject }?.asJsonObject
    val itemsArray: JsonArray = when {
        data != null && data.isJsonArray -> data.asJsonArray
        dataObject != null -> listOf("items", "users", "list", "blocked")
            .firstNotNullOfOrNull { key -> dataObject.get(key)?.takeIf { it.isJsonArray }?.asJsonArray }
            ?: JsonArray()
        else -> JsonArray()
    }

    val items = itemsArray.mapNotNull { element ->
        val item = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
        val user = listOf("user", "blockedUser", "blocked_user")
            .firstNotNullOfOrNull { key -> item.get(key)?.takeIf { it.isJsonObject }?.asJsonObject }
            ?: item
        val id = user.intOrNull("id")
            ?: item.intOrNull("userId") ?: item.intOrNull("blockedUserId") ?: item.intOrNull("blocked_user_id")
            ?: return@mapNotNull null
        SavedProfile(
            id = id,
            fullName = user.stringOrNull("fullName") ?: user.stringOrNull("full_name") ?: user.stringOrNull("name").orEmpty(),
            role = user.stringOrNull("role").orEmpty(),
            roleLabel = user.stringOrNull("roleLabel").orEmpty(),
            avatarUrl = user.photoUrl("avatar") ?: user.photoUrl("photo").orEmpty()
        )
    }

    val pagination = dataObject?.get("pagination")?.takeIf { it.isJsonObject }?.asJsonObject
    val total = pagination?.get("meta")?.takeIf { it.isJsonObject }?.asJsonObject?.intOrNull("totalCount")
        ?: pagination?.intOrNull("totalCount")
        ?: pagination?.intOrNull("total")
        ?: dataObject?.intOrNull("total")
        // No total reported: a full page means there may be more
        ?: if (itemsArray.size() >= limit) offset + itemsArray.size() + 1 else offset + itemsArray.size()

    return PaginatedResult(items = items, totalCount = total)
}

private fun JsonObject.member(key: String): JsonElement? = get(key)?.takeIf { !it.isJsonNull }

private fun JsonObject.intOrNull(key: String): Int? =
    member(key)?.takeIf { it.isJsonPrimitive }?.let { runCatching { it.asInt }.getOrNull() }

private fun JsonObject.stringOrNull(key: String): String? =
    member(key)?.takeIf { it.isJsonPrimitive }?.asString

private fun JsonObject.photoUrl(key: String): String? {
    val value = member(key) ?: return null
    return when {
        value.isJsonObject -> value.asJsonObject.stringOrNull("fullUrl")
        value.isJsonPrimitive -> value.asString
        else -> null
    }?.takeIf { it.isNotBlank() }
}

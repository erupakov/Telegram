package org.telegram.divo.apitest

import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.telegram.divo.dal.api.PublicationService
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.dto.publication.LikeRequest
import org.telegram.divo.dal.dto.publication.PublicationListResponse
import org.telegram.divo.dal.dto.publication.toEntities
import org.telegram.divo.dal.dto.user.UserGalleryListResponse
import org.telegram.divo.dal.dto.user.toEntities
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/** Likes and views of single photos (user-gallery) and videos (publications). */
class MediaEngagementTest {

    private val backend = FakeBackend(FakeTokenProvider("user-token"))

    @AfterEach
    fun tearDown() = backend.close()

    @Test
    fun `photo like and unlike post the gallery item id`() = runTest {
        val users = backend.service<UserService>()
        backend.enqueueJson("""{"status":"success"}""")
        backend.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(204))
        assertIs<DivoResult.Success<*>>(resultOf { users.likeGalleryItem(LikeRequest(30101)) })
        assertIs<DivoResult.Success<*>>(resultOf { users.unlikeGalleryItem(LikeRequest(30101)) }, "an empty 204 is a success too")

        val like = backend.takeRequest()
        assertEquals("/api/user-gallery/like", like.path)
        assertEquals("Bearer user-token", like.getHeader("Authorization"))
        assertEquals(30101, JsonParser.parseString(like.body.readUtf8()).asJsonObject["id"].asInt)
        assertEquals("/api/user-gallery/unlike", backend.takeRequest().path)
    }

    @Test
    fun `video like and unlike post the publication id`() = runTest {
        val publications = backend.service<PublicationService>()
        backend.enqueueJson("""{"status":"success"}""")
        backend.enqueueJson("""{"status":"success"}""")
        publications.likePublication(LikeRequest(55))
        publications.unlikePublication(LikeRequest(55))

        val like = backend.takeRequest()
        assertEquals("/api/publication/like", like.path)
        assertEquals(55, JsonParser.parseString(like.body.readUtf8()).asJsonObject["id"].asInt)
        assertEquals("/api/publication/unlike", backend.takeRequest().path)
    }

    @Test
    fun `gallery list without viewsCount hides views, with it shows them`() {
        val item = """{"id":1,"photo":{"fullUrl":"https://x/p.png"},"likesCount":7,"isLikedByUser":true,"preview":{"fullUrl":"https://x/t.png"}%s}"""
        fun parse(extra: String) = appGson.fromJson(
            """{"data":{"items":[${item.format(extra)}],"pagination":{"meta":{"limit":12,"currentOffset":0,"totalCount":1}}}}""",
            UserGalleryListResponse::class.java
        ).toEntities().items.single()

        val stage = parse("")
        assertEquals(7, stage.likesCount)
        assertEquals(true, stage.isLikedByUser)
        assertNull(stage.viewsCount, "stage doesn't send viewsCount yet")
        assertEquals(1234, parse(""","viewsCount":1234""").viewsCount)
    }

    @Test
    fun `publication list maps likes and optional views`() {
        val json = """{"data":{"items":[
            {"id":55,"title":null,"description":null,"type":"video","likesCount":3,"isLikedByUser":false,"viewsCount":40,
             "files":[{"order":0,"fileName":"v.mp4","fullUrl":"https://x/v.mp4","fileUuid":"u","extension":"mp4","description":null}]}],
            "pagination":{"meta":{"limit":12,"currentOffset":0,"totalCount":1}}}}"""
        val publication = appGson.fromJson(json, PublicationListResponse::class.java).toEntities().items.single()
        assertEquals(3, publication.likesCount)
        assertEquals(40, publication.viewsCount)
    }
}

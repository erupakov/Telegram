package org.telegram.divo.common.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.core.graphics.scale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.telegram.divo.entity.Publication
import org.telegram.divo.entity.PublicationFile
import java.util.concurrent.ConcurrentHashMap

class ThumbnailProcessor(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(
        Runtime.getRuntime().availableProcessors()
    )
) {
    companion object {
        private const val THUMB_SIZE = 256
        private val emptyHeaders: Map<String, String> = emptyMap()
    }

    private val inFlight = ConcurrentHashMap<String, Deferred<Bitmap?>>()

    suspend fun withThumbnails(publications: List<Publication>): List<Publication> =
        coroutineScope {
            publications.map { pub ->
                async {
                    pub.copy(
                        files = pub.files.map { file ->
                            file.copy(thumbnailBitmap = getThumbnail(file))
                        }
                    )
                }
            }.awaitAll()
        }

    private suspend fun getThumbnail(file: PublicationFile): Bitmap? {
        if (!file.isVideo) return null

        val deferred = coroutineScope {
            inFlight.computeIfAbsent(file.fullUrl) {
                async(dispatcher) {
                    try {
                        extractThumbnail(file)
                    } finally {
                        inFlight.remove(file.fullUrl)
                    }
                }
            }
        }
        return deferred.await()
    }

    private fun extractThumbnail(file: PublicationFile): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.fullUrl, emptyHeaders)

            val original = retriever.getFrameAtTime(
                0L,
                MediaMetadataRetriever.OPTION_NEXT_SYNC
            ) ?: return null

            val (targetW, targetH) = scaledDimensions(original.width, original.height)

            if (targetW == original.width && targetH == original.height) original
            else original.scale(targetW, targetH).also {
                if (it !== original) original.recycle()
            }
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun scaledDimensions(w: Int, h: Int): Pair<Int, Int> =
        if (w > h)
            THUMB_SIZE to (THUMB_SIZE * h.toFloat() / w).toInt().coerceAtLeast(1)
        else
            (THUMB_SIZE * w.toFloat() / h).toInt().coerceAtLeast(1) to THUMB_SIZE
}
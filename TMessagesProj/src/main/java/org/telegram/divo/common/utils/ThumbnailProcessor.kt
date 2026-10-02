package org.telegram.divo.common.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.core.graphics.scale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Deferred
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
                            async {
                                file.copy(thumbnailBitmap = getThumbnail(file))
                            }
                        }.awaitAll()
                    )
                }
            }.awaitAll()
        }

    private suspend fun getThumbnail(file: PublicationFile): Bitmap? = coroutineScope {
        val deferred = inFlight.computeIfAbsent(file.fullUrl) {
            async(dispatcher) {
                try {
                    extractThumbnail(file)
                } finally {
                    inFlight.remove(file.fullUrl)
                }
            }
        }

        deferred.await()
    }

    private fun extractThumbnail(file: PublicationFile): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.fullUrl, emptyHeaders)

            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val originalW = widthStr?.toIntOrNull() ?: 0
            val originalH = heightStr?.toIntOrNull() ?: 0

            if (originalW > 0 && originalH > 0) {
                val (targetW, targetH) = scaledDimensions(originalW, originalH)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    retriever.getScaledFrameAtTime(
                        0L,
                        MediaMetadataRetriever.OPTION_NEXT_SYNC,
                        targetW,
                        targetH
                    )
                } else {
                    val original = retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_NEXT_SYNC) ?: return null
                    original.scale(targetW, targetH).also {
                        if (it !== original) original.recycle()
                    }
                }
            } else {
                retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_NEXT_SYNC)
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
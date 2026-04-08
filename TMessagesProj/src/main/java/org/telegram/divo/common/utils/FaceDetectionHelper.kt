package org.telegram.divo.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.abs

data class ProcessedFaces(
    val faces: List<Face>,
    val imageWidth: Int,
    val imageHeight: Int
)

object FaceDetectionHelper {
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.20f)
            .build()
    )

    suspend fun detect(context: Context, uri: Uri): ProcessedFaces? {
        return try {
            val bitmap = loadBitmap(context, uri) ?: return null
            val image = InputImage.fromBitmap(bitmap, 0)

            val faces = detector.process(image).await()
                .filterReal()
                .filterByShape()

            ProcessedFaces(faces, bitmap.width, bitmap.height)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun loadBitmap(context: Context, uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val stream = if (uri.scheme == "http" || uri.scheme == "https") {
                    val cachedUri = ImageCacheHelper.getLocalUri(context, uri.toString())
                        ?: return@withContext null
                    context.contentResolver.openInputStream(cachedUri)
                } else {
                    context.contentResolver.openInputStream(uri)
                }

                val bitmap = stream?.use { BitmapFactory.decodeStream(it) } ?: return@withContext null

                val rotation = context.contentResolver.openInputStream(uri)?.use { input ->
                    ExifInterface(input).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL

                val degrees = when (rotation) {
                    ExifInterface.ORIENTATION_ROTATE_90  -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }

                if (degrees == 0f) bitmap
                else {
                    val matrix = Matrix().apply { postRotate(degrees) }
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun List<Face>.filterReal(): List<Face> = filter { face ->
        val hasEyes = face.getLandmark(FaceLandmark.LEFT_EYE) != null &&
                face.getLandmark(FaceLandmark.RIGHT_EYE) != null
        val hasNose = face.getLandmark(FaceLandmark.NOSE_BASE) != null

        val isLookingStraight = abs(face.headEulerAngleY) < 35f &&
                abs(face.headEulerAngleZ) < 35f

        hasEyes && hasNose && isLookingStraight
    }

    private fun List<Face>.filterByShape(): List<Face> = filter { face ->
        val ratio = face.boundingBox.width().toFloat() / face.boundingBox.height()
        ratio in 0.6f..1.1f
    }
}
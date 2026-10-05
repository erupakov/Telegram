package org.telegram.divo.components.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.messenger.FileLog
import org.telegram.messenger.R
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val MAX_SOURCE_SIDE = 2560
private const val MAX_ZOOM = 5f

/**
 * Full-screen rectangular crop: a frame with a fixed [aspectRatio] (width / height) over the image,
 * the image is moved and pinch-zoomed under it. [onCropped] gets a JPEG of the framed area.
 */
@Composable
fun ImageCropDialog(
    uri: Uri,
    aspectRatio: Float,
    onCropped: (File) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }
    var failed by remember(uri) { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(uri) {
        val loaded = withContext(Dispatchers.IO) { context.loadOrientedBitmap(uri) }
        if (loaded == null) failed = true else bitmap = loaded
    }
    LaunchedEffect(failed) {
        if (failed) onDismiss()
    }

    Dialog(
        onDismissRequest = { if (!saving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val source = bitmap
            if (source == null) {
                LottieProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                    color = Color.White
                )
            } else {
                CropArea(
                    bitmap = source,
                    aspectRatio = aspectRatio,
                    enabled = !saving,
                    onDone = { cropRect ->
                        saving = true
                        scope.launch {
                            val file = withContext(Dispatchers.Default) { context.saveCrop(source, cropRect) }
                            saving = false
                            if (file != null) onCropped(file) else onDismiss()
                        }
                    },
                    onCancel = onDismiss
                )
                if (saving) {
                    LottieProgressIndicator(
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CropArea(
    bitmap: Bitmap,
    aspectRatio: Float,
    enabled: Boolean,
    onDone: (android.graphics.Rect) -> Unit,
    onCancel: () -> Unit,
) {
    val image: ImageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val bw = bitmap.width.toFloat()
    val bh = bitmap.height.toFloat()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val areaW = with(density) { maxWidth.toPx() }
        val areaH = with(density) { maxHeight.toPx() }
        val margin = with(density) { 16.dp.toPx() }
        val controlsH = with(density) { 96.dp.toPx() }

        // Frame: as wide as the screen allows, keeping the aspect ratio
        var frameW = areaW - margin * 2
        var frameH = frameW / aspectRatio
        val maxFrameH = areaH - controlsH * 2
        if (frameH > maxFrameH) {
            frameH = maxFrameH
            frameW = frameH * aspectRatio
        }
        val frame = Rect(
            offset = Offset((areaW - frameW) / 2f, (areaH - frameH) / 2f),
            size = Size(frameW, frameH)
        )
        // Scale at which the image just covers the frame
        val baseScale = max(frameW / bw, frameH / bh)

        var zoom by remember(bitmap, aspectRatio) { mutableFloatStateOf(1f) }
        // Image center relative to the frame center
        var offsetX by remember(bitmap, aspectRatio) { mutableFloatStateOf(0f) }
        var offsetY by remember(bitmap, aspectRatio) { mutableFloatStateOf(0f) }

        fun clampOffsets(scale: Float) {
            val maxX = (bw * scale - frameW) / 2f
            val maxY = (bh * scale - frameH) / 2f
            offsetX = offsetX.coerceIn(-maxX, maxX)
            offsetY = offsetY.coerceIn(-maxY, maxY)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(bitmap, aspectRatio, enabled, frameW, frameH) {
                    if (!enabled) return@pointerInput
                    detectTransformGestures { centroid, pan, zoomChange, _ ->
                        val oldScale = baseScale * zoom
                        zoom = (zoom * zoomChange).coerceIn(1f, MAX_ZOOM)
                        val newScale = baseScale * zoom
                        val k = newScale / oldScale
                        // Zoom around the gesture centroid
                        val fx = centroid.x - frame.center.x
                        val fy = centroid.y - frame.center.y
                        offsetX = (offsetX - fx) * k + fx + pan.x
                        offsetY = (offsetY - fy) * k + fy + pan.y
                        clampOffsets(newScale)
                    }
                }
        ) {
            val scale = baseScale * zoom
            val drawW = bw * scale
            val drawH = bh * scale
            val left = frame.center.x + offsetX - drawW / 2f
            val top = frame.center.y + offsetY - drawH / 2f
            drawImage(
                image = image,
                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt())
            )

            // Dim everything outside the frame
            val dim = Color.Black.copy(alpha = 0.6f)
            drawRect(dim, Offset.Zero, Size(size.width, frame.top))
            drawRect(dim, Offset(0f, frame.bottom), Size(size.width, size.height - frame.bottom))
            drawRect(dim, Offset(0f, frame.top), Size(frame.left, frame.height))
            drawRect(dim, Offset(frame.right, frame.top), Size(size.width - frame.right, frame.height))

            // Rule-of-thirds grid and the frame border
            val gridColor = Color.White.copy(alpha = 0.35f)
            for (i in 1..2) {
                val x = frame.left + frame.width * i / 3f
                val y = frame.top + frame.height * i / 3f
                drawLine(gridColor, Offset(x, frame.top), Offset(x, frame.bottom), strokeWidth = 1f)
                drawLine(gridColor, Offset(frame.left, y), Offset(frame.right, y), strokeWidth = 1f)
            }
            drawRect(
                color = Color.White,
                topLeft = frame.topLeft,
                size = frame.size,
                style = Stroke(width = with(density) { 1.5.dp.toPx() })
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.clickableWithoutRipple { if (enabled) onCancel() },
                text = stringResource(R.string.ButtonCancel),
                color = Color.White,
                fontSize = 17.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                modifier = Modifier.clickableWithoutRipple {
                    if (!enabled) return@clickableWithoutRipple
                    val scale = baseScale * zoom
                    val cropLeft = (bw * scale / 2f - offsetX - frameW / 2f) / scale
                    val cropTop = (bh * scale / 2f - offsetY - frameH / 2f) / scale
                    val cropW = frameW / scale
                    val cropH = frameH / scale
                    val l = cropLeft.roundToInt().coerceIn(0, bitmap.width - 1)
                    val t = cropTop.roundToInt().coerceIn(0, bitmap.height - 1)
                    onDone(
                        android.graphics.Rect(
                            l,
                            t,
                            min(bitmap.width, l + cropW.roundToInt().coerceAtLeast(1)),
                            min(bitmap.height, t + cropH.roundToInt().coerceAtLeast(1))
                        )
                    )
                },
                text = stringResource(R.string.ButtonDone),
                color = Color.White,
                fontSize = 17.sp
            )
        }
    }
}

/** Decodes [uri] downsampled to at most [MAX_SOURCE_SIDE] and rotated per its EXIF orientation. */
private fun Context.loadOrientedBitmap(uri: Uri): Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SOURCE_SIDE) sample *= 2
    val decoded = contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    }
    val orientation = contentResolver.openInputStream(uri)?.use {
        ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } ?: ExifInterface.ORIENTATION_NORMAL
    decoded?.let { applyExifOrientation(it, orientation) }
} catch (e: Throwable) {
    FileLog.e(e)
    null
}

private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
        else -> return bitmap
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        .also { if (it != bitmap) bitmap.recycle() }
}

private fun Context.saveCrop(source: Bitmap, rect: android.graphics.Rect): File? = try {
    val cropped = Bitmap.createBitmap(source, rect.left, rect.top, rect.width(), rect.height())
    val file = File.createTempFile("crop_", ".jpg", cacheDir)
    file.outputStream().use { cropped.compress(Bitmap.CompressFormat.JPEG, 92, it) }
    if (cropped != source) cropped.recycle()
    file
} catch (e: Throwable) {
    FileLog.e(e)
    null
}

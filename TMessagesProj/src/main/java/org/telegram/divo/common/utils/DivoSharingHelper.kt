package org.telegram.divo.common.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.telegram.divo.dal.network.DivoApiConfig
import java.io.File
import java.io.FileOutputStream
import java.net.URL

enum class DivoShareType(val pathSegment: String) {
    PROFILE("profile"),
    EVENT("event"),
    CHANNEL("channel");

    fun buildUrl(id: Int? = null): String {
        val webUrl = DivoApiConfig.WEB_URL

        return buildString {
            append(webUrl)
            append(pathSegment)
            if (id != null) {
                append("/")
                append(id)
            }
        }
    }
}

object DivoSharingHelper {
    fun share(
        context: Context,
        scope: CoroutineScope,
        type: DivoShareType,
        id: Int? = null,
        imageUrl: String? = null,
        customMessage: String? = null
    ) {
        val shareUrl = type.buildUrl(id)
        val textBody = "$customMessage\n$shareUrl"

        if (imageUrl.isNullOrBlank()) {
            openShareSheet(context, textBody, null)
        } else {
            scope.launch(Dispatchers.IO) {
                val downloadedFile = ImageCacheHelper.getLocalUri(context, imageUrl)
                    ?.let { File(context.cacheDir, "share_preview_temp.jpg") }
                withContext(Dispatchers.Main) {
                    openShareSheet(context, textBody, downloadedFile)
                }
            }
        }
    }

    private fun openShareSheet(
        context: Context,
        textBody: String,
        imageFile: File?
    ) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textBody)

            if (imageFile != null && imageFile.exists()) {
                val imageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                )

                clipData = ClipData.newUri(context.contentResolver, "Preview", imageUri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
        }

        val shareIntent = Intent.createChooser(sendIntent, "Поделиться...")
        context.startActivity(shareIntent)
    }
}

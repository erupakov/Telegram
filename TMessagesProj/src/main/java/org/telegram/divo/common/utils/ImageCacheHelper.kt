package org.telegram.divo.common.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object ImageCacheHelper {
    suspend fun getLocalUri(context: Context, imageUrl: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "cached_${imageUrl.hashCode()}.jpg"
                val file = File(context.cacheDir, fileName)

                if (!file.exists()) {
                    URL(imageUrl).openStream().use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun cacheUri(context: Context, sourceUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (sourceUri.scheme == "file" || sourceUri.scheme?.startsWith("http") == true) {
                    return@withContext sourceUri.toString()
                }
                
                val fileName = "fr_history_${System.currentTimeMillis()}_${sourceUri.hashCode()}.jpg"
                val file = File(context.cacheDir, fileName)

                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
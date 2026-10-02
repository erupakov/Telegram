package org.telegram.divo.common.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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
                if (sourceUri.scheme == "file") {
                    val path = sourceUri.path
                    return@withContext path
                }
                
                val fileName = "fr_history_${System.currentTimeMillis()}_${sourceUri.hashCode()}.jpg"
                val file = File(context.cacheDir, fileName)

                val success = openInputStream(context, sourceUri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                        true
                    }
                } ?: false

                if (success) {
                    file.absolutePath
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun savePersistent(context: Context, sourceUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val dir = File(context.filesDir, "fr_history")
                if (!dir.exists()) {
                    dir.mkdirs()
                }

                val fileName = "fr_hist_${System.currentTimeMillis()}_${sourceUri.hashCode()}.jpg"
                val file = File(dir, fileName)

                val success = openInputStream(context, sourceUri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                        true
                    }
                } ?: false

                if (success) {
                    file.absolutePath
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun openInputStream(context: Context, uri: Uri): InputStream? {
        return try {
            if (uri.scheme?.startsWith("http") == true) {
                URL(uri.toString()).openStream()
            } else {
                context.contentResolver.openInputStream(uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
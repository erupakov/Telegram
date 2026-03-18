package org.telegram.divo.common

import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

object AppImageLoader {
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        Coil.setImageLoader(
            ImageLoader.Builder(context.applicationContext)
                .memoryCache {
                    MemoryCache.Builder(context)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(context.cacheDir.resolve("image_cache"))
                        .maxSizeBytes(100 * 1024 * 1024)
                        .build()
                }
                .crossfade(false)
                .build()
        )
    }
}
package org.telegram.divo.entity

class Photo(
    val photoId: Long,
    val fileName: String = "",
    val fullUrl: String = "",
    val extension: String = "",
    val fileUuid: String = ""
)
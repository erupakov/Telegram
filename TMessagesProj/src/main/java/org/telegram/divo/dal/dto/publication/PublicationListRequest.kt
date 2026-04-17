package org.telegram.divo.dal.dto.publication

class PublicationListRequest(
    val offset: Int,
    val limit: Int,
    val type: String,
    val userId: Int
)
package org.telegram.divo.entity

data class City(
    val id: Int = -1,
    val countryCode: String = "",
    val countryName: String = "",
    val areaName: String = "",
    val name: String = "",
    /** GeoNames id: the same city in the bundled cities.txt (LocalCity.id); null for cities not from GeoNames */
    val geonamesId: Long? = null,
)
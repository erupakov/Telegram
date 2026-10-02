package org.telegram.divo.dal.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.telegram.divo.entity.LocalCountry
import org.telegram.divo.screen.search.LocalCity
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController
import java.io.BufferedReader
import java.io.InputStreamReader

class LocationRepository {

    private val excludedCodes = setOf("FT", "GO", "YL")

    suspend fun getCountries(): List<LocalCountry> = withContext(Dispatchers.IO) {
        val list = mutableListOf<LocalCountry>()
        try {
            val stream = ApplicationLoader.applicationContext.assets.open("countries.txt")
            val reader = BufferedReader(InputStreamReader(stream))
            reader.forEachLine { line ->
                val args = line.split(";")
                if (args.size >= 3) {
                    val code = args[0]
                    val shortname = args[1]
                    if (shortname.uppercase() !in excludedCodes) {
                        val defaultName = args[2]
                        val locName = LocaleController.getCountryName(shortname)
                        val name = if (!locName.isNullOrEmpty()) locName else defaultName
                        val flag = LocaleController.getLanguageFlag(shortname)
                        list.add(
                            LocalCountry(
                                code = code,
                                shortName = shortname,
                                name = name,
                                flag = flag
                            )
                        )
                    }
                }
            }
            reader.close()
            stream.close()
            list.sortBy { it.name }
        } catch (e: Exception) {
            FileLog.e(e)
        }
        list
    }

    suspend fun getCities(): List<LocalCity> = withContext(Dispatchers.IO) {
        val cities = mutableListOf<LocalCity>()
        try {
            ApplicationLoader.applicationContext.assets.open("cities.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val columns = line.split("\t")
                    if (columns.size > 14) {
                        val id = columns[0].toLongOrNull() ?: return@forEach
                        cities.add(
                            LocalCity(
                                id = id,
                                name = columns[1],
                                asciiName = columns[2],
                                alternateNames = columns[3],
                                countryCode = columns[8],
                                population = columns[14].toIntOrNull() ?: 0
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            FileLog.e(e)
        }
        cities
    }
}

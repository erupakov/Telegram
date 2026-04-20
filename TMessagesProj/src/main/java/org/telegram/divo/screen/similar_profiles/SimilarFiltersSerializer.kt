package org.telegram.divo.screen.similar_profiles

import org.json.JSONArray
import org.json.JSONObject
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter

internal data class SimilarFiltersPayload(
    val countryShortNames: List<String> = emptyList(),
    val countryNames: List<String> = emptyList(),
    val similarityPercent: Int = MIN_SIMILARITY,
    val roleValue: String = "",
    val blockParams: List<ProfileParameter> = emptyList()
)

internal object SimilarFiltersSerializer {
    fun serialize(state: State): String {
        val root = JSONObject()
            .put("countryShortNames", JSONArray(state.selectedCountries.map { it.shortName }))
            .put("countryNames", JSONArray(state.selectedCountries.map { it.name }))
            .put("similarityPercent", state.similarityPercent)
            .put("roleValue", state.role.value)

        val params = JSONArray()
        state.blockParams.forEach { param ->
            params.put(
                JSONObject()
                    .put("type", param.type.name)
                    .put("value", param.value)
            )
        }

        root.put("blockParams", params)
        return root.toString()
    }

    fun deserialize(json: String?): SimilarFiltersPayload? {
        if (json.isNullOrBlank()) return null

        return runCatching {
            val root = JSONObject(json)
            val countries = root.optJSONArray("countryShortNames")
                ?.let { array ->
                    buildList {
                        for (index in 0 until array.length()) {
                            add(array.optString(index))
                        }
                    }
                }
                .orEmpty()
                .filter { it.isNotBlank() }
            val countryNames = root.optJSONArray("countryNames")
                ?.let { array ->
                    buildList {
                        for (index in 0 until array.length()) {
                            add(array.optString(index))
                        }
                    }
                }
                .orEmpty()
                .filter { it.isNotBlank() }

            val roleValue = root.optString("roleValue")
            val similarityPercent = root.optInt("similarityPercent", MIN_SIMILARITY)
            val blockParams = root.optJSONArray("blockParams")
                ?.let { array ->
                    buildList {
                        for (index in 0 until array.length()) {
                            val item = array.optJSONObject(index) ?: continue
                            val type = runCatching {
                                ParametersType.valueOf(item.optString("type"))
                            }.getOrNull() ?: continue
                            add(ProfileParameter(type = type, value = item.optString("value")))
                        }
                    }
                }
                .orEmpty()

            SimilarFiltersPayload(
                countryShortNames = countries,
                countryNames = countryNames,
                similarityPercent = similarityPercent,
                roleValue = roleValue,
                blockParams = blockParams
            )
        }.getOrNull()
    }
}

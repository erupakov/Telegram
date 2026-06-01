package org.telegram.divo.dal.dto.event

import com.google.gson.annotations.SerializedName
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.numericFilterRange
import org.telegram.divo.components.items.resolveNumericBlockParamBounds
import org.telegram.divo.screen.event_create.State
import java.text.SimpleDateFormat
import java.util.Locale

fun State.toCreateEventRequest(uploadedFiles: List<org.telegram.divo.entity.UploadedFile>): CreateEventRequest {
    fun getAppearanceIds(options: List<org.telegram.divo.entity.AppearanceItem>, paramValue: String): List<Int> {
        if (paramValue.isEmpty() || paramValue.contains("All", ignoreCase = true)) {
            return options.mapNotNull { it.id }
        }

        return options.filter { it.title?.lowercase()?.contains(paramValue.lowercase()) == true }
            .mapNotNull { it.id }
    }

    fun blockNumericRangeToDto(type: ParametersType, raw: String): EventRangeDto {
        val bounds = checkNotNull(type.numericFilterRange())
        val (from, to) = resolveNumericBlockParamBounds(raw, bounds)
        return EventRangeDto(from = from?.toDouble(), to = to?.toDouble())
    }

    fun mapRoleLabelToApiType(roleValue: String): List<String> {
        if (roleValue.isEmpty() || roleValue.contains("All", ignoreCase = true)) {
            return listOf(
                org.telegram.divo.entity.RoleType.MODEL.value,
                org.telegram.divo.entity.RoleType.FAN.value,
                org.telegram.divo.entity.RoleType.NEW_FACE.value
            )
        }
        return roleValue
            .split(",")
            .map { it.trim().lowercase().replace(" ", "_") }
            .map { role ->
                when (role) {
                    org.telegram.divo.entity.RoleType.NEW_TALENT.value -> org.telegram.divo.entity.RoleType.NEW_FACE.value
                    else -> role
                }
            }
            .filter { it.isNotEmpty() }
    }

    fun mapGenderLabelToApiType(genderValue: String): List<String> {
        if (genderValue.isEmpty() || genderValue.contains("All", ignoreCase = true)) {
            return listOf("male", "female")
        }
        return genderValue
            .split(",")
            .map { it.trim().lowercase() }
            .mapNotNull { 
                when(it) {
                    "male" -> "male"
                    "female" -> "female"
                    else -> null
                }
            }
    }

    fun formatDateForApi(dateStr: String, timeStr: String, hoursOffset: Int = 0): String {
        val dateFormats = listOf(
            "d MMM yyyy",
            "dd MMM yyyy",
            "d MMMM yyyy",
            "dd MMMM yyyy",
            "yyyy-MM-dd",
            "dd.MM.yyyy",
            "dd/MM/yyyy"
        )
        
        val timeFormats = listOf(
            "h:mm a",
            "HH:mm",
            "HH:mm:ss"
        )
        
        var parsedDate: java.util.Date? = null
        var parsedTime: java.util.Date? = null

        for (format in dateFormats) {
            try {
                val sdf = SimpleDateFormat(format, org.telegram.divo.dal.network.DivoLanguageManager.getSystemLocale())
                parsedDate = sdf.parse(dateStr)
                if (parsedDate != null) break
            } catch (_: Exception) {}
        }

        for (format in timeFormats) {
            try {
                val sdf = SimpleDateFormat(format, org.telegram.divo.dal.network.DivoLanguageManager.getSystemLocale())
                parsedTime = sdf.parse(timeStr)
                if (parsedTime != null) break
            } catch (_: Exception) {}
        }

        if (parsedDate == null || parsedTime == null) {
            val calendar = java.util.Calendar.getInstance()
            if (parsedDate != null) {
                val dateCal = java.util.Calendar.getInstance()
                dateCal.time = parsedDate
                calendar.set(java.util.Calendar.YEAR, dateCal.get(java.util.Calendar.YEAR))
                calendar.set(java.util.Calendar.MONTH, dateCal.get(java.util.Calendar.MONTH))
                calendar.set(java.util.Calendar.DAY_OF_MONTH, dateCal.get(java.util.Calendar.DAY_OF_MONTH))
            }
            if (parsedTime != null) {
                val timeCal = java.util.Calendar.getInstance()
                timeCal.time = parsedTime
                calendar.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
                calendar.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
                calendar.set(java.util.Calendar.SECOND, 0)
            }
            calendar.add(java.util.Calendar.HOUR_OF_DAY, hoursOffset)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return outputFormat.format(calendar.time)
        }

        val dateCal = java.util.Calendar.getInstance()
        dateCal.time = parsedDate
        
        val timeCal = java.util.Calendar.getInstance()
        timeCal.time = parsedTime
        
        dateCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
        dateCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
        dateCal.set(java.util.Calendar.SECOND, 0)
        dateCal.add(java.util.Calendar.HOUR_OF_DAY, hoursOffset)
        
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return outputFormat.format(dateCal.time)
    }

    val formattedDate = formatDateForApi(eventDate, eventTime)
    val formattedDateTo = formatDateForApi(eventDate, eventTime, hoursOffset = 4)
    val formattedDeadline = if (deadlineDate.isNotEmpty()) {
        formatDateForApi(deadlineDate, deadlineTime)
    } else {
        null
    }

    return CreateEventRequest(
        title = eventName,
        description = eventDescription,
        typeId = selectedEventType?.id ?: 1, // FIXME: Add proper typeId mapping
        date = formattedDate,
        dateTo = formattedDateTo, // Event doesn't have an end date in UI currently, so using start date + 4h
        isPublic = isPublicEvent,
        ndaRequired = isNdaRequired,
        applicationDeadline = formattedDeadline,
        maxAttendees = maxParticipants,
        requirements = eventRequirements,
        address = CreateEventAddressRequest(
            street = "Some street", // FIXME: Add address fields to UI
            house = "100B",
            apartment = "123",
            formatted = selectedCountries.firstOrNull()?.name ?: "",
            latitude = 51.507351, // FIXME: Add geolocation
            longitude = -0.127758,
            cityId = 1 // FIXME: Add city selection
        ),
        files = uploadedFiles.mapIndexed { index, file ->
            CreateEventFileRequest(
                order = index + 1,
                fileUuid = file.uuid
            )
        },
        paymentType = selectedPaymentType?.id ?: 2,
        paymentFrequency = selectedPaymentFrequency?.id ?: 1,
        cost = eventRate.toIntOrNull() ?: 0,
        role = mapRoleLabelToApiType(role.value),
        gender = mapGenderLabelToApiType(gender.value),
        age = blockNumericRangeToDto(ParametersType.AGE, blockParams.find { it.type == ParametersType.AGE }?.value.orEmpty()),
        height = blockNumericRangeToDto(ParametersType.HEIGHT, blockParams.find { it.type == ParametersType.HEIGHT }?.value.orEmpty()),
        weight = blockNumericRangeToDto(ParametersType.WEIGHT, blockParams.find { it.type == ParametersType.WEIGHT }?.value.orEmpty()),
        breastSize = blockNumericRangeToDto(ParametersType.BREAST_SIZE, blockParams.find { it.type == ParametersType.BREAST_SIZE }?.value.orEmpty()),
        waist = blockNumericRangeToDto(ParametersType.WAIST, blockParams.find { it.type == ParametersType.WAIST }?.value.orEmpty()),
        hips = blockNumericRangeToDto(ParametersType.HIPS, blockParams.find { it.type == ParametersType.HIPS }?.value.orEmpty()),
        shoesSize = blockNumericRangeToDto(ParametersType.SHOE_SIZE, blockParams.find { it.type == ParametersType.SHOE_SIZE }?.value.orEmpty()),
        measuringSystem = "metric", // FIXME: Add system selection
        hairColor = getAppearanceIds(hairColorOptions, hairColor.value),
        hairLength = getAppearanceIds(hairLengthOptions, hairLength.value),
        eyeColor = getAppearanceIds(eyeColorOptions, eyeColor.value),
        skinColor = getAppearanceIds(skinColorOptions, skinColor.value)
    )
}

class CreateEventRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("typeId") val typeId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("dateTo") val dateTo: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("ndaRequired") val ndaRequired: Boolean,
    @SerializedName("applicationDeadline") val applicationDeadline: String?,
    @SerializedName("maxAttendees") val maxAttendees: Int,
    @SerializedName("requirements") val requirements: String,
    @SerializedName("address") val address: CreateEventAddressRequest,
    @SerializedName("files") val files: List<CreateEventFileRequest>?,
    @SerializedName("paymentType") val paymentType: Int,
    @SerializedName("paymentFrequency") val paymentFrequency: Int,
    @SerializedName("cost") val cost: Int,
    @SerializedName("role") val role: List<String>,
    @SerializedName("gender") val gender: List<String>,
    @SerializedName("age") val age: EventRangeDto?,
    @SerializedName("height") val height: EventRangeDto?,
    @SerializedName("weight") val weight: EventRangeDto?,
    @SerializedName("breastSize") val breastSize: EventRangeDto?,
    @SerializedName("waist") val waist: EventRangeDto?,
    @SerializedName("hips") val hips: EventRangeDto?,
    @SerializedName("shoesSize") val shoesSize: EventRangeDto?,
    @SerializedName("measuringSystem") val measuringSystem: String,
    @SerializedName("hairColor") val hairColor: List<Int>?,
    @SerializedName("hairLength") val hairLength: List<Int>?,
    @SerializedName("eyeColor") val eyeColor: List<Int>?,
    @SerializedName("skinColor") val skinColor: List<Int>?
)

class CreateEventAddressRequest(
    @SerializedName("street") val street: String,
    @SerializedName("house") val house: String,
    @SerializedName("apartment") val apartment: String,
    @SerializedName("formatted") val formatted: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("cityId") val cityId: Int
)

class CreateEventFileRequest(
    @SerializedName("order") val order: Int,
    @SerializedName("fileUuid") val fileUuid: String
)

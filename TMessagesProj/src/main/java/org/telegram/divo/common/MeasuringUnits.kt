package org.telegram.divo.common

import org.telegram.divo.components.items.ParametersType
import kotlin.math.roundToInt

object MeasuringUnits {
    fun isImperial(measuringSystem: String = DivoSettings.measuringSystem): Boolean =
        measuringSystem == DivoSettings.SYSTEM_IMPERIAL

    fun cmToInches(cm: Int): Int = (cm / 2.54).roundToInt()

    fun inchesToCm(inches: Int): Int = (inches * 2.54).roundToInt()

    fun kgToLb(kg: Int): Int = (kg * 2.20462).roundToInt()

    fun lbToKg(lb: Int): Int = (lb / 2.20462).roundToInt()

    fun euShoeToUs(eu: Int): Int = (eu - 30).coerceAtLeast(1)

    fun usShoeToEu(us: Int): Int = us + 30

    fun toDisplayValue(type: ParametersType, metricValue: Int): Int = when (type) {
        ParametersType.HEIGHT,
        ParametersType.WAIST,
        ParametersType.HIPS,
        ParametersType.BREAST_SIZE -> cmToInches(metricValue)
        ParametersType.WEIGHT -> kgToLb(metricValue)
        ParametersType.SHOE_SIZE -> euShoeToUs(metricValue)
        else -> metricValue
    }

    fun toMetricValue(type: ParametersType, displayValue: Int): Int = when (type) {
        ParametersType.HEIGHT,
        ParametersType.WAIST,
        ParametersType.HIPS,
        ParametersType.BREAST_SIZE -> inchesToCm(displayValue)
        ParametersType.WEIGHT -> lbToKg(displayValue)
        ParametersType.SHOE_SIZE -> usShoeToEu(displayValue)
        else -> displayValue
    }

    fun convertRangeForDisplay(
        type: ParametersType,
        metricRange: String,
        measuringSystem: String = DivoSettings.measuringSystem,
    ): String {
        if (metricRange.isBlank() || !isImperial(measuringSystem) || !type.hasMeasurableUnits()) {
            return metricRange
        }
        return convertRange(type, metricRange, ::toDisplayValue)
    }

    fun convertRangeToMetric(
        type: ParametersType,
        displayRange: String,
        measuringSystem: String = DivoSettings.measuringSystem,
    ): String {
        if (displayRange.isBlank() || !isImperial(measuringSystem) || !type.hasMeasurableUnits()) {
            return displayRange
        }
        return convertRange(type, displayRange, ::toMetricValue)
    }

    fun convertBetweenSystems(
        type: ParametersType,
        value: String,
        from: String,
        to: String,
    ): String {
        if (value.isBlank() || from == to || !type.hasMeasurableUnits()) return value
        return when {
            from == DivoSettings.SYSTEM_METRIC && to == DivoSettings.SYSTEM_IMPERIAL ->
                convertRange(type, value, ::toDisplayValue)
            from == DivoSettings.SYSTEM_IMPERIAL && to == DivoSettings.SYSTEM_METRIC ->
                convertRange(type, value, ::toMetricValue)
            else -> value
        }
    }

    fun resolveStoredSystem(stored: String?): String =
        stored?.takeIf { it.isNotBlank() } ?: DivoSettings.SYSTEM_METRIC

    fun formatStoredValue(
        type: ParametersType,
        value: String,
        storedSystem: String?,
    ): String = convertBetweenSystems(
        type = type,
        value = value,
        from = resolveStoredSystem(storedSystem),
        to = DivoSettings.measuringSystem,
    )

    fun formatStoredNumber(
        type: ParametersType,
        value: Number?,
        storedSystem: String?,
        emptyPlaceholder: String = "",
    ): String {
        if (value == null || value.toFloat() == 0f) return emptyPlaceholder
        val raw = value.toFloat().let { floatValue ->
            if (floatValue == floatValue.toLong().toFloat()) floatValue.toLong().toString() else floatValue.toString()
        }
        return formatStoredValue(type, raw, storedSystem)
    }

    fun formatStoredRange(
        type: ParametersType,
        from: Int?,
        to: Int?,
        storedSystem: String?,
    ): String {
        if (from == null && to == null) return ""
        val fromVal = from ?: to ?: return ""
        val toVal = to ?: fromVal
        val raw = if (from != null && to != null && from != to) "$fromVal-$toVal" else fromVal.toString()
        return formatStoredValue(type, raw, storedSystem)
    }

    private fun convertRange(
        type: ParametersType,
        range: String,
        transform: (ParametersType, Int) -> Int,
    ): String {
        if ("-" in range) {
            val parts = range.split("-")
            val from = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return range
            val to = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: return range
            return "${transform(type, from)}-${transform(type, to)}"
        }
        val single = range.trim().toDoubleOrNull()?.roundToInt() ?: range.trim().toIntOrNull()
        return single?.let { transform(type, it).toString() } ?: range
    }
}

private fun IntRange.mapRange(transform: (Int) -> Int): IntRange =
    transform(first)..transform(last)

fun ParametersType.hasMeasurableUnits(): Boolean = when (this) {
    ParametersType.HEIGHT,
    ParametersType.WEIGHT,
    ParametersType.WAIST,
    ParametersType.HIPS,
    ParametersType.SHOE_SIZE,
    ParametersType.BREAST_SIZE -> true
    else -> false
}

fun ParametersType.labelRes(measuringSystem: String = DivoSettings.measuringSystem): Int = when (this) {
    ParametersType.HEIGHT -> if (MeasuringUnits.isImperial(measuringSystem)) {
        org.telegram.messenger.R.string.LabelHeightImperial
    } else {
        titleRes
    }
    ParametersType.WEIGHT -> if (MeasuringUnits.isImperial(measuringSystem)) {
        org.telegram.messenger.R.string.LabelWeightImperial
    } else {
        titleRes
    }
    ParametersType.WAIST -> if (MeasuringUnits.isImperial(measuringSystem)) {
        org.telegram.messenger.R.string.LabelWaistImperial
    } else {
        titleRes
    }
    ParametersType.HIPS -> if (MeasuringUnits.isImperial(measuringSystem)) {
        org.telegram.messenger.R.string.LabelHipsImperial
    } else {
        titleRes
    }
    ParametersType.SHOE_SIZE -> if (MeasuringUnits.isImperial(measuringSystem)) {
        org.telegram.messenger.R.string.LabelShoeSizeUS
    } else {
        titleRes
    }
    ParametersType.BREAST_SIZE -> if (MeasuringUnits.isImperial(measuringSystem)) {
        org.telegram.messenger.R.string.LabelBreastSizeImperial
    } else {
        titleRes
    }
    else -> titleRes
}

fun ParametersType.numericFilterRange(measuringSystem: String = DivoSettings.measuringSystem): IntRange? {
    val metricRange = numericFilterRangeMetric() ?: return null
    if (!MeasuringUnits.isImperial(measuringSystem)) return metricRange
    return when (this) {
        ParametersType.HEIGHT,
        ParametersType.WAIST,
        ParametersType.HIPS,
        ParametersType.BREAST_SIZE -> metricRange.mapRange { MeasuringUnits.cmToInches(it) }
        ParametersType.WEIGHT -> metricRange.mapRange { MeasuringUnits.kgToLb(it) }
        ParametersType.SHOE_SIZE -> metricRange.mapRange { MeasuringUnits.euShoeToUs(it) }
        else -> metricRange
    }
}

private fun ParametersType.numericFilterRangeMetric(): IntRange? = when (this) {
    ParametersType.AGE -> 16..45
    ParametersType.HEIGHT -> 120..220
    ParametersType.WEIGHT -> 30..200
    ParametersType.WAIST -> 40..130
    ParametersType.HIPS -> 60..150
    ParametersType.SHOE_SIZE -> 32..50
    ParametersType.BREAST_SIZE -> 65..130
    else -> null
}

fun ParametersType.profilePickerRange(measuringSystem: String = DivoSettings.measuringSystem): IntRange =
    when (this) {
        ParametersType.HEIGHT -> 120..220
        ParametersType.WEIGHT -> 30..200
        ParametersType.WAIST -> 40..130
        ParametersType.HIPS -> 60..150
        ParametersType.SHOE_SIZE -> 32..50
        ParametersType.BREAST_SIZE -> 65..130
        else -> 0..250
    }.let { metricRange ->
        if (!MeasuringUnits.isImperial(measuringSystem) || !hasMeasurableUnits()) {
            metricRange
        } else {
            when (this) {
                ParametersType.HEIGHT,
                ParametersType.WAIST,
                ParametersType.HIPS,
                ParametersType.BREAST_SIZE -> metricRange.mapRange { MeasuringUnits.cmToInches(it) }
                ParametersType.WEIGHT -> metricRange.mapRange { MeasuringUnits.kgToLb(it) }
                ParametersType.SHOE_SIZE -> metricRange.mapRange { MeasuringUnits.euShoeToUs(it) }
                else -> metricRange
            }
        }
    }

fun resolveNumericBlockParamBounds(
    initialValue: String,
    bounds: IntRange,
    type: ParametersType? = null,
    measuringSystem: String = DivoSettings.measuringSystem,
    valuesInMetric: Boolean = false,
): Pair<Int, Int> {
    val displayValue = if (valuesInMetric && type != null) {
        MeasuringUnits.convertRangeForDisplay(type, initialValue, measuringSystem)
    } else {
        initialValue
    }
    if (displayValue.isBlank()) return bounds.first to bounds.last
    if ("-" in displayValue) {
        val parts = displayValue.split("-").map { it.trim().toIntOrNull() ?: return bounds.first to bounds.last }
        return if (parts.size >= 2) {
            parts[0].coerceIn(bounds) to parts[1].coerceIn(bounds)
        } else {
            bounds.first to bounds.last
        }
    }
    val single = displayValue.toDoubleOrNull()?.toInt() ?: displayValue.toIntOrNull()
    return if (single != null) {
        val v = single.coerceIn(bounds)
        v to v
    } else {
        bounds.first to bounds.last
    }
}

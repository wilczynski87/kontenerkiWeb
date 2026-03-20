package com.kontenery.util

import com.kontenery.model.enums.WindowWidthSizeClass
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

fun calculateWidthSizeClass(widthDp: Float): WindowWidthSizeClass =
    when {
        widthDp < 600f -> WindowWidthSizeClass.Compact
        widthDp < 840f -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }

fun String.toDoublePl(): Double? =
    this.trim()
        .replace("\u00A0", " ")   // NBSP → space
        .replace(" ", "")         // usuń separatory tysięcy
        .replace(",", ".")        // zamień przecinek na kropkę
        .toDoubleOrNull()

fun Double.to2Decimals(): String {
    return buildString {
        append(this@to2Decimals)
    }.let {
        if (!it.contains(".")) "$it.00"
        else {
            val parts = it.split(".")
            val frac = parts[1].padEnd(2, '0').take(2)
            parts[0] + "." + frac
        }
    }
}

fun String.isDigitsOnly(): Boolean =
    this.isNotEmpty() && this.all {
        it in '0'..'9' || it == '.' || it == ','
    }

fun formatLocalDate(date: LocalDate): String {
    val day = date.day.toString().padStart(2, '0')
    val month = date.month.number.toString().padStart(2, '0')
    val year = date.year.toString()
    return "$day/$month/$year"
}

fun formatCurrency(value: Double): String {
    val rounded = (value * 100).toInt() / 100.0
    return "$rounded zł"
}

fun getMonthFinanceFromString(date: String): String {
    val month: String = date.substringAfter("-").substringBefore("-")
    val year: String = date.substringBefore("-")
    return "$month.$year"
}

fun unifyMonth(input: String): String {
    val parts = when {
        input.contains("-") -> input.split("-")       // np. 2-2026
        input.contains(".") -> input.split(".")       // np. 2026.12
        else -> throw IllegalArgumentException("Nieobsługiwany format: $input")
    }

    val (year, month) = if (input.contains("-")) {
        parts[1].toInt() to parts[0].toInt()
    } else {
        parts[0].toInt() to parts[1].toInt()
    }

    // ręczne formatowanie w yyyy-MM
    val monthStr = month.toString().padStart(2, '0')
    val yearStr = year.toString().padStart(4, '0')

    return "$monthStr.$yearStr"
}

fun LocalDate.startOfYear(): LocalDate =
    LocalDate(this.year, 1, 1)
fun LocalDate.endOfYear(): LocalDate =
    LocalDate(this.year, 12, 31)
fun startOfYear(date: Int): LocalDate =
    LocalDate(date, 1, 1)
fun endOfYear(date: Int): LocalDate =
    LocalDate(date, 12, 31)
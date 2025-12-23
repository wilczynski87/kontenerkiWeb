package com.kontenery.service

import com.kontenery.model.enums.WindowWidthSizeClass
import kotlinx.datetime.LocalDate

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
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    val year = date.year.toString()
    return "$day/$month/$year"
}
package com.kontenery.util

fun Double.round2(): Double =
    kotlin.math.round(this * 100) / 100

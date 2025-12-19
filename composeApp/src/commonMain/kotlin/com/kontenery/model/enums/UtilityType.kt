package com.kontenery.model.enums

enum class UtilityType(
    val polishName: String,
    val uom: String
) {
    ELECTRICITY("Energia elektryczna", "kWh"),
    WATER("Woda", "m³");
}
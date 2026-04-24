package com.kontenery.model.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class UtilityType(
    val polishName: String,
    val uom: String,
) {
    ELECTRICITY("Energia elektryczna", "kWh"),
    WATER("Woda", "m³");
}

@Composable
fun UtilityIcon(
    type: UtilityType? = null,
    modifier: Modifier = Modifier
) {
    when (type) {
        UtilityType.ELECTRICITY -> Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "prąd",
            modifier
        )

        UtilityType.WATER -> Icon(
            imageVector = Icons.Default.WaterDrop,
            contentDescription = "woda",
            modifier
        )

        null -> Text("-")
    }
}
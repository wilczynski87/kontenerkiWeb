package com.kontenery.model

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

data class ModalData(
    val onDismissRequest: () -> Unit = {},
    val onConfirmation: () -> Unit = {},
    val dialogTitle: String = "",
    val dialogText: String = "",
    val icon: ImageVector? = null ,
    )

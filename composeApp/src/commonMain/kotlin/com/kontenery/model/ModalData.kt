package com.kontenery.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class ModalData(
    val onDismissRequest: () -> Unit = {},
    val onConfirmation: () -> Unit = {},
    val dialogTitle: String = "",
    val dialogText: String = "",
    val icon: ImageVector? = null,
    )

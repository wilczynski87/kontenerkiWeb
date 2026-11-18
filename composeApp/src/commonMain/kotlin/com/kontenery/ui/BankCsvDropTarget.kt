package com.kontenery.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kontenery.FileResult

@Composable
expect fun Modifier.bankCsvDropTarget(
    onFiles: (List<FileResult>) -> Unit,
    onHoverChange: (Boolean) -> Unit,
    onNotice: (String) -> Unit,
): Modifier

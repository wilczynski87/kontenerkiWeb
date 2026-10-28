package com.kontenery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser

actual suspend fun pickFile(): FileResult? = withContext(Dispatchers.Main) {
    val chooser = JFileChooser()
    if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
        return@withContext null
    }
    val file: File = chooser.selectedFile ?: return@withContext null
    FileResult(
        name = file.name,
        bytes = file.readBytes(),
    )
}

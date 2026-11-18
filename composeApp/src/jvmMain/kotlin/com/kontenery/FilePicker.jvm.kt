package com.kontenery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.prefs.Preferences
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

private val csvDirectoryPrefs = Preferences.userRoot().node("com/kontenery/csvimport")

actual val csvFileDropSupported: Boolean = true

actual suspend fun pickFile(): FileResult? = pickCsvFiles().firstOrNull()

actual suspend fun pickCsvFiles(): List<FileResult> {
    val chosen: List<File> = withContext(Dispatchers.Main) {
        val chooser = JFileChooser(lastCsvDirectory()).apply {
            isMultiSelectionEnabled = true
            isAcceptAllFileFilterUsed = true
            fileFilter = FileNameExtensionFilter("Pliki CSV (*.csv)", "csv")
            dialogTitle = "Wybierz wyciągi CSV"
        }
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return@withContext emptyList()
        }
        chooser.currentDirectory?.takeIf { it.isDirectory }?.let(::rememberCsvDirectory)
        val selected = chooser.selectedFiles.toList()
        if (selected.isNotEmpty()) selected else listOfNotNull(chooser.selectedFile)
    }
    if (chosen.isEmpty()) return emptyList()
    return withContext(Dispatchers.IO) {
        chosen.filter { it.isFile }.map { file ->
            FileResult(
                name = file.name,
                bytes = file.readBytes(),
            )
        }
    }
}

private fun lastCsvDirectory(): File? =
    csvDirectoryPrefs.get("lastDirectory", null)
        ?.let(::File)
        ?.takeIf { it.isDirectory }

private fun rememberCsvDirectory(directory: File) {
    csvDirectoryPrefs.put("lastDirectory", directory.absolutePath)
}

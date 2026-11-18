@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.kontenery.ui

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draganddrop.dragData
import com.kontenery.FileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.net.URI

@Composable
actual fun Modifier.bankCsvDropTarget(
    onFiles: (List<FileResult>) -> Unit,
    onHoverChange: (Boolean) -> Unit,
    onNotice: (String) -> Unit,
): Modifier {
    val scope = rememberCoroutineScope()
    val onFilesState = rememberUpdatedState(onFiles)
    val onHoverState = rememberUpdatedState(onHoverChange)
    val onNoticeState = rememberUpdatedState(onNotice)
    val target = remember {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) {
                publishHover(true)
            }

            override fun onExited(event: DragAndDropEvent) {
                publishHover(false)
            }

            override fun onEnded(event: DragAndDropEvent) {
                publishHover(false)
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val uris = event.fileUris()
                if (uris.isEmpty()) return false
                scope.launch {
                    val loaded = withContext(Dispatchers.IO) { loadCsvFiles(uris) }
                    Snapshot.withMutableSnapshot {
                        onHoverState.value(false)
                        if (loaded.files.isNotEmpty()) onFilesState.value(loaded.files)
                        if (loaded.skipped > 0) {
                            onNoticeState.value("Pominięto pliki inne niż CSV (${loaded.skipped}).")
                        }
                    }
                }
                return true
            }

            private fun publishHover(hovering: Boolean) {
                scope.launch {
                    Snapshot.withMutableSnapshot {
                        onHoverState.value(hovering)
                    }
                }
            }
        }
    }
    val acceptsFiles = remember {
        { event: DragAndDropEvent -> event.canAcceptFiles() }
    }
    return this.dragAndDropTarget(
        shouldStartDragAndDrop = acceptsFiles,
        target = target,
    )
}

private data class LoadedCsvFiles(
    val files: List<FileResult>,
    val skipped: Int,
)

private fun loadCsvFiles(uris: List<String>): LoadedCsvFiles {
    val csvFiles = mutableListOf<FileResult>()
    var skipped = 0
    uris.forEach { uri ->
        val file = uri.toLocalFile()
        if (file == null || !file.isFile || !file.name.endsWith(".csv", ignoreCase = true)) {
            skipped++
            return@forEach
        }
        csvFiles += FileResult(name = file.name, bytes = file.readBytes())
    }
    return LoadedCsvFiles(csvFiles, skipped)
}

private fun DragAndDropEvent.canAcceptFiles(): Boolean {
    val flavors = runCatching { awtTransferable.transferDataFlavors }.getOrNull() ?: return false
    if (flavors.any { it.isFileFlavor() }) return true
    val data = runCatching { dragData() }.getOrNull()
    return data is DragData.FilesList || data is DragData.Text
}

private fun DragAndDropEvent.fileUris(): List<String> {
    val transferable = awtTransferable
    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        val files = runCatching {
            @Suppress("UNCHECKED_CAST")
            transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
        }.getOrNull().orEmpty().filterIsInstance<File>()
        if (files.isNotEmpty()) return files.map { it.toURI().toString() }
    }
    val uriList = transferable.transferDataFlavors
        .firstOrNull { it.mimeType.startsWith("text/uri-list", ignoreCase = true) }
        ?.let { flavor -> runCatching { flavor.getReaderForText(transferable).readText() }.getOrNull() }
    if (!uriList.isNullOrBlank()) return parseUriList(uriList)
    return when (val data = runCatching { dragData() }.getOrNull()) {
        is DragData.FilesList -> data.readFiles()
        is DragData.Text -> parseUriList(data.readText())
        else -> emptyList()
    }
}

private fun DataFlavor.isFileFlavor(): Boolean =
    this == DataFlavor.javaFileListFlavor || mimeType.startsWith("text/uri-list", ignoreCase = true)

private fun parseUriList(text: String): List<String> =
    text.lineSequence()
        .map { it.trim().trimEnd('\u0000') }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .toList()

private fun String.toLocalFile(): File? {
    val value = trim().trim('"')
    if (value.startsWith("file:", ignoreCase = true)) {
        return runCatching { File(URI(value)) }.getOrNull()
    }
    if (value.startsWith("/")) return File(value)
    return null
}

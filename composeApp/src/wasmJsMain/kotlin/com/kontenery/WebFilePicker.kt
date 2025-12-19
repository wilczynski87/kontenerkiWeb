package com.kontenery

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.files.Blob
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalWasmJsInterop::class)
actual suspend fun pickFile(): FileResult? = suspendCancellableCoroutine { cont ->
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"

    input.onchange = {
        val file: File? = input.files?.item(0)
        if (file == null) {
            cont.resume(null)
//            return@suspendCancellableCoroutine
        }

        val reader = FileReader()

        reader.onload = {
            val arrayBuffer = reader.result as? org.khronos.webgl.ArrayBuffer
            if (arrayBuffer == null) {
                cont.resumeWithException(RuntimeException("Brak ArrayBuffer"))
//                return@onload
            }

            val u8 = Uint8Array(arrayBuffer!!)
            val bytes = ByteArray(u8.length) { i ->
                u8[i].toByte()
            }

            cont.resume(
                FileResult(
                    name = file?.name ?: "noname",
                    bytes = bytes,
                    mimeType = file?.type?.ifBlank { null }
                )
            )

        }

        reader.onerror = {
            cont.resumeWithException(RuntimeException("Błąd odczytu pliku"))
        }
        reader.readAsArrayBuffer(file as Blob)
    }

    input.click()

    cont.invokeOnCancellation {
        input.remove()
    }
}

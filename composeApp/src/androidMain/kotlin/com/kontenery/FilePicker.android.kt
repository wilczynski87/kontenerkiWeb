package com.kontenery

import android.util.Log

actual val csvFileDropSupported: Boolean = false

actual suspend fun pickFile(): FileResult? {
    Log.d("pickFile", "do not implemented")
    return null
}

actual suspend fun pickCsvFiles(): List<FileResult> = emptyList()
package com.kontenery

import android.util.Log

actual suspend fun pickFile(): FileResult? {
    Log.d("pickFile", "do not implemented")
    return null
}
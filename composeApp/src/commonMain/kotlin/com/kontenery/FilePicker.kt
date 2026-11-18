package com.kontenery

expect suspend fun pickFile(): FileResult?

/** Wybór jednego lub wielu plików CSV. Pusta lista oznacza anulowanie. */
expect suspend fun pickCsvFiles(): List<FileResult>

/** Desktop przyjmuje upuszczenie pliku na obszar importu. */
expect val csvFileDropSupported: Boolean

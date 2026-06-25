package com.kontenery.ui

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.kontenery.FilePickerButton
import com.kontenery.PickFileFunc
import com.kontenery.controller.ApiClientsService
import com.kontenery.data.CSVType
import com.kontenery.data.CsvUploadResult
import com.kontenery.data.MessageRequest
import kotlinx.coroutines.launch

@Composable
fun MyFilePickerButton(
    pickFile: PickFileFunc,
    csvType: CSVType? = null,
    onUploadResult: (CsvUploadResult) -> Unit = {},
) {
    var isUploading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val type = csvType ?: CSVType.PEKAOSABUSSINESS

    if (isUploading) {
        CircularProgressIndicator()
    } else {
        FilePickerButton(
            pickFile = pickFile,
            onPicked = { file ->
                scope.launch {
                    isUploading = true
                    try {
                        val csvText = file.bytes.decodeToString()
                        val result = ApiClientsService.csvPayments.uploadCsv(
                            MessageRequest(csvText),
                            type,
                        )
                        onUploadResult(result)
                    } finally {
                        isUploading = false
                    }
                }
            },
            buttonText = "Wybierz plik dla: ${type.endpoint}",
            fileType = type,
        )
    }
}

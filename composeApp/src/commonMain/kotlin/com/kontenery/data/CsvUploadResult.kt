package com.kontenery.data

import com.kontenery.model.payment.PaymentsRecogniseList

sealed class CsvUploadResult {
    data class Simple(val message: String) : CsvUploadResult()
    data class Recognised(val result: PaymentsRecogniseList) : CsvUploadResult()
    data class Failed(val message: String) : CsvUploadResult()
}

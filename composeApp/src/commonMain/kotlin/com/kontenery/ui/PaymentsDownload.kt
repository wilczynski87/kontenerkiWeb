package com.kontenery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kontenery.data.CSVType
import com.kontenery.data.CsvUploadResult
import com.kontenery.model.PaymentDto
import com.kontenery.model.errors.PaymentError
import com.kontenery.model.payment.PaymentsRecogniseList
import com.kontenery.pickFile
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.util.formatLocalDate
import com.kontenery.util.to2Decimals

@Composable
fun PaymentsDownload(viewModel: ParkingAppViewModel, modifier: Modifier) {
    var uploadResult by remember { mutableStateOf<CsvUploadResult?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Import płatności z pliku CSV",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        MyFilePickerButton(
            csvType = CSVType.NEST,
            pickFile = { pickFile() },
            onUploadResult = { uploadResult = it },
        )
        MyFilePickerButton(
            csvType = CSVType.PEKAOSABUSSINESS,
            pickFile = { pickFile() },
            onUploadResult = { uploadResult = it },
        )
        MyFilePickerButton(
            csvType = CSVType.ALIOR,
            pickFile = { pickFile() },
            onUploadResult = { uploadResult = it },
        )

        uploadResult?.let { result ->
            println("result: $result")
            when (result) {
                is CsvUploadResult.Recognised -> PaymentsRecogniseResult(result.result)
                is CsvUploadResult.Simple -> UploadSimpleResult(result.message)
                is CsvUploadResult.Failed -> UploadErrorResult(result.message)
            }
        }
    }
}

@Composable
private fun UploadSimpleResult(message: String) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Import zakończony: $message",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF2E7D32),
        )
    }
}

@Composable
private fun UploadErrorResult(message: String) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFFFEBEE)),
    ) {
        Text(
            text = "Błąd importu: $message",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFC62828),
        )
    }
}

@Composable
fun PaymentsRecogniseResult(result: PaymentsRecogniseList) {
    val newCount = result.newPayments.orEmpty().size
    val oldCount = result.oldPayments.orEmpty().size
    val unrecognizedCount = result.unrecognizedPayments.orEmpty().size
    val errorCount = result.errors.orEmpty().size

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Podsumowanie importu",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SummaryChip("Nowe", newCount, Color(0xFF2E7D32))
            SummaryChip("Duplikaty", oldCount, Color(0xFFF57C00))
            SummaryChip("Nierozpoznane", unrecognizedCount, Color(0xFF1565C0))
            SummaryChip("Błędy", errorCount, Color(0xFFC62828))
        }

        PaymentDtoSection(
            title = "Nowe płatności",
            payments = result.newPayments.orEmpty(),
            emptyText = "Brak nowych płatności.",
        )
        PaymentDtoSection(
            title = "Duplikaty (już w bazie)",
            payments = result.oldPayments.orEmpty(),
            emptyText = "Brak duplikatów.",
        )
        PaymentDtoSection(
            title = "Nierozpoznane przelewy",
            payments = result.unrecognizedPayments.orEmpty(),
            emptyText = "Brak nierozpoznanych przelewów.",
        )
        PaymentErrorsSection(errors = result.errors.orEmpty())
    }
}

@Composable
private fun SummaryChip(label: String, count: Int, color: Color) {
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = color.copy(alpha = 0.08f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = color)
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun PaymentDtoSection(
    title: String,
    payments: List<PaymentDto>,
    emptyText: String,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "$title (${payments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (payments.isEmpty()) {
                Text(text = emptyText, style = MaterialTheme.typography.bodyMedium)
            } else {
                payments.forEachIndexed { index, payment ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.LightGray,
                        )
                    }
                    PaymentDtoListItem(payment)
                }
            }
        }
    }
}

@Composable
private fun PaymentDtoListItem(payment: PaymentDto) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        PaymentDtoField("Data", payment.date?.let(::formatLocalDate))
        PaymentDtoField("Kwota", payment.amount?.let { "${it.to2Decimals()} zł" })
        PaymentDtoField("Tytuł", payment.title)
        PaymentDtoField("Klient (ID)", payment.fromClientId?.toString())
        PaymentDtoField("Metoda", payment.method)
        PaymentDtoField("Konto odbiorcy", payment.toAccount)
        PaymentDtoField("Konto nadawcy", payment.fromAccount)
        PaymentDtoField("Nr referencyjny", payment.referenceNumber)
        PaymentDtoField("Za faktury", payment.forInvoices?.joinToString(", "))
        PaymentDtoField("ID płatności", payment.paymentId)
    }
}

@Composable
private fun PaymentDtoField(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(130.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PaymentErrorsSection(errors: List<PaymentError>) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (errors.isNotEmpty()) Color(0xFFFFEBEE) else Color.Unspecified,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Błędy walidacji (${errors.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (errors.isNotEmpty()) Color(0xFFC62828) else Color.Unspecified,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (errors.isEmpty()) {
                Text(text = "Brak błędów.", style = MaterialTheme.typography.bodyMedium)
            } else {
                errors.forEachIndexed { index, error ->
                    if (index > 0) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    PaymentErrorRow(error)
                }
            }
        }
    }
}

@Composable
private fun PaymentErrorRow(error: PaymentError) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = listOfNotNull(error.title, error.message).joinToString(": "),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFC62828),
        )
        error.payment?.let { payment ->
            PaymentDtoField("Data", formatLocalDate(payment.date))
            PaymentDtoField("Kwota", "${payment.amount.to2Decimals()} zł")
            PaymentDtoField("Tytuł", payment.title)
            PaymentDtoField("Klient", payment.fromClient?.getName())
            PaymentDtoField("Konto nadawcy", payment.fromAccount)
            PaymentDtoField("Nr referencyjny", payment.referenceNumber)
        }
    }
}

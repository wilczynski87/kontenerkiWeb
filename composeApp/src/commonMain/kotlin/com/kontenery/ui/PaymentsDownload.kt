package com.kontenery.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kontenery.FileResult
import com.kontenery.controller.ApiClientsService
import com.kontenery.csv.BankCsvDetector
import com.kontenery.csvFileDropSupported
import com.kontenery.data.CSVType
import com.kontenery.data.CsvUploadResult
import com.kontenery.data.MessageRequest
import com.kontenery.model.Client
import com.kontenery.model.ClientBankAccount
import com.kontenery.model.ClientOnList
import com.kontenery.model.PaymentDto
import com.kontenery.model.errors.PaymentError
import com.kontenery.model.payment.PaymentsRecogniseList
import com.kontenery.pickCsvFiles
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.util.formatLocalDate
import com.kontenery.util.to2Decimals
import kotlinx.coroutines.launch

private data class CsvImportItem(
    val id: Long,
    val fileName: String,
    val csvText: String,
    val detected: CSVType?,
    val selected: CSVType? = detected,
    val uploading: Boolean = false,
    val result: CsvUploadResult? = null,
)

@Composable
fun PaymentsDownload(viewModel: ParkingAppViewModel, modifier: Modifier) {
    var imports by remember { mutableStateOf<List<CsvImportItem>>(emptyList()) }
    var nextId by remember { mutableLongStateOf(1L) }
    var notice by remember { mutableStateOf<String?>(null) }
    var dropHover by remember { mutableStateOf(false) }
    var ignoredAccounts by remember { mutableStateOf<List<ClientBankAccount>>(emptyList()) }
    var ignoredRefresh by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(ignoredRefresh) {
        ignoredAccounts = try {
            ApiClientsService.bankAccounts.listIgnored()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun updateItem(id: Long, transform: (CsvImportItem) -> CsvImportItem) {
        imports = imports.map { item -> if (item.id == id) transform(item) else item }
    }

    fun upload(id: Long, type: CSVType) {
        val text = imports.find { it.id == id }?.csvText ?: return
        updateItem(id) { it.copy(selected = type, uploading = true, result = null) }
        scope.launch {
            val result = ApiClientsService.csvPayments.uploadCsv(MessageRequest(text), type)
            updateItem(id) { it.copy(uploading = false, selected = type, result = result) }
        }
    }

    fun addFiles(files: List<FileResult>) {
        if (files.isEmpty()) return
        notice = null
        files.forEach { file ->
            val text = file.bytes.decodeToString()
            val detected = BankCsvDetector.detect(text)
            val id = nextId++
            imports = imports + CsvImportItem(
                id = id,
                fileName = file.name,
                csvText = text,
                detected = detected,
            )
            if (detected != null) upload(id, detected)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .bankCsvDropTarget(
                onFiles = ::addFiles,
                onHoverChange = { dropHover = it },
                onNotice = { notice = it },
            )
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Import płatności z pliku CSV",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Nest, Alior i Pekao SA są rozpoznawane po nagłówku pliku.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        CsvImportDropZone(
            dropHover = dropHover,
            notice = notice,
            onPick = {
                scope.launch {
                    addFiles(pickCsvFiles())
                }
            },
        )

        IgnoredAccountsCard(
            accounts = ignoredAccounts,
            viewModel = viewModel,
            onAccountsChanged = { ignoredRefresh++ },
        )

        imports.forEach { item ->
            CsvImportItemCard(
                item = item,
                viewModel = viewModel,
                onSelectBank = { type -> upload(item.id, type) },
                onAccountsChanged = { ignoredRefresh++ },
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CsvImportDropZone(
    dropHover: Boolean,
    notice: String?,
    onPick: () -> Unit,
) {
    val border = if (dropHover) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        CardDefaults.outlinedCardBorder()
    }
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = border,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (dropHover) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                Color.Unspecified
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (csvFileDropSupported) {
                    "Upuść tutaj plik CSV albo wybierz go z dysku."
                } else {
                    "Wybierz plik CSV z dysku."
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onPick) {
                Text("Wybierz pliki CSV")
            }
            notice?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1565C0),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CsvImportItemCard(
    item: CsvImportItem,
    viewModel: ParkingAppViewModel,
    onSelectBank: (CSVType) -> Unit,
    onAccountsChanged: () -> Unit,
) {
    val bank = item.selected ?: item.detected
    val needsChoice = bank == null || item.result is CsvUploadResult.Failed
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = importStatusLabel(item),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (item.uploading) {
                    CircularProgressIndicator()
                }
            }

            if (needsChoice && !item.uploading) {
                Text(
                    text = if (item.result is CsvUploadResult.Failed) {
                        "Import się nie powiódł. Możesz wysłać plik ponownie."
                    } else {
                        "Wskaż bank, żeby wysłać plik."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CSVType.entries.forEach { type ->
                        Button(onClick = { onSelectBank(type) }) {
                            Text(type.label)
                        }
                    }
                }
            }

            item.result?.let { result ->
                when (result) {
                    is CsvUploadResult.Simple -> UploadSimpleResult(result.message)
                    is CsvUploadResult.Failed -> UploadErrorResult(result.message)
                    is CsvUploadResult.Recognised -> Unit
                }
            }
        }
    }
    if (item.result is CsvUploadResult.Recognised) {
        PaymentsRecogniseResult(item.result.result, viewModel, onAccountsChanged)
    }
}

private fun importStatusLabel(item: CsvImportItem): String {
    val bank = item.selected ?: item.detected
    val prefix = when {
        item.uploading -> "Wysyłanie"
        bank != null && bank == item.detected -> "Rozpoznano"
        bank != null -> "Wybrano"
        else -> return "Nie rozpoznano formatu — ${item.fileName}"
    }
    return "$prefix: ${bank?.label} — ${item.fileName}"
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
fun PaymentsRecogniseResult(
    result: PaymentsRecogniseList,
    viewModel: ParkingAppViewModel,
    onAccountsChanged: () -> Unit = {},
) {
    val newCount = result.newPayments.orEmpty().size
    val oldCount = result.oldPayments.orEmpty().size
    val unrecognizedCount = result.unrecognizedPayments.orEmpty().size
    val ignoredCount = result.ignoredPayments.orEmpty().size
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
            SummaryChip("Zignorowane", ignoredCount, Color(0xFF455A64))
            SummaryChip("Błędy", errorCount, Color(0xFFC62828))
        }

        PaymentDtoSection(
            title = "Nierozpoznane przelewy",
            payments = result.unrecognizedPayments.orEmpty(),
            emptyText = "Brak nierozpoznanych przelewów.",
            viewModel = viewModel,
            containerColor = Color(0xFFE3F2FD),
            titleColor = Color(0xFF1565C0),
            showIgnore = true,
            onAccountsChanged = onAccountsChanged,
        )
        PaymentDtoSection(
            title = "Zignorowane przelewy",
            payments = result.ignoredPayments.orEmpty(),
            emptyText = "Brak zignorowanych przelewów.",
            viewModel = viewModel,
            containerColor = Color(0xFFECEFF1),
            titleColor = Color(0xFF455A64),
            showUnignore = true,
            onAccountsChanged = onAccountsChanged,
        )
        PaymentDtoSection(
            title = "Nowe płatności",
            payments = result.newPayments.orEmpty(),
            emptyText = "Brak nowych płatności.",
            containerColor = Color(0xFFE8F5E9),
            titleColor = Color(0xFF2E7D32),
        )
        PaymentDtoSection(
            title = "Duplikaty (już w bazie)",
            payments = result.oldPayments.orEmpty(),
            emptyText = "Brak duplikatów.",
            containerColor = Color(0xFFFFF3E0),
            titleColor = Color(0xFFF57C00),
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
    viewModel: ParkingAppViewModel? = null,
    containerColor: Color = Color.Unspecified,
    titleColor: Color = Color.Unspecified,
    showIgnore: Boolean = false,
    showUnignore: Boolean = false,
    onAccountsChanged: () -> Unit = {},
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "$title (${payments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
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
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isWide = maxWidth >= 600.dp
                        if (isWide && viewModel != null && !payment.fromAccount.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    PaymentDtoListItem(payment)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    AssignAccountToClientRow(
                                        accountNumber = payment.fromAccount,
                                        viewModel = viewModel,
                                        showIgnore = showIgnore,
                                        showUnignore = showUnignore,
                                        onAccountsChanged = onAccountsChanged,
                                    )
                                }
                            }
                        } else {
                            PaymentDtoListItem(payment)
                            if (viewModel != null && !payment.fromAccount.isNullOrBlank()) {
                                AssignAccountToClientRow(
                                    accountNumber = payment.fromAccount,
                                    viewModel = viewModel,
                                    showIgnore = showIgnore,
                                    showUnignore = showUnignore,
                                    onAccountsChanged = onAccountsChanged,
                                )
                            }
                        }
                    }
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
        PaymentDtoField("Nadawca", payment.senderName)
        PaymentDtoField("Tytuł", payment.title)
        PaymentDtoField("Klient (ID)", payment.fromClientId?.toString())
        PaymentDtoField("Metoda", payment.method)
        PaymentDtoField("Konto odbiorcy", payment.toAccount?.formatIban())
        PaymentDtoField("Konto nadawcy", payment.fromAccount?.formatIban())
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
            PaymentDtoField("Konto nadawcy", payment.fromAccount?.formatIban())
            PaymentDtoField("Nr referencyjny", payment.referenceNumber)
        }
    }
}

private fun String.formatIban(): String {
    val digits = this.filter { it.isDigit() }
    val prefix = if (this.length >= 2 && this[0].isLetter() && this[1].isLetter())
        this.substring(0, 2).uppercase() else "PL"
    return (prefix + digits).chunked(4).joinToString(" ").trim()
}

@Composable
private fun AssignAccountToClientRow(
    accountNumber: String,
    viewModel: ParkingAppViewModel,
    accountId: Long? = null,
    showIgnore: Boolean = false,
    showUnignore: Boolean = false,
    onAccountsChanged: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val clients: List<ClientOnList> = viewModel.state.collectAsState().value.clients
    if (clients.isEmpty()) viewModel.getClientsList(0, 1000)

    var expanded by remember { mutableStateOf(false) }
    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saveResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HorizontalDivider(color = Color(0xFF1565C0).copy(alpha = 0.3f))

        if ((showIgnore || showUnignore) && saveResult == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (showIgnore) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                try {
                                    ApiClientsService.bankAccounts.ignoreAccount(accountNumber)
                                    saveResult = "Konto oznaczone do ignorowania"
                                    onAccountsChanged()
                                } catch (e: Exception) {
                                    saveResult = "Błąd zapisu: ${e.message}"
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving && saveResult == null,
                    ) {
                        Text("Ignoruj konto")
                    }
                }
                if (showUnignore) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                try {
                                    ApiClientsService.bankAccounts.deleteClientBankAccount("0", accountNumber)
                                    saveResult = "Cofnięto ignorowanie konta"
                                    onAccountsChanged()
                                } catch (e: Exception) {
                                    saveResult = "Błąd zapisu: ${e.message}"
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving && saveResult == null,
                    ) {
                        Text("Cofnij ignorowanie")
                    }
                }
            }
        }

        if (saveResult != null) {
            Text(
                text = saveResult!!,
                style = MaterialTheme.typography.bodySmall,
                color = if (saveResult!!.startsWith("Błąd")) Color(0xFFC62828) else Color(0xFF2E7D32),
            )
        } else {
            ClientsListDropdown(
                client = selectedClient,
                clients = clients,
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                onSelect = { clientId ->
                    expanded = false
                    selectedClient = Client(id = clientId)
                },
            )

            if (selectedClient != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { selectedClient = null }) {
                        Text("Anuluj")
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                try {
                                    val clientId = selectedClient!!.id!!
                                    val assignment = ClientBankAccount(
                                        bankAccount = accountNumber,
                                        client = Client(id = clientId),
                                    )
                                    if (accountId != null) {
                                        ApiClientsService.bankAccounts.assignBankAccount(accountId, assignment)
                                    } else {
                                        ApiClientsService.bankAccounts.saveClientBankAccount(assignment)
                                    }
                                    onAccountsChanged()
                                    val clientName = clients.find { it.id == clientId }?.name ?: "klienta"
                                    saveResult = "Zapisano konto dla: $clientName"
                                } catch (e: Exception) {
                                    saveResult = "Błąd zapisu: ${e.message}"
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving,
                    ) {
                        Text(if (isSaving) "Zapisuję..." else "Przypisz konto do klienta")
                    }
                }
            }
        }
    }
}

@Composable
private fun IgnoredAccountsCard(
    accounts: List<ClientBankAccount>,
    viewModel: ParkingAppViewModel,
    onAccountsChanged: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFECEFF1)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Ignorowane konta (${accounts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF455A64),
            )
            Text(
                text = "Przelewy z tych kont nie są zapisywane przy imporcie. Można cofnąć ignorowanie albo przypisać konto do klienta.",
                style = MaterialTheme.typography.bodySmall,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            if (accounts.isEmpty()) {
                Text(text = "Brak kont oznaczonych do ignorowania.", style = MaterialTheme.typography.bodyMedium)
            } else {
                accounts.forEachIndexed { index, account ->
                    val number = account.bankAccount
                    if (number.isNullOrBlank()) return@forEachIndexed
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.LightGray,
                        )
                    }
                    Text(
                        text = number.formatIban(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    AssignAccountToClientRow(
                        accountNumber = number,
                        viewModel = viewModel,
                        accountId = account.id,
                        showUnignore = true,
                        onAccountsChanged = onAccountsChanged,
                    )
                }
            }
        }
    }
}

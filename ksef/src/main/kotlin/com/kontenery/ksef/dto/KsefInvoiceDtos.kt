package com.kontenery.ksef.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class InvoiceQuerySubjectType {
    @SerialName("Subject1") Subject1,
    @SerialName("Subject2") Subject2,
    @SerialName("Subject3") Subject3,
    @SerialName("SubjectAuthorized") SubjectAuthorized,
}

@Serializable
enum class InvoiceQueryDateType {
    @SerialName("Issue") Issue,
    @SerialName("Invoicing") Invoicing,
    @SerialName("PermanentStorage") PermanentStorage,
}

@Serializable
data class InvoiceQueryDateRange(
    val dateType: InvoiceQueryDateType,
    val from: String,
    val to: String? = null,
)

@Serializable
data class InvoiceQueryFilters(
    val subjectType: InvoiceQuerySubjectType,
    val dateRange: InvoiceQueryDateRange,
)

@Serializable
data class InvoiceMetadataSeller(
    val nip: String,
    val name: String? = null,
)

@Serializable
data class InvoiceMetadataBuyerIdentifier(
    val type: String,
    val value: String? = null,
)

@Serializable
data class InvoiceMetadataBuyer(
    val identifier: InvoiceMetadataBuyerIdentifier,
    val name: String? = null,
)

@Serializable
data class InvoiceFormCode(
    val systemCode: String,
    val schemaVersion: String,
    val value: String,
)

@Serializable
data class InvoiceMetadata(
    val ksefNumber: String,
    val invoiceNumber: String,
    val issueDate: String,
    val invoicingDate: String,
    val acquisitionDate: String,
    val permanentStorageDate: String,
    val seller: InvoiceMetadataSeller,
    val buyer: InvoiceMetadataBuyer,
    val netAmount: Double,
    val grossAmount: Double,
    val vatAmount: Double,
    val currency: String,
    val invoicingMode: String,
    val invoiceType: String,
    val formCode: InvoiceFormCode,
    val isSelfInvoicing: Boolean,
    val hasAttachment: Boolean,
    val invoiceHash: String,
)

@Serializable
data class QueryInvoicesMetadataResponse(
    val hasMore: Boolean,
    val isTruncated: Boolean,
    val permanentStorageHwmDate: String? = null,
    val invoices: List<InvoiceMetadata> = emptyList(),
)

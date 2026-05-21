package com.kontenery.ksef.model

import com.kontenery.ksef.dto.InvoiceQueryDateType
import com.kontenery.ksef.dto.InvoiceQuerySubjectType
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * Parametry zapytania o metadane faktur (POST /invoices/query/metadata).
 */
data class InvoiceListRequest(
    val subjectType: InvoiceQuerySubjectType = InvoiceQuerySubjectType.Subject1,
    val dateType: InvoiceQueryDateType = InvoiceQueryDateType.PermanentStorage,
    val dateFrom: String,
    val dateTo: String? = null,
    val pageOffset: Int = 0,
    val pageSize: Int = 50,
    val sortOrder: String = "Asc",
) {
    init {
        require(pageSize in 10..250) { "pageSize musi być w zakresie 10–250" }
        require(pageOffset >= 0) { "pageOffset nie może być ujemny" }
    }

    companion object {
        fun lastMonths(
            months: Int = 3,
            subjectType: InvoiceQuerySubjectType = InvoiceQuerySubjectType.Subject1,
            dateType: InvoiceQueryDateType = InvoiceQueryDateType.PermanentStorage,
            timeZone: TimeZone = TimeZone.of("Europe/Warsaw"),
        ): InvoiceListRequest {
            val today = Clock.System.now().toLocalDateTime(timeZone).date
            val from = today.minus(months, DateTimeUnit.MONTH)
            return InvoiceListRequest(
                subjectType = subjectType,
                dateType = dateType,
                dateFrom = from.atStartOfDayIn(timeZone).toString(),
                dateTo = today.atStartOfDayIn(timeZone).toString(),
            )
        }

        fun forDateRange(
            from: LocalDate,
            to: LocalDate,
            timeZone: TimeZone = TimeZone.of("Europe/Warsaw"),
            subjectType: InvoiceQuerySubjectType = InvoiceQuerySubjectType.Subject1,
            dateType: InvoiceQueryDateType = InvoiceQueryDateType.PermanentStorage,
        ): InvoiceListRequest = InvoiceListRequest(
            subjectType = subjectType,
            dateType = dateType,
            dateFrom = from.atStartOfDayIn(timeZone).toString(),
            dateTo = to.atStartOfDayIn(timeZone).toString(),
        )
    }
}

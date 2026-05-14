package com.kontenery.service

import com.kontenery.model.Client
import com.kontenery.model.ClientCompanyData
import com.kontenery.model.enums.InvoiceType
import com.kontenery.model.invoice.Invoice
import com.kontenery.model.invoice.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class InvoiceDraftControllerTest {
    @Test
    fun createNewInvoiceStoresEmptyDraftWithRequestedType() {
        val state = MutableStateFlow(ParkingAppState())
        val controller = InvoiceDraftController(state)

        controller.createNewInvoice(InvoiceType.OTHER)

        val invoice = assertNotNull(state.value.invoice)
        assertEquals(InvoiceType.OTHER.name, invoice.type)
        assertEquals(emptyList(), invoice.products)
    }

    @Test
    fun calculatePositionUpdatesNetVatAndGrossValues() {
        val state = MutableStateFlow(ParkingAppState())
        val controller = InvoiceDraftController(state)

        controller.calculatePosition(
            Position(
                productName = "Kontener",
                unitPrice = "10",
                quantity = "2",
                vatRate = "23"
            )
        )

        val position = assertNotNull(state.value.position)
        assertEquals("20.00", position.price)
        assertEquals("4.60", position.vatAmount)
        assertEquals("24.60", position.priceWithVat)
    }

    @Test
    fun addProductToInvoiceAppendsPositionAndUpdatesSums() {
        val state = MutableStateFlow(
            ParkingAppState(
                invoice = Invoice(),
                position = Position(
                    productName = "Kontener",
                    price = "20.00",
                    vatAmount = "4.60",
                    priceWithVat = "24.60"
                )
            )
        )
        val controller = InvoiceDraftController(state)

        controller.addProductToInvoice()

        val invoice = assertNotNull(state.value.invoice)
        assertEquals(1, invoice.products.size)
        assertEquals("20.0", invoice.priceSum)
        assertEquals("4.6", invoice.vatAmountSum)
        assertEquals("24.6", invoice.priceWithVatSum)
    }

    @Test
    fun sellerForInvoiceUpdateUsesVatInvoiceForCompanyClient() {
        val state = MutableStateFlow(
            ParkingAppState(
                client = Client(clientCompany = ClientCompanyData(needInvoice = true)),
                invoice = Invoice()
            )
        )
        val controller = InvoiceDraftController(state)

        controller.sellerForInvoiceUpdate()

        val invoice = assertNotNull(state.value.invoice)
        assertEquals("Faktura VAT", invoice.invoiceTitle)
        assertNotNull(invoice.seller)
    }
}

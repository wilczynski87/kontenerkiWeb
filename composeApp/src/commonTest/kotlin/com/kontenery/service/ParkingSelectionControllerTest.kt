package com.kontenery.service

import com.kontenery.model.ClientOnList
import com.kontenery.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParkingSelectionControllerTest {
    @Test
    fun toggleClientNavRowSelectsAndClearsSameRow() {
        val state = MutableStateFlow(ParkingAppState())
        val controller = ParkingSelectionController(state)

        controller.toggleClientNavRow(7)
        assertEquals(7, state.value.clientNavRow)

        controller.toggleClientNavRow(7)
        assertNull(state.value.clientNavRow)
    }

    @Test
    fun productDraftCanBeSetUpdatedAndCleared() {
        val state = MutableStateFlow(ParkingAppState())
        val controller = ParkingSelectionController(state)
        val firstProduct = Product.Container(id = 1, name = "A")
        val updatedProduct = Product.Container(id = 1, name = "B")

        controller.newProduct(firstProduct)
        assertEquals(firstProduct, state.value.newProduct)

        controller.updateProduct(updatedProduct)
        assertEquals(updatedProduct, state.value.newProduct)

        controller.clearProduct()
        assertNull(state.value.newProduct)
    }

    @Test
    fun clientLookupReadsFromCurrentState() {
        val state = MutableStateFlow(
            ParkingAppState(
                clients = listOf(
                    ClientOnList(
                        id = 3,
                        name = "Jan Kowalski",
                        paymentsOverdue = 12.5,
                        contracts = emptyList(),
                        active = true,
                        invoice = false,
                        lastBill = null
                    )
                )
            )
        )
        val controller = ParkingSelectionController(state)

        assertEquals("Jan Kowalski", controller.getClientNameById(3))
        assertEquals(12.5, controller.getClientOverdue(3))
        assertNull(controller.getClientNameById(99))
    }

    @Test
    fun dateAndFinanceYearAreStoredInState() {
        val state = MutableStateFlow(ParkingAppState())
        val controller = ParkingSelectionController(state)
        val date = LocalDate(2026, 5, 14)

        controller.updateForDate(date)
        controller.changeFinanceYear(2026)

        assertEquals(date, state.value.forDate)
        assertEquals(2026, state.value.financeYear)
    }
}

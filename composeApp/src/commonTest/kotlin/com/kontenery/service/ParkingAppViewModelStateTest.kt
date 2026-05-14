package com.kontenery.service

import com.kontenery.model.Client
import com.kontenery.model.ClientEvent
import com.kontenery.model.ClientPersonalData
import com.kontenery.model.enums.CurrentScreen
import com.kontenery.model.enums.InvoiceType
import com.kontenery.model.invoice.Position
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ParkingAppViewModelStateTest {
    @Test
    fun modalMethodsUpdateStateWithoutBackendInitialization() {
        val viewModel = testViewModel()

        viewModel.showConfirmModal(
            dialogTitle = "Potwierdź",
            dialogText = "Czy zapisać?",
            onConfirmation = {}
        )

        assertEquals("Potwierdź", viewModel.state.value.confirmModal?.dialogTitle)

        viewModel.closeConfirmationModal()

        assertEquals(null, viewModel.state.value.confirmModal)
    }

    @Test
    fun navigationMethodsDelegateToNavigationController() {
        val viewModel = testViewModel()

        viewModel.setGoBack(
            targetScreen = CurrentScreen.CLIENTS_LIST,
            triggerScreen = CurrentScreen.ADD_PRODUCT
        )
        viewModel.checkGoBack()
        assertFalse(viewModel.state.value.canGoBack)

        viewModel.toAddProduct()
        viewModel.checkGoBack()
        assertTrue(viewModel.state.value.canGoBack)

        viewModel.goBack()
        assertEquals(CurrentScreen.CLIENTS_LIST, viewModel.state.value.currentScreen)
        assertFalse(viewModel.state.value.canGoBack)
    }

    @Test
    fun invoiceDraftMethodsUpdateInvoiceAndPositionState() {
        val viewModel = testViewModel()

        viewModel.createNewInvoice(InvoiceType.OTHER)
        viewModel.calculatePosition(
            Position(
                productName = "Plac",
                unitPrice = "100",
                quantity = "2",
                vatRate = "8"
            )
        )
        viewModel.addProductToInvoice()

        val invoice = assertNotNull(viewModel.state.value.invoice)
        val position = assertNotNull(viewModel.state.value.position)
        assertEquals("200.00", position.price)
        assertEquals(InvoiceType.OTHER.name, invoice.type)
        assertEquals(1, invoice.products.size)
    }

    @Test
    fun clientEventsUpdateClientStateThroughPublicViewModelMethod() {
        val viewModel = testViewModel(
            initialState = ParkingAppState(client = Client(clientPrivate = ClientPersonalData(), isActive = true))
        )

        viewModel.onClientEvent(ClientEvent.Personal.FirstNameChanged("Anna"))
        viewModel.onClientEvent(ClientEvent.ToggleActive)

        val client = assertNotNull(viewModel.state.value.client)
        assertEquals("Anna", client.clientPrivate?.firstName)
        assertFalse(client.isActive ?: true)
    }

    private fun testViewModel(
        initialState: ParkingAppState = ParkingAppState()
    ): ParkingAppViewModel {
        return ParkingAppViewModel(
            coroutineScope = CoroutineScope(EmptyCoroutineContext),
            autoInitialize = false,
            initialState = initialState,
        )
    }
}

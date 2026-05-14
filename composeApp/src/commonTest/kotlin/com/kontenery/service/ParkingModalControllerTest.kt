package com.kontenery.service

import com.kontenery.model.ModalData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ParkingModalControllerTest {
    @Test
    fun showConfirmModalStoresDialogDataAndCloseClearsIt() {
        val state = MutableStateFlow(ParkingAppState())
        val controller = ParkingModalController(state)

        controller.showConfirmModal(
            dialogTitle = "Tytuł",
            dialogText = "Treść",
            onConfirmation = {}
        )

        val modal = assertNotNull(state.value.confirmModal)
        assertEquals("Tytuł", modal.dialogTitle)
        assertEquals("Treść", modal.dialogText)

        controller.closeConfirmationModal()

        assertNull(state.value.confirmModal)
    }

    @Test
    fun createConfirmationModalStoresProvidedModal() {
        val state = MutableStateFlow(ParkingAppState())
        val controller = ParkingModalController(state)
        val modal = ModalData(dialogTitle = "Gotowe", dialogText = "Zapisano")

        controller.createConfirmationModal(modal)

        assertEquals(modal, state.value.confirmModal)
    }
}

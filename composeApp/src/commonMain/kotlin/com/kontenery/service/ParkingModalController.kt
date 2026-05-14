package com.kontenery.service

import com.kontenery.model.ModalData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ParkingModalController(
    private val state: MutableStateFlow<ParkingAppState>
) {
    fun closeConfirmationModal() {
        state.update { currentState ->
            currentState.copy(confirmModal = null)
        }
    }

    fun showConfirmModal(
        dialogTitle: String,
        dialogText: String,
        onConfirmation: () -> Unit,
    ) {
        state.update { currentState ->
            currentState.copy(
                confirmModal = ModalData(
                    dialogTitle = dialogTitle,
                    dialogText = dialogText,
                    onConfirmation = onConfirmation,
                )
            )
        }
    }

    fun showErrorModal(
        dialogTitle: String,
        dialogText: String,
        onDismissRequest: () -> Unit,
        onConfirmation: () -> Unit,
    ) {
        state.update { currentState ->
            currentState.copy(
                confirmModal = ModalData(
                    dialogTitle = dialogTitle,
                    dialogText = dialogText,
                    onConfirmation = onConfirmation,
                    onDismissRequest = onDismissRequest,
                )
            )
        }
    }

    fun createConfirmationModal(modal: ModalData) {
        state.update { currentState ->
            currentState.copy(confirmModal = modal)
        }
    }

    fun closeResponseModal() {
        state.update { currentState ->
            currentState.copy(responseErrors = listOf())
        }
    }
}

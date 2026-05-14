package com.kontenery.service

import com.kontenery.model.enums.CurrentScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ParkingNavigationController(
    private val state: MutableStateFlow<ParkingAppState>
) {
    fun setGoBack(targetScreen: CurrentScreen, triggerScreen: CurrentScreen) {
        state.update { currentState ->
            currentState.copy(
                canGoBack = true,
                triggerScreen = triggerScreen,
                targetScreen = targetScreen
            )
        }
    }

    fun goBack() {
        state.update { currentState ->
            currentState.copy(
                currentScreen = currentState.targetScreen ?: CurrentScreen.CLIENTS_LIST,
                canGoBack = false
            )
        }
    }

    fun checkGoBack() {
        state.update { currentState ->
            currentState.copy(
                canGoBack = currentState.triggerScreen != null &&
                    currentState.currentScreen == currentState.triggerScreen
            )
        }
    }
}

package com.kontenery.service

import com.kontenery.model.enums.CurrentScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParkingNavigationControllerTest {
    @Test
    fun checkGoBackEnablesBackOnlyForTriggerScreen() {
        val state = MutableStateFlow(
            ParkingAppState(
                currentScreen = CurrentScreen.CLIENT_DATA,
                triggerScreen = CurrentScreen.CLIENT_DATA,
                canGoBack = false
            )
        )
        val navigationController = ParkingNavigationController(state)

        navigationController.checkGoBack()

        assertTrue(state.value.canGoBack)

        state.value = state.value.copy(currentScreen = CurrentScreen.CLIENTS_LIST)

        navigationController.checkGoBack()

        assertFalse(state.value.canGoBack)
    }

    @Test
    fun goBackUsesTargetScreenAndDisablesBack() {
        val state = MutableStateFlow(
            ParkingAppState(
                currentScreen = CurrentScreen.ADD_PRODUCT,
                targetScreen = CurrentScreen.PRODUCTS_LIST,
                canGoBack = true
            )
        )
        val navigationController = ParkingNavigationController(state)

        navigationController.goBack()

        assertEquals(CurrentScreen.PRODUCTS_LIST, state.value.currentScreen)
        assertFalse(state.value.canGoBack)
    }
}

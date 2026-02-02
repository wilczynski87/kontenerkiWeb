package com.kontenery.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.parkingandroidview.ui.BankAccountMenu
import com.example.parkingandroidview.ui.ContractList
import com.example.parkingandroidview.ui.PaymentForm
import com.kontenery.model.ModalData
import com.kontenery.model.enums.CurrentScreen
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.service.ParkingAppState
import com.kontenery.service.ParkingAppViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning

@Composable
fun ParkingApp(
    windowSize: WindowWidthSizeClass,
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val state: ParkingAppState = viewModel.state.collectAsState().value
    val confirmModal: ModalData? = state.confirmModal
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val canGoBack: Boolean = state.canGoBack
    val goBack: () -> Unit = { viewModel.goBack() }
    val scope = rememberCoroutineScope()

    Drawer(
        viewModel = viewModel,
        scope = scope,
        drawerState = drawerState,
        modifier = modifier,
    ) {
        Scaffold(
            topBar = {
                TopUpBar(
                    modifier = modifier
                        .statusBarsPadding()
                        .fillMaxWidth(),
                    title = "Menu",
                    canNavigateBack = canGoBack,
                    navigateUp = goBack,
                    drawerClick = { toggleDrawer(scope, drawerState) }
                )
            },
            modifier = modifier
        ) { innerPadding ->

            Box(modifier = modifier.fillMaxSize()) {

                Crossfade(
                    targetState = state.currentScreen, label = "cross fade"
                ) { screen ->
                    viewModel.checkGoBack() // check every time if menu can allow to go back
                    when (screen) {
                        CurrentScreen.CLIENTS_LIST -> {
                            ClientTable(
                                viewModel = viewModel,
                                windowSize = windowSize,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.CLIENT_DATA -> {
                            ClientBox(
                                viewModel = viewModel,
                                windowSize = windowSize,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.CLIENT_FINANCE -> {}
                        CurrentScreen.CLIENT_CONTRACTS -> {
                            ContractList(
//                        windowSize = windowSize,
                                viewModel = viewModel,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.PRODUCTS_LIST -> {
                            ProductListWithFilter(
                                windowSize = windowSize,
                                viewModel = viewModel,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.ADD_PRODUCT -> {
                            AddProductMenu(
                                windowSize = windowSize,
                                viewModel = viewModel,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.PRODUCT_DATA -> {}
                        CurrentScreen.ADD_CONTRACT -> {
                            ContractForm(
                                viewModel = viewModel,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.EDIT_CONTRACT -> {
                            ContractForm(
                                viewModel = viewModel,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.ADD_INVOICE -> {
                            InvoiceForm(
                                viewModel = viewModel,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.PAYMENT_MENU -> {
                            PaymentsMenu(
                                viewModel = viewModel,
                                windowSize = windowSize,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.BANK_ACCOUNT_MENU -> {
                            BankAccountMenu(
                                viewModel = viewModel,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.PAYMENT_FORM -> {
                            PaymentForm(
                                viewModel = viewModel,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.UPLOAD_PAYMENTS -> {
                            PaymentsDownload(
                                viewModel = viewModel,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                        CurrentScreen.FINANCES -> {
                            Finances(
                                viewModel = viewModel,
                                windowSize = windowSize,
                                modifier = modifier.padding(innerPadding)
                            )
                        }

                    }
                }

                // Dialogy warunkowe
                confirmModal?.let { modal ->
                    ConfirmSending(
                        onDismissRequest = { viewModel.closeConfirmationModal() },
                        onConfirmation = {
                            modal.onConfirmation()
                            viewModel.closeConfirmationModal()
                        },
                        dialogTitle = modal.dialogTitle,
                        dialogText = modal.dialogText,
                        icon = Icons.Default.Warning,
                    )
                }

                if (state.responseErrors.isNotEmpty()) {
                    ResponseModal(
                        onDismissRequest = { viewModel.closeResponseModal() },
                        onConfirmation = { viewModel.closeResponseModal() },
                        dialogTitle = "Błąd przy wysyłaniu faktury okresowej",
                        dialogText = state.responseErrors,
                        icon = Icons.Default.Warning,
                    )
                }

            }
        }
    }
}

//
//@Preview(showBackground = true, widthDp = 500 )
//@Composable
//fun GreetingPreviewCompact() {
//    val viewModel: ParkingAppViewModel = viewModel()
//
//    ParkingApp(
//        windowSize = WindowWidthSizeClass.Compact,
//        viewModel = viewModel,
//        modifier = Modifier,
//    )
//}
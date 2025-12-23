package com.kontenery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kontenery.library.utils.InvoiceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.kontenery.model.ModalData
import com.kontenery.service.ParkingAppViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpBar(
    modifier: Modifier = Modifier,
    title: String,
    canNavigateBack: Boolean,
    drawerClick: () -> Unit = {},
    navigateUp: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                modifier,
                textAlign = TextAlign.Center,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back icon",
                    modifier = Modifier.clickable {
                        navigateUp()
                    }.padding(8.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "no Icon",
                    modifier = Modifier
                        .padding(8.dp)
                        .alpha(0f)
                )
            }

        },
        actions = {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu icon",
                modifier = Modifier.clickable {
                    drawerClick()
                }.padding(8.dp)
            )
        }
    )
}

@Composable
fun Drawer(
    viewModel: ParkingAppViewModel,
    scope: CoroutineScope,
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text(text = "Klienci") },
                    selected = true,
                    onClick = {
                            toggleDrawer(scope, drawerState)
                            viewModel.getClientsList(0, 100)
                            viewModel.toClientList()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Dodaj klienta") },
                    selected = false,
                    onClick = {
                        toggleDrawer(scope, drawerState)
//                        viewModel.createNewClient()
                        viewModel.toClientData()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Produkty") },
                    selected = false,
                    onClick = {
                        toggleDrawer(scope, drawerState)
                        viewModel.toProductsList()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Dodaj produkt") },
                    selected = false,
                    onClick = {
                        toggleDrawer(scope, drawerState)
                        viewModel.toAddProduct()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Wyślij faktury okresowe") },
                    selected = false,
                    onClick = {
                        val modal = ModalData(
                            onDismissRequest = { viewModel.closeConfirmationModal() },
                            onConfirmation = { viewModel.sendPeriodicInvoiceToAllClients() },
                            dialogTitle = "Wyślij faktury okresowe",
                            dialogText = "Czy na pewno chcesz wysłać faktury okresowe?"
                        )
                        viewModel.createConfirmationModal(modal)
                        println("sendPeriodicInvoiceToAllClients Wyślij faktury okresowe")
                        toggleDrawer(scope, drawerState)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Drukuj faktury") },
                    selected = false,
                    onClick = {
                        viewModel.printAllInvoices()
                        toggleDrawer(scope, drawerState)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Wystaw Fakturę") },
                    selected = false,
                    onClick = {
                        toggleDrawer(scope, drawerState)
                        viewModel.createNewInvoice(InvoiceType.OTHER)
                        viewModel.toAddInvoice()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Wgraj Płatności") },
                    selected = false,
                    onClick = {
                        toggleDrawer(scope, drawerState)
                        viewModel.toUploadPayments()
                    }
                )
            }
        },
        modifier = modifier,
        drawerState = drawerState,
        gesturesEnabled = false,
        scrimColor = DrawerDefaults.scrimColor,
        content = content
    )
}

fun toggleDrawer(scope: CoroutineScope, drawerState: DrawerState) {
    scope.launch {
        if(drawerState.isOpen) {
            drawerState.close()
        } else {
            drawerState.open()
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun Preview() {
//    TopUpBar(
//        modifier = Modifier.fillMaxWidth(),
//        title = "dupa",
//        canNavigateBack = true,
//        navigateUp = {},
//        drawerClick = {},
////        actions = {}
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewDrawer() {
//    Drawer(
//        viewModel = ParkingAppViewModel(),
//        scope = rememberCoroutineScope(),
//        drawerState = DrawerState(DrawerValue.Open),
//        modifier = Modifier,
//        content = {},
//    )
//}

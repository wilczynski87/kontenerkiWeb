package com.kontenery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.parkingandroidview.ui.DatePickerDocked
import com.kontenery.model.Product.Container
import com.kontenery.model.Product.Yard
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.model.enums.now
import com.kontenery.service.ParkingAppViewModel
import kotlinx.datetime.LocalDate

@Composable
fun AddProductMenu(
    windowSize: WindowWidthSizeClass,
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val product = viewModel.state.collectAsState().value.newProduct
    val openAlertDialog = remember { mutableStateOf(false) }
    val addNewProductModal = viewModel.state.collectAsState().value.addNewProductError

    when { addNewProductModal ->
        {
            AddNewProductErrorModal(
                onDismissRequest = {
                    if (product != null) viewModel.saveProduct(product)
                    viewModel.toggleAddProductModal()
                },
                onConfirmation = {
                    viewModel.toggleAddProductModal()
                    println("Confirmation registered") // Add logic here to handle confirmation.
                },
                dialogTitle = "Dodaj produkt",
                dialogText = "Problem z dodaniem produktu",
                icon = Icons.Default.Info
            )
            AddNewProductErrorModal(
                onDismissRequest = {
//                    viewModel.getClientsList(0, 100)
                    viewModel.toggleClientsListModal()
                },
                onConfirmation = {
                    viewModel.getClientsList(0, 100)
                    viewModel.toggleClientsListModal()
                    println("Confirmation registered") // Add logic here to handle confirmation.
                },
                dialogTitle = "Lista Klientów",
                dialogText = "Problem z wczytaniem listy",
                icon = Icons.Default.Info
            )
        }
    }

    if(windowSize == WindowWidthSizeClass.Compact) {
//        Crossfade(targetState = currentPage, label = "cross fade"
//        ) { screen ->
            when (product) {
                is Yard -> {
                    YardForm(viewModel, modifier)
                }
                is Container -> {
                    ContainerForm(viewModel, modifier)
                }
                else -> {
                    AddProductChoice(viewModel, modifier)
                }
            }
//        }
    } else {
        Row(modifier = modifier
                .fillMaxWidth()
            , horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AddProductChoice(viewModel)
                when (product) {
                    is Yard -> {
                        // YardForm(viewModel, modifier)
                    }
                    is Container -> {
                        ContainerForm(viewModel, modifier)
                    }
                    else -> {
                        Column {
                            Spacer(modifier = Modifier.height(80.dp))
                            println("nowy produkt $product")
                            Text(
                                "Wybierz Produkt z listy",
                                modifier = modifier
                                    .fillMaxWidth(),
                                style = MaterialTheme.typography.headlineLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
        }
    }
}

@Composable
fun AddProductChoice(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Dodaj nowy produkt:",
            modifier = Modifier
                .padding(bottom = 16.dp,
                    start = 4.dp,
                    end = 4.dp,
                    top = 4.dp)
            , style = MaterialTheme.typography.headlineLarge
            , textAlign = TextAlign.Center
        )

        TextButton(
            onClick = {
                viewModel.newProduct(Container(length = "20ft"))
                println("nowy produkt nowy produkt")
                      },
            modifier = Modifier,
            enabled = true,
        ){
            Text("Kontener 6m")
        }

        TextButton(
            onClick = { viewModel.newProduct(Container(length = "40ft")) },
            modifier = Modifier,
            enabled = true,
        ){
            Text("Kontener 12m")
        }

        TextButton(
            onClick = { viewModel.newProduct(Yard()) },
            modifier = Modifier,
            enabled = true,
        ){
            Text("Plac")
        }

//        TextButton(
//            onClick = { viewModel.newProduct(Product()) },
//            modifier = Modifier,
//            enabled = true,
//        ){
//            Text("Magazynek")
//        }

//        TextButton(
//            onClick = { viewModel.newProduct(Product()) },
//            modifier = Modifier,
//            enabled = true,
//        ){
//            Text("Paleta")
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerForm(
    viewModel: ParkingAppViewModel
    , modifier: Modifier = Modifier
) {
    val container: Container = viewModel.state.collectAsState().value.newProduct as Container

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        var textFieldModyfier = Modifier.fillMaxWidth()
        Text(
            "Dodaj nowy kontener 6m"
            , modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
            , style = MaterialTheme.typography.headlineLarge
            , textAlign = TextAlign.Center
        )
        TextField(
            value = container.name ?: "",
            onValueChange = {
                viewModel.updateProduct(container.copy(name = it))
            },
            label = { Text("Nazwa") },
            modifier = textFieldModyfier
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = container.height ?: "",
            onValueChange = { viewModel.updateProduct(container.copy(height = it)) },
            label = { Text("Wysokość") },
            modifier = textFieldModyfier
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = container.color ?: "",
            onValueChange = { viewModel.updateProduct(container.copy(color = it)) },
            label = { Text("Kolor") },
            modifier = textFieldModyfier
        )
        Spacer(modifier = Modifier.height(8.dp))
        DatePickerDocked(
            currentDate = LocalDate.now(),
            updateDate = { },
            title = "Data zakupu:",
        )
        Spacer(modifier = Modifier.height(8.dp))
        DatePickerDocked(
            currentDate = LocalDate.now(),
            updateDate = {},
            title = "Ostatnie Malowanie:",
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = container.description ?: "",
            onValueChange = { viewModel.updateProduct(container.copy(description = it)) },
            label = { Text("Opis: ") },
            modifier = textFieldModyfier
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = container.location ?: "",
            onValueChange = { viewModel.updateProduct(container.copy(location = it)) },
            label = { Text("Miejsce: ") },
            modifier = textFieldModyfier
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
//            value = container.photo ?: "",
            value = "",
            onValueChange = {
//                viewModel.updateProduct(container.copy(photo = ))
            },
            label = { Text("Zdjecie:") },
            modifier = textFieldModyfier
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
//                .align(Alignment.CenterHorizontally)
            , horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Button(
                onClick = {
                    viewModel.clearProduct()
                },
                modifier = Modifier
                ,
            ) {
                Text("Nowy produkt")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    // Create a Container6 object with the entered data
                    viewModel.saveProduct(container)
                },
                modifier = Modifier
                ,
            ) {
                Text("Dodaj")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YardForm(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val yard: Yard = (viewModel.state.collectAsState().value.newProduct as? Yard) ?: Yard()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Dodaj nowy plac",
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        ProductTextField(
            value = yard.name,
            onValueChange = { viewModel.updateProduct(yard.copy(name = it)) },
            label = "Nazwa"
        )

        ProductTextField(
            value = yard.location,
            onValueChange = { viewModel.updateProduct(yard.copy(location = it)) },
            label = "Lokalizacja"
        )

        ProductTextField(
            value = yard.quantity?.toString(),
            onValueChange = { input ->
                val quantity = input.toLongOrNull()
                viewModel.updateProduct(yard.copy(quantity = quantity))
            },
            label = "Powierzchnia (m2)"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { viewModel.clearProduct() }) {
                Text("Nowy produkt")
            }

            Button(onClick = { viewModel.saveProduct(yard) }) {
                Text("Dodaj")
            }
        }
    }
}

@Composable
fun ProductTextField(
    value: String?,
    onValueChange: (String) -> Unit,
    label: String
) {
    TextField(
        value = value.orEmpty(),
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
}

//@Preview(showBackground = true, widthDp = 340 )
//@Composable
//fun PreviewProductUiCompact() {
//    val viewModel = ParkingAppViewModel()
//    val windowSize = WindowWidthSizeClass.Compact
//    AddProductMenu(windowSize, viewModel)
//}
//
//@Preview(showBackground = true, widthDp = 340 )
//@Composable
//fun PreviewContainer6Form() {
//    val viewModel: ParkingAppViewModel = ParkingAppViewModel()
//    val state = ParkingAppState().copy(newProduct = Container())
//    viewModel.setState(state)
//    ContainerForm(viewModel)
//}
//
//@Preview(showBackground = true, widthDp = 340 )
//@Composable
//fun PreviewYardForm() {
//    val viewModel: ParkingAppViewModel = ParkingAppViewModel()
//    val state = ParkingAppState().copy(newProduct = Container())
//    viewModel.setState(state)
//    YardForm(viewModel)
//}

//@Preview(showBackground = true, widthDp = 700 )
//@Composable
//fun PreviewProductUiMedium() {
//    val viewModel = ParkingAppViewModel()
//    val windowSize = WindowWidthSizeClass.Medium
//    AddProductMenu(windowSize, viewModel)
//}
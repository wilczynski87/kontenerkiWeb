package com.kontenery.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kontenery.library.model.Contract
import com.kontenery.model.Product
import com.kontenery.model.Product.Container
import com.kontenery.model.Product.Yard
import com.kontenery.model.Client
import com.kontenery.model.enums.ProductFilter
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.service.ParkingAppViewModel
import konteneryweb.composeapp.generated.resources.Res
import konteneryweb.composeapp.generated.resources.container_blue
import konteneryweb.composeapp.generated.resources.yard_ruler
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProductListWithFilter(
    windowSize: WindowWidthSizeClass,
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val products = viewModel.state.collectAsState().value.products
    var selectedFilter by remember { mutableStateOf(ProductFilter.ALL) }
    val changeFilter: (ProductFilter) -> Unit = { selectedFilter = it }
    var query by remember { mutableStateOf("") }

    val filteredProducts = remember(query, products, selectedFilter) {
        products.filter{ it.name!!.contains(query, ignoreCase = true) }
    }
    val sortedProducts = when (selectedFilter) {
        ProductFilter.ALL -> filteredProducts.sortedBy { it.name }
        ProductFilter.ACTIVE -> filteredProducts.sortedWith(
            compareByDescending<Product> { it.client == null }.thenBy { it.name }
        )
        ProductFilter.TYPE -> filteredProducts.sortedWith(
            compareByDescending<Product> { it is Container }.thenBy { it.name }
        )
        ProductFilter.SUBTYPE -> filteredProducts.sortedWith(
            compareBy<Product> {
                when (it) {
                    is Container -> when (it.length) {
                        "40ft" -> 1
                        "20ft" -> 2
                        else -> 3
                    }
                    is Yard -> 4
                }
            }.thenBy { it.name }
        )
    }

    if(products.isEmpty()) LoadingBox("kontenery i place")
    else {
        when(windowSize) {
            WindowWidthSizeClass.Expanded -> {
                Column(modifier = modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Szukaj Produktu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    FilterButtons(
                        filters = ProductFilter.entries,
                        selectedFilter = selectedFilter,
                        changeFilter = { selectedFilter = it },
                        labelProvider = { it.label }
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(
                            sortedProducts,
                        ) {
                            ProductCard(it, viewModel)
                        }
                    }
                }
            }
            else -> {
                Column(modifier = modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Szukaj Produktu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    FilterButtons(
                        filters = ProductFilter.entries,
                        selectedFilter = selectedFilter,
                        changeFilter = { selectedFilter = it },
                        labelProvider = { it.label }
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(
                            sortedProducts,
                        ) {
                            ProductCard(it, viewModel)
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun ProductCard(product: Product, viewModel: ParkingAppViewModel) {
    val details: Long? = viewModel.state.collectAsState().value.productNavRow
    OutlinedCard(
        modifier = Modifier.padding(2.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        viewModel.toggleProductNavRow(product.id)
                    })
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProductIcon(product)
                Spacer(modifier = Modifier.padding(8.dp))
                Text(product.location.toString())
                Spacer(modifier = Modifier.padding(8.dp))
                ProductClient(product.client, viewModel)
                Spacer(modifier = Modifier.padding(8.dp))
            }

            AnimatedVisibility(
                visible = product.id == details,
                modifier = Modifier.padding(horizontal = 5.dp)
            ) {
                ProductNavRow(
                    product = product,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun ProductClient(client: Client?, viewModel: ParkingAppViewModel) {
    if(client == null) Text("BRAK NAJEMCY")
    else {
        val overdue: Double? = viewModel.getClientOverdue(client.id!!)
        val color: Color = if(overdue != null && overdue <= 0) Color.Red else Color.Transparent
        Text(client.getName(), modifier = Modifier.background(color, RoundedCornerShape(25)))
    }
}

@Composable
fun ProductNavRow(
    product: Product,
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier,
) {
    val clientId: Long? = product.client?.id

    Row(modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Dane produktu
        Button(
            modifier = Modifier.padding(end = 4.dp),
            onClick = {
                viewModel.updateProduct(product)
                viewModel.toAddProduct()
            }
        ) {
            Text("Dane produktu")
        }
        // Umowa
        if(product.id != null) {
            Button(
                modifier = Modifier.padding(end = 4.dp),
                onClick = {
                    if(clientId == null) {
                        viewModel.updateContract(Contract(product = product))
                        viewModel.toAddContract(productEnabled = false)
                    } else {
                        viewModel.getContractByProductId(product.id!!)
                    }
//                    println("umowa: ${Contract(product = product)}")
                }
            ) {
                Text("Umowa")
            }
        }
    }
}

@Composable
fun ProductIcon(product: Product?) {
//    println("ProductIcon $product")
    when(product) {
        is Container -> {
            val containerColor: Color = if(product.length.equals("20ft")) Color.Blue else Color.Red
            Row() {
                Icon(
                    painter = painterResource(resource = Res.drawable.container_blue),
                    contentDescription = "Kontener morski",
                    tint = containerColor
                )
                Text(
                    product.length.toString(),
                    modifier = Modifier.padding(start = 4.dp)
                )

            }
        }
        is Yard -> {
            Row() {
                Icon(
                    painter = painterResource(resource = Res.drawable.yard_ruler),
                    contentDescription = null
                )
                Text(
                    product.quantity.toString()
                    ,  modifier = Modifier.padding(4.dp)
                )
            }
        }
        else -> {
        }
    }
}

//@Preview(showBackground = true, widthDp = 500 )
//@Composable
//fun PreviewProductList() {
//    val sampleContainers = listOf(
//        Container(
//            id = 1,
//            name = "a1",
//            location = "a1",
//            length = "20ft",
//            height = "2.5m",
//            color = "Blue",
//            acquireDate = LocalDate(2021, 5, 10),
//            lastPainting = LocalDate(2023, 4, 1),
//            description = "Used for storing construction materials"
//        ),
//        Container(
//            id = 2,
//            name = "a2",
//            location = "a2",
//            length = "40ft",
//            height = "2.9m",
//            color = "Green",
//            acquireDate = LocalDate(2020, 3, 14),
//            lastPainting = LocalDate(2022, 8, 15),
//            description = "Refrigerated container for perishables"
//        ),
//        Container(
//            id = 3,
//            name = "B1",
//            location = "B1",
//            length = "20ft",
//            height = "2.6m",
//            color = "Red",
//            acquireDate = LocalDate(2019, 11, 25),
//            lastPainting = LocalDate(2023, 1, 10),
//            description = "Container used for heavy equipment transport"
//        )
//    )
//
//    val sampleYards = listOf(
//        Yard(
//            id = 4,
//            name = "Main Storage Yard",
//            location = "South Wing",
//            quantity = 1500L
//        ),
//        Yard(
//            id = 5,
//            name = "Secondary Yard",
//            location = "North Section",
//            quantity = 800L
//        ),
//        Yard(
//            id = 6,
//            name = "Temporary Yard",
//            location = "East Wing",
//            quantity = 400L
//        )
//    )
//
//    val viewModel = ParkingAppViewModel()
//    val state = ParkingAppState()
//    state.copy(products = sampleContainers + sampleYards)
//    viewModel.setState(state.copy(products = sampleContainers + sampleYards))
//    val windowSize: WindowWidthSizeClass = WindowWidthSizeClass.Compact
//
//    ProductListWithFilter(windowSize, viewModel)
//}
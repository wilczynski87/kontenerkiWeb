package com.kontenery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoadingBox(info: String? = "") {
    Box(modifier = Modifier.fillMaxSize()
        , contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier
        ) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            LoadingDotsText(baseText = "Wczytywanie $info", modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun LoadingDotsText(
    baseText: String = "",
    dotChar: String = ".",
    dotCount: Int = 3,
    intervalMillis: Long = 500L,
    modifier: Modifier = Modifier
) {
    var dots by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            for (i in 0..dotCount) {
                dots = dotChar.repeat(i)
                delay(intervalMillis)
            }
            dots = "" // reset i cykl od nowa
        }
    }

    Text(text = baseText + dots)
}

@Preview
@Composable
fun LoadingBoxPreview() {
    LoadingBox()
}
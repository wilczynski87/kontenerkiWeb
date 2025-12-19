package com.kontenery

// commonMain
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.kontenery.data.CSVType

data class FileResult(
    val name: String,
    val bytes: ByteArray,
    val mimeType: String? = null
)

typealias PickFileFunc = suspend () -> FileResult?

/**
 * Wspólny composable: wymagamy od platformy dostarczenia funkcji pickFile
 */
@Composable
fun FilePickerButton(
    pickFile: PickFileFunc,
    onPicked: (FileResult) -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String = "Wybierz plik",
    fileType: CSVType? = CSVType.PEKAOSABUSSINESS,
) {
    val scope = rememberCoroutineScope()
    Button(onClick = {
        scope.launch {
            val result = pickFile()
            if (result != null) onPicked(result)
        }
    }, modifier = modifier) {
        Text(buttonText)
    }
}

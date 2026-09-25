package com.kontenery

// commonMain
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.kontenery.data.CSVType
import kotlinx.coroutines.launch

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

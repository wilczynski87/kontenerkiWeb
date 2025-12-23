package com.kontenery.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.kontenery.library.utils.errors.ErrorMessage

@Composable
fun AddNewProductErrorModal(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Zamknij")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Powtórz")
            }
        }
    )
}

@Composable
fun ConfirmSending(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Potwierdź")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Zamknij")
            }
        }
    )
}

@Composable
fun ResponseModal(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: List<ErrorMessage>,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Column {
                for(error : ErrorMessage in dialogText) {
                    Text(text = error.title ?: "")
                    Text(text = error.message ?: "")
                }
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Potwierdź")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Zamknij")
            }
        }
    )
}

//@Preview
//@Composable
//fun prewiewAlert() {
//    AddNewProductErrorModal(
//        onConfirmation = {},
//        onDismissRequest = {},
//        dialogTitle = "Server Error",
//        dialogText = "Could not connect to server",
//        icon = Icons.Default.Warning
//    )
//}

//@Preview
//@Composable
//fun prewiewAlert() {
//    ConfirmSending(
//        onConfirmation = {},
//        onDismissRequest = {},
//        dialogTitle = "Server Error",
//        dialogText = "Could not connect to server",
//        icon = Icons.Default.Warning
//    )
//}
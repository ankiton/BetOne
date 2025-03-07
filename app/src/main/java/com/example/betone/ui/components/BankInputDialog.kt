package com.example.betone.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun BankInputDialog(onConfirm: (Double) -> Unit, onDismiss: () -> Unit) {
    var bankText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Введите начальный банк") },
        text = {
            TextField(
                value = bankText,
                onValueChange = { bankText = it.filter { it.isDigit() || it == '.' } },
                label = { Text("Сумма") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            Button(onClick = { bankText.toDoubleOrNull()?.let { onConfirm(it) } }) { Text("ОК") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Отмена") } }
    )
}
package com.example.betone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.betone.viewmodel.BettingViewModel
import kotlinx.coroutines.launch

@Composable
fun BankScreen(viewModel: BettingViewModel, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val initialBank by produceState<Double?>(initialValue = null) { value = viewModel.getBankAmount() }
    val currentBank by viewModel.currentBank.observeAsState(initialBank)
    var bankText by remember { mutableStateOf(initialBank?.toString() ?: "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Начальный банк: ${initialBank ?: "Не задан"}",
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Текущий банк: ${currentBank ?: "Не задан"}",
            color = MaterialTheme.colorScheme.onBackground
        )
        TextField(
            value = bankText,
            onValueChange = { bankText = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Новый начальный банк") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF1F8E9), // Бледный салатовый
                unfocusedContainerColor = Color(0xFFF1F8E9),
                disabledContainerColor = Color(0xFFF1F8E9),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
            )
        )
        Button(onClick = {
            bankText.toDoubleOrNull()?.let { amount ->
                scope.launch { viewModel.updateBank(amount) }
            }
        }) { Text("Обновить банк") }
    }
}
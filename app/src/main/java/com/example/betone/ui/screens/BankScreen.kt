package com.example.betone.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Начальный банк: ${initialBank ?: "Не задан"}")
        Text("Текущий банк: ${currentBank ?: "Не задан"}")
        TextField(
            value = bankText,
            onValueChange = { bankText = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Новый начальный банк") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Button(onClick = {
            bankText.toDoubleOrNull()?.let { amount ->
                scope.launch { viewModel.updateBank(amount) }
            }
        }) { Text("Обновить банк") }
    }
}
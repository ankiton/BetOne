package com.example.betone.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.betone.data.entity.BetEntity
import com.example.betone.viewmodel.BetResult
import com.example.betone.viewmodel.BettingViewModel
import kotlinx.coroutines.launch

@Composable
fun BettingScreen(branchId: Int, viewModel: BettingViewModel, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var coefficientText by remember { mutableStateOf(TextFieldValue("")) }
    val betAmount by viewModel.currentBetAmount.observeAsState()
    var forceUpdate by remember { mutableStateOf(0) }
    val activeBet by produceState<BetEntity?>(initialValue = null, key1 = branchId, key2 = forceUpdate) {
        value = viewModel.getActiveBet(branchId)
    }
    val pendingLosses by produceState<List<BetEntity>>(initialValue = emptyList(), key1 = branchId, key2 = forceUpdate) {
        value = viewModel.getPendingLosses(branchId)
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = coefficientText,
            onValueChange = { newValue ->
                val filtered = newValue.text.filter { it.isDigit() }
                coefficientText = when (filtered.length) {
                    0 -> TextFieldValue("")
                    1 -> TextFieldValue(filtered, selection = TextRange(filtered.length))
                    else -> {
                        val newText = "${filtered.take(1)}.${filtered.drop(1)}"
                        TextFieldValue(newText, selection = TextRange(newText.length))
                    }
                }
                val coef = coefficientText.text.toDoubleOrNull()
                if (coef != null) {
                    scope.launch { viewModel.calculateBet(branchId, coef) }
                }
            },
            label = { Text("Коэффициент (1.65–2.3)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = activeBet == null
        )

        Text(
            text = when {
                betAmount == null -> "Введите коэффициент"
                (betAmount ?: 0.0) < 0 -> "Неверный коэффициент"
                else -> "Сумма ставки: %.2f".format(betAmount)
            }
        )

        if (activeBet == null) {
            Button(
                onClick = {
                    val coef = coefficientText.text.toDoubleOrNull() ?: return@Button
                    scope.launch {
                        viewModel.placeBet(branchId, coef)
                        coefficientText = TextFieldValue("")
                        forceUpdate += 1
                    }
                },
                enabled = betAmount?.let { it >= 0 } ?: false
            ) { Text("Поставить") }
        } else {
            activeBet?.let { bet ->
                Text("Ставка в игре: ${bet.amount} на ${bet.coefficient}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        scope.launch {
                            viewModel.resolveBet(bet.id, BetResult.WIN)
                            forceUpdate += 1
                        }
                    }) { Text("Выигрыш") }
                    Button(onClick = {
                        scope.launch {
                            viewModel.resolveBet(bet.id, BetResult.LOSS)
                            forceUpdate += 1
                        }
                    }) { Text("Проигрыш") }
                    Button(onClick = {
                        scope.launch {
                            viewModel.resolveBet(bet.id, BetResult.RETURN)
                            forceUpdate += 1
                        }
                    }) { Text("Возврат") }
                }
            }
        }

        if (pendingLosses.isNotEmpty()) {
            Text("Завершённые ставки до выигрыша:")
            LazyColumn {
                items(pendingLosses) { bet ->
                    val status by produceState(initialValue = "Загрузка...", key1 = bet.id) {
                        value = viewModel.getBetStatus(bet)
                    }
                    Text("Сумма: ${bet.amount}, Коэффициент: ${bet.coefficient}, Статус: $status")
                }
            }
        }
    }
}

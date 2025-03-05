package com.example.betone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.betone.viewmodel.BettingViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.betone.viewmodel.BettingViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BettingApp()
        }
    }
}

@Composable
fun BettingApp(viewModel: BettingViewModel = viewModel(
    factory = BettingViewModelFactory(AppDatabase.getDatabase(androidx.compose.ui.platform.LocalContext.current))
)) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            viewModel.initBank(10000.0) // Инициализация асинхронно
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    val betAmount by viewModel.currentBetAmount.observeAsState()

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                (0..2).forEach { index ->
                    Tab(
                        text = { Text("Ветка ${index + 1}") },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        BettingScreen(
            branchId = selectedTab + 1,
            viewModel = viewModel,
            betAmount = betAmount,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
@Composable
fun BettingScreen(branchId: Int, viewModel: BettingViewModel, betAmount: Double?, modifier: Modifier = Modifier) {
    var coefficientText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = coefficientText,
            onValueChange = { newValue ->
                coefficientText = newValue
                val coef = newValue.toDoubleOrNull()
                if (coef != null) {
                    viewModel.calculateBet(branchId, coef) // Синхронный вызов
                }
            },
            label = { Text("Введите коэффициент (1.65–2.3)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = when {
                betAmount == null -> "Введите коэффициент"
                betAmount < 0 -> "Неверный коэффициент"
                else -> "Сумма ставки: %.2f".format(betAmount)
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    val coef = coefficientText.toDoubleOrNull() ?: return@Button
                    scope.launch {
                        viewModel.processBet(branchId, coef, true)
                        coefficientText = ""
                    }
                },
                enabled = betAmount != null && betAmount >= 0
            ) {
                Text("Выигрыш")
            }
            Button(
                onClick = {
                    val coef = coefficientText.toDoubleOrNull() ?: return@Button
                    scope.launch {
                        viewModel.processBet(branchId, coef, false)
                        coefficientText = ""
                    }
                },
                enabled = betAmount != null && betAmount >= 0
            ) {
                //Text("Проигрыш")
            }
        }
    }
}

package com.example.betone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.betone.data.entity.BetEntity
import com.example.betone.viewmodel.BettingViewModel

@Composable
fun HomeScreen(viewModel: BettingViewModel, modifier: Modifier = Modifier) {
    val activeBets by produceState<List<BetEntity>>(initialValue = emptyList()) {
        value = viewModel.getActiveBets()
    }
    val branchNames by viewModel.branchNames.observeAsState(emptyMap())
    val currentBank by viewModel.currentBank.observeAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Добавляем фон из темы
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeBets.isEmpty()) {
            Text(
                text = "Нет незакрытых ставок",
                color = MaterialTheme.colorScheme.onBackground // Цвет текста из темы
            )
        } else {
            Text(
                text = "Незакрытые ставки по веткам:",
                color = MaterialTheme.colorScheme.onBackground
            )
            LazyColumn {
                items(activeBets) { bet ->
                    val branchName = branchNames[bet.branchId] ?: "Ветка ${bet.branchId}"
                    Text(
                        text = "$branchName: ${bet.amount}, Коэффициент: ${bet.coefficient} (в игре)",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
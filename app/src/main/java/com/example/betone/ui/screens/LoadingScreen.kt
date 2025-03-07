package com.example.betone.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun LoadingScreen(viewModel: BettingViewModel) {
    val currentBank by viewModel.currentBank.observeAsState()
    val activeBets by produceState<List<BetEntity>>(initialValue = emptyList()) {
        value = viewModel.getActiveBets()
    }
    val branchNames by viewModel.branchNames.observeAsState(emptyMap())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Текущий банк: ${currentBank ?: "Загрузка..."}")
        if (activeBets.isNotEmpty()) {
            Text("Незакрытые ставки:")
            LazyColumn {
                items(activeBets) { bet ->
                    val branchName = branchNames[bet.branchId] ?: "Ветка ${bet.branchId}"
                    Text("$branchName: ${bet.amount}, ${bet.coefficient} (в игре)")
                }
            }
        } else {
            Text("Нет незакрытых ставок")
        }
    }
}
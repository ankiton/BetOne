package com.example.betone.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.betone.viewmodel.BettingViewModel
import kotlinx.coroutines.flow.flow

@Composable
fun HistoryScreen(viewModel: BettingViewModel, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var filterBranchId by remember { mutableStateOf<Int?>(null) }
    val betsFlow = remember(filterBranchId) {
        if (filterBranchId == null) flow { emit(viewModel.getAllBets()) }
        else flow { emit(viewModel.getBetsForBranch(filterBranchId!!)) }
    }
    val bets by betsFlow.collectAsState(initial = emptyList())
    val branchNames by viewModel.branchNames.observeAsState(emptyMap())

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { filterBranchId = null }) { Text("Все ветки") }
            Button(onClick = { filterBranchId = 1 }) { Text(branchNames[1] ?: "Ветка 1") }
            Button(onClick = { filterBranchId = 2 }) { Text(branchNames[2] ?: "Ветка 2") }
            Button(onClick = { filterBranchId = 3 }) { Text(branchNames[3] ?: "Ветка 3") }
        }
        if (bets.isEmpty()) {
            Text("История ставок пуста")
        } else {
            LazyColumn {
                items(bets) { bet ->
                    val branchName = branchNames[bet.branchId] ?: "Ветка ${bet.branchId}"
                    val status by produceState(initialValue = "Загрузка...", key1 = bet.id) {
                        value = viewModel.getBetStatus(bet)
                    }
                    Text(
                        "$branchName: ${bet.amount}, Коэффициент: ${bet.coefficient}, Статус: $status"
                    )
                }
            }
        }
    }
}
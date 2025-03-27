package com.example.betone.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.betone.data.entity.BetEntity
import com.example.betone.viewmodel.BettingViewModel
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(viewModel: BettingViewModel, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var filterBranchId by remember { mutableStateOf<Int?>(null) }
    var forceUpdate by remember { mutableStateOf(0) }

    val bets by produceState(
        initialValue = emptyList<BetEntity>(),
        key1 = filterBranchId,
        key2 = forceUpdate
    ) {
        try {
            value = if (filterBranchId == null) {
                Log.d("HistoryScreen", "Loading all bets")
                viewModel.getAllBets()
            } else {
                Log.d("HistoryScreen", "Loading bets for branch $filterBranchId")
                viewModel.getBetsForBranch(filterBranchId!!)
            }
            Log.d("HistoryScreen", "Loaded bets: $value")
        } catch (e: Exception) {
            Log.e("HistoryScreen", "Error loading bets", e)
            value = emptyList()
        }
    }

    val branchNames by viewModel.branchNames.observeAsState(emptyMap())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    filterBranchId = null
                    scope.launch {
                        forceUpdate++
                    }
                }
            ) { Text("Все ветки") }
            Button(
                onClick = {
                    filterBranchId = 1
                    scope.launch {
                        forceUpdate++
                    }
                }
            ) { Text(branchNames[1] ?: "Ветка 1") }
            Button(
                onClick = {
                    filterBranchId = 2
                    scope.launch {
                        forceUpdate++
                    }
                }
            ) { Text(branchNames[2] ?: "Ветка 2") }
            Button(
                onClick = {
                    filterBranchId = 3
                    scope.launch {
                        forceUpdate++
                    }
                }
            ) { Text(branchNames[3] ?: "Ветка 3") }
        }

        if (bets.isEmpty()) {
            Text(
                text = "История ставок пуста",
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            LazyColumn {
                items(bets) { bet ->
                    val branchName = branchNames[bet.branchId] ?: "Ветка ${bet.branchId}"
                    val status by produceState(
                        initialValue = "Загрузка...",
                        key1 = bet.id,
                        key2 = forceUpdate
                    ) {
                        value = viewModel.getBetStatus(bet)
                    }
                    Text(
                        text = "$branchName: ${bet.amount}, Коэффициент: ${bet.coefficient}, Статус: $status",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
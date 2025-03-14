package com.example.betone.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.betone.viewmodel.BettingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: BettingViewModel, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var branch1Name by remember { mutableStateOf(viewModel.branchNames.value?.get(1) ?: "Ветка 1") }
    var branch2Name by remember { mutableStateOf(viewModel.branchNames.value?.get(2) ?: "Ветка 2") }
    var branch3Name by remember { mutableStateOf(viewModel.branchNames.value?.get(3) ?: "Ветка 3") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = { scope.launch { viewModel.clearHistory() } }) { Text("Очистить историю") }
        TextField(value = branch1Name, onValueChange = { branch1Name = it }, label = { Text("Название Ветки 1") })
        Button(onClick = { scope.launch { viewModel.renameBranch(1, branch1Name) } }) { Text("Переименовать Ветку 1") }
        TextField(value = branch2Name, onValueChange = { branch2Name = it }, label = { Text("Название Ветки 2") })
        Button(onClick = { scope.launch { viewModel.renameBranch(2, branch2Name) } }) { Text("Переименовать Ветку 2") }
        TextField(value = branch3Name, onValueChange = { branch3Name = it }, label = { Text("Название Ветки 3") })
        Button(onClick = { scope.launch { viewModel.renameBranch(3, branch3Name) } }) { Text("Переименовать Ветку 3") }
    }
}
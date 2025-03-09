package com.example.betone.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.betone.data.AppDatabase
import com.example.betone.ui.screens.BankScreen
import com.example.betone.ui.screens.BettingScreen
import com.example.betone.ui.screens.HistoryScreen
import com.example.betone.ui.screens.HomeScreen
import com.example.betone.ui.screens.SettingsScreen
import com.example.betone.viewmodel.BettingViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BettingApp(database: AppDatabase, viewModel: BettingViewModel) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var selectedScreen by remember { mutableStateOf("Главная") }
    val branchNames by viewModel.branchNames.observeAsState(emptyMap())
    val currentBank by viewModel.currentBank.observeAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Меню", modifier = Modifier.padding(16.dp))
                Divider()
                listOf("Главная", "Ветка 1", "Ветка 2", "Ветка 3", "История", "Банк", "Настройки").forEach { item ->
                    val branchId = when (item) {
                        "Ветка 1" -> 1
                        "Ветка 2" -> 2
                        "Ветка 3" -> 3
                        else -> null
                    }
                    val hasPending = branchId?.let { runBlocking { viewModel.hasPendingBet(it) } } ?: false
                    NavigationDrawerItem(
                        label = { Text(if (branchId != null) "${branchNames[branchId] ?: item}${if (hasPending) " (активно)" else ""}" else item) },
                        selected = selectedScreen == item,
                        onClick = {
                            selectedScreen = item
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedScreen) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        if (selectedScreen != "Банк") {
                            currentBank?.let {
                                Text(
                                    text = "Банк: %.2f".format(it),
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            when (selectedScreen) {
                "Главная" -> HomeScreen(viewModel, Modifier.padding(paddingValues))
                "Ветка 1" -> BettingScreen(branchId = 1, viewModel, Modifier.padding(paddingValues))
                "Ветка 2" -> BettingScreen(branchId = 2, viewModel, Modifier.padding(paddingValues))
                "Ветка 3" -> BettingScreen(branchId = 3, viewModel, Modifier.padding(paddingValues))
                "История" -> HistoryScreen(viewModel, Modifier.padding(paddingValues))
                "Банк" -> BankScreen(viewModel, Modifier.padding(paddingValues))
                "Настройки" -> SettingsScreen(viewModel, Modifier.padding(paddingValues))
            }
        }
    }
}
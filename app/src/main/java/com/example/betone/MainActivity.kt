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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.betone.data.AppDatabase
import com.example.betone.data.entity.BetEntity
import com.example.betone.viewmodel.BetResult
import com.example.betone.viewmodel.BettingViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)
        val viewModel = BettingViewModelFactory(database).create(BettingViewModel::class.java)

        setContent {
            val scope = rememberCoroutineScope()
            var showBankDialog by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                if (!viewModel.isBankInitialized()) {
                    showBankDialog = true
                }
                isLoading = false
            }

            if (showBankDialog) {
                BankInputDialog(
                    onConfirm = { amount ->
                        scope.launch {
                            viewModel.initBank(amount)
                            showBankDialog = false
                        }
                    },
                    onDismiss = { showBankDialog = false }
                )
            } else if (isLoading) {
                LoadingScreen(viewModel)
            } else {
                BettingApp(database, viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BettingApp(database: AppDatabase, viewModel: BettingViewModel) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var selectedScreen by remember { mutableStateOf("Ветка 1") }
    val branchNames by viewModel.branchNames.observeAsState(emptyMap())

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Меню", modifier = Modifier.padding(16.dp))
                Divider()
                listOf("Ветка 1", "Ветка 2", "Ветка 3", "История", "Банк", "Настройки").forEach { item ->
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
                    }
                )
            }
        ) { paddingValues ->
            when (selectedScreen) {
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

@Composable
fun BettingScreen(branchId: Int, viewModel: BettingViewModel, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var coefficientText by remember { mutableStateOf("") }
    val betAmount by viewModel.currentBetAmount.observeAsState()
    val activeBet by produceState<BetEntity?>(initialValue = null, key1 = branchId) {
        value = viewModel.getActiveBet(branchId)
    }
    val pendingLosses by produceState<List<BetEntity>>(initialValue = emptyList(), key1 = branchId) {
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
                val filtered = newValue.filter { it.isDigit() }
                coefficientText = when (filtered.length) {
                    0 -> ""
                    1 -> filtered
                    else -> "${filtered[0]}.${filtered.drop(1)}"
                }
                val coef = coefficientText.toDoubleOrNull()
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
                (betAmount ?: 0.0) < 0 -> "Неверный коэффициент" // Используем Elvis для значения по умолчанию
                else -> "Сумма ставки: %.2f".format(betAmount)
            }
        )

        if (activeBet == null) {
            Button(
                onClick = {
                    val coef = coefficientText.toDoubleOrNull() ?: return@Button
                    scope.launch {
                        viewModel.placeBet(branchId, coef)
                        coefficientText = ""
                    }
                },
                enabled = betAmount?.let { it >= 0 } ?: false
            ) { Text("Поставить") }
        } else {
            val bet = activeBet // Получаем значение activeBet в локальную переменную

            if (bet != null) {
                Text("Ставка в игре: ${bet.amount} на ${bet.coefficient}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { scope.launch { viewModel.resolveBet(bet.id, BetResult.WIN) } }) {
                        Text("Выигрыш")
                    }
                    Button(onClick = { scope.launch { viewModel.resolveBet(bet.id, BetResult.LOSS) } }) {
                        Text("Проигрыш")
                    }
                    Button(onClick = { scope.launch { viewModel.resolveBet(bet.id, BetResult.RETURN) } }) {
                        Text("Возврат")
                    }
                }
            } else {
                Text("Нет активной ставки")
            }

        }

        if (pendingLosses.isNotEmpty()) {
            Text("Проигрыши:")
            LazyColumn {
                items(pendingLosses) { bet ->
                    Text("Сумма: ${bet.amount}, Коэффициент: ${bet.coefficient}")
                }
            }
        }
    }
}

@Composable
fun BankInputDialog(onConfirm: (Double) -> Unit, onDismiss: () -> Unit) {
    var bankText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Введите начальный банк") },
        text = {
            TextField(
                value = bankText,
                onValueChange = { bankText = it.filter { it.isDigit() || it == '.' } },
                label = { Text("Сумма") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            Button(onClick = { bankText.toDoubleOrNull()?.let { onConfirm(it) } }) { Text("ОК") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
fun HistoryScreen(viewModel: BettingViewModel, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var filterBranchId by remember { mutableStateOf<Int?>(null) }
    val bets by produceState<List<BetEntity>>(initialValue = emptyList(), key1 = filterBranchId) {
        value = if (filterBranchId == null) viewModel.getAllBets() else viewModel.getBetsForBranch(filterBranchId!!)
    }
    val branchNames by viewModel.branchNames.observeAsState(emptyMap())

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { filterBranchId = null }) { Text("Все ветки") }
            Button(onClick = { filterBranchId = 1 }) { Text(branchNames[1] ?: "Ветка 1") }
            Button(onClick = { filterBranchId = 2 }) { Text(branchNames[2] ?: "Ветка 2") }
            Button(onClick = { filterBranchId = 3 }) { Text(branchNames[3] ?: "Ветка 3") }
        }
        LazyColumn {
            items(bets) { bet ->
                val branchName = branchNames[bet.branchId] ?: "Ветка ${bet.branchId}"
                Text("$branchName: ${bet.amount}, ${bet.coefficient}, ${when (bet.isWin) { true -> "Выигрыш"; false -> "Проигрыш/Возврат"; null -> "В игре" }}")
            }
        }
    }
}

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
        Button(onClick = { viewModel.renameBranch(1, branch1Name) }) { Text("Переименовать Ветку 1") }
        TextField(value = branch2Name, onValueChange = { branch2Name = it }, label = { Text("Название Ветки 2") })
        Button(onClick = { viewModel.renameBranch(2, branch2Name) }) { Text("Переименовать Ветку 2") }
        TextField(value = branch3Name, onValueChange = { branch3Name = it }, label = { Text("Название Ветки 3") })
        Button(onClick = { viewModel.renameBranch(3, branch3Name) }) { Text("Переименовать Ветку 3") }
    }
}

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
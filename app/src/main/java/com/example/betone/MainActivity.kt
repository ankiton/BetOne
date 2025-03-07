package com.example.betone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.betone.viewmodel.BettingViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.example.betone.data.AppDatabase
import com.example.betone.ui.BettingApp
import com.example.betone.ui.components.BankInputDialog
import com.example.betone.ui.screens.LoadingScreen
import com.example.betone.viewmodel.BettingViewModelFactory
import kotlinx.coroutines.launch


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




package com.example.betone

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.betone.data.AppDatabase
import com.example.betone.ui.BettingApp
import com.example.betone.ui.components.BankInputDialog
import com.example.betone.ui.screens.LoadingScreen
import com.example.betone.ui.theme.BetOneTheme
import com.example.betone.viewmodel.BettingViewModel
import com.example.betone.viewmodel.BettingViewModelFactory
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        val database = AppDatabase.getDatabase(this)
        val viewModel = BettingViewModelFactory(database).create(BettingViewModel::class.java)

        splashScreen.setKeepOnScreenCondition { true }

        setContent {
            BetOneTheme {
                val scope = rememberCoroutineScope()
                var showBankDialog by remember { mutableStateOf(false) }
                var isLoading by remember { mutableStateOf(true) }
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(1500) // 1 сек вращение + 0.5 сек задержка
                    showSplash = false

                    if (!viewModel.isBankInitialized()) {
                        showBankDialog = true
                    } else {
                        isLoading = false
                    }
                    splashScreen.setKeepOnScreenCondition { false }
                }

                when {
                    showSplash -> {
                        SplashScreenContent()
                    }
                    showBankDialog -> {
                        BankInputDialog(
                            onConfirm = { amount ->
                                scope.launch {
                                    viewModel.initBank(amount)
                                    showBankDialog = false
                                    isLoading = false
                                }
                            },
                            onDismiss = { showBankDialog = false }
                        )
                    }
                    isLoading -> {
                        LoadingScreen(viewModel)
                    }
                    else -> {
                        BettingApp(database, viewModel)
                    }
                }
            }
        }
    }

    @Composable
    fun SplashScreenContent() {
        val rotation = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            Log.d("SplashScreen", "Starting rotation animation")
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = 1000)
            )
            Log.d("SplashScreen", "Rotation animation completed")
        }

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground_1_0_1),
            contentDescription = "App Icon",
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F8E9))
                .rotate(rotation.value)
        )
    }
}
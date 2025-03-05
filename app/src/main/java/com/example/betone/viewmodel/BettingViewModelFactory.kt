package com.example.betone.viewmodel

import AppDatabase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BettingViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BettingViewModel(database.bankDao(), database.branchDao(), database.betDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
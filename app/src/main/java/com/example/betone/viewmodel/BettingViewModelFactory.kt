package com.example.betone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.betone.data.AppDatabase
import com.example.betone.viewmodel.bet.BetManagerImpl
import com.example.betone.viewmodel.branch.BranchManagerImpl

class BettingViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BettingViewModel::class.java)) {
            val bankManager = BankManagerImpl(database.bankDao(), BranchManagerImpl(database.branchDao()))
            val betManager = BetManagerImpl(database.betDao(), BranchManagerImpl(database.branchDao()))
            val branchManager = BranchManagerImpl(database.branchDao())
            @Suppress("UNCHECKED_CAST")
            return BettingViewModel(bankManager, betManager, branchManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.example.betone.viewmodel

import com.example.betone.data.entity.BetBranchEntity
import com.example.betone.data.entity.BetEntity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.betone.data.dao.BankDao
import com.example.betone.data.dao.BetDao
import com.example.betone.data.dao.BranchDao
import com.example.betone.data.entity.BankEntity
import java.util.Calendar
class BettingViewModel(
    private val bankDao: BankDao,
    private val branchDao: BranchDao,
    private val betDao: BetDao
) : ViewModel() {
    private val _currentBetAmount = MutableLiveData<Double>()
    val currentBetAmount: LiveData<Double> = _currentBetAmount

    suspend fun initBank(amount: Double) {
        val bank = BankEntity(amount = amount, startDate = System.currentTimeMillis())
        bankDao.insert(bank)
        listOf(1, 2, 3).forEach { branchId ->
            branchDao.insert(BetBranchEntity(branchId, flatAmount = amount * 0.01))
        }
    }

    // Изменяем calculateBet на suspend
    suspend fun calculateBet(branchId: Int, coefficient: Double) {
        if (coefficient !in 1.65..2.3) {
            _currentBetAmount.postValue(-1.0)
            return
        }
        val branch = branchDao.getBranch(branchId) ?: return // Теперь корректно внутри suspend
        val betAmount = if (branch.accumulatedLoss == 0.0) {
            branch.flatAmount
        } else {
            (branch.accumulatedLoss + branch.flatAmount / 2) / (coefficient - 1)
        }
        _currentBetAmount.postValue(betAmount)
    }

    suspend fun processBet(branchId: Int, coefficient: Double, isWin: Boolean) {
        val betAmount = currentBetAmount.value ?: return
        val branch = branchDao.getBranch(branchId) ?: return
        if (isWin) {
            branchDao.updateAccumulatedLoss(branchId, 0.0)
        } else {
            branchDao.updateAccumulatedLoss(branchId, branch.accumulatedLoss + betAmount)
        }
        betDao.insert(
            BetEntity(
                branchId = branchId,
                coefficient = coefficient,
                amount = betAmount,
                isWin = isWin,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Раскомментируй и исправь, если нужно
    suspend fun checkAndResetBank() {
        val bank = bankDao.getLatestBank() ?: return
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val bankMonth = Calendar.getInstance().apply { timeInMillis = bank.startDate }.get(Calendar.MONTH)
        if (currentMonth != bankMonth) {
            initBank(bank.amount) // Переинициализация с той же суммой
        }
    }
}
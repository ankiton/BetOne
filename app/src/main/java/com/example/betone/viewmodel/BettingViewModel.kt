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

    private val _currentBank = MutableLiveData<Double>()
    val currentBank: LiveData<Double> = _currentBank

    private val _branchNames = MutableLiveData<Map<Int, String>>(mapOf(1 to "Ветка 1", 2 to "Ветка 2", 3 to "Ветка 3"))
    val branchNames: LiveData<Map<Int, String>> = _branchNames

    suspend fun initBank(amount: Double) {
        val bank = BankEntity(amount = amount, startDate = System.currentTimeMillis())
        bankDao.insert(bank)
        listOf(1, 2, 3).forEach { branchId ->
            branchDao.insert(BetBranchEntity(branchId, flatAmount = amount * 0.01))
        }
        _currentBank.postValue(amount)
    }

    suspend fun isBankInitialized(): Boolean {
        val bank = bankDao.getLatestBank()
        if (bank != null) _currentBank.postValue(bank.amount)
        return bank != null
    }

    suspend fun updateBank(amount: Double) {
        val latestBank = bankDao.getLatestBank()
        if (latestBank != null) {
            bankDao.insert(BankEntity(id = latestBank.id, amount = amount, startDate = latestBank.startDate))
            recalculateFlat(amount)
            _currentBank.postValue(amount) // Обновляем текущий банк
        } else {
            initBank(amount)
        }
    }

    suspend fun calculateBet(branchId: Int, coefficient: Double) {
        if (coefficient !in 1.65..2.3) {
            _currentBetAmount.postValue(-1.0)
            return
        }
        val branch = branchDao.getBranch(branchId) ?: return
        val betAmount = if (branch.accumulatedLoss == 0.0) {
            branch.flatAmount
        } else {
            (branch.accumulatedLoss + branch.flatAmount / 2) / (coefficient - 1)
        }
        _currentBetAmount.postValue(betAmount)
    }

    suspend fun placeBet(branchId: Int, coefficient: Double) {
        val betAmount = currentBetAmount.value ?: return
        val currentBankValue = _currentBank.value ?: bankDao.getLatestBank()?.amount ?: return
        betDao.insert(
            BetEntity(
                branchId = branchId,
                coefficient = coefficient,
                amount = betAmount,
                isWin = null, // null означает "в игре"
                timestamp = System.currentTimeMillis()
            )
        )
        _currentBank.postValue(currentBankValue - betAmount) // Уменьшаем банк при постановке
    }

    suspend fun resolveBet(betId: Long, result: BetResult) {
        val bet = betDao.getBetById(betId) ?: return // Получаем ставку по ID
        val currentBankValue = _currentBank.value ?: bankDao.getLatestBank()?.amount ?: return
        when (result) {
            BetResult.WIN -> {
                val winAmount = bet.amount * bet.coefficient
                betDao.update(bet.copy(isWin = true)) // Обновляем ставку
                branchDao.updateAccumulatedLoss(bet.branchId, 0.0)
                _currentBank.postValue(currentBankValue + winAmount)
            }
            BetResult.LOSS -> {
                betDao.update(bet.copy(isWin = false)) // Обновляем ставку
                val branch = branchDao.getBranch(bet.branchId) ?: return
                branchDao.updateAccumulatedLoss(bet.branchId, branch.accumulatedLoss + bet.amount)
            }
            BetResult.RETURN -> {
                betDao.update(bet.copy(isWin = false)) // Обновляем ставку
                _currentBank.postValue(currentBankValue + bet.amount)
            }
        }
    }

    suspend fun getPendingLosses(branchId: Int): List<BetEntity> {
        val bets = betDao.getBetsForBranch(branchId)
        val firstWinIndex = bets.indexOfFirst { it.isWin == true }
        return if (firstWinIndex == -1) {
            bets.filter { it.isWin != null } // Все завершённые ставки, если нет выигрыша
        } else {
            bets.take(firstWinIndex + 1).filter { it.isWin != null } // До первого выигрыша + сам выигрыш
        }
    }

    suspend fun getBetStatus(bet: BetEntity): String {
        val bets = betDao.getBetsForBranch(bet.branchId)
        return when {
            bet.isWin == true -> "Выигрыш"
            bet.isWin == false && bets.any { it.isWin == true && it.timestamp > bet.timestamp } -> "Возврат"
            else -> "Проигрыш"
        }
    }

    suspend fun getActiveBet(branchId: Int): BetEntity? {
        return betDao.getBetsForBranch(branchId).firstOrNull { it.isWin == null }
    }

    suspend fun hasPendingBet(branchId: Int): Boolean {
        return getActiveBet(branchId) != null
    }

    suspend fun getAllBets(): List<BetEntity> {
        return betDao.getBetsForBranch(1) + betDao.getBetsForBranch(2) + betDao.getBetsForBranch(3)
    }

    suspend fun getBetsForBranch(branchId: Int): List<BetEntity> {
        return betDao.getBetsForBranch(branchId)
    }

    suspend fun clearHistory() {
        betDao.clearBetsForBranch(1)
        betDao.clearBetsForBranch(2)
        betDao.clearBetsForBranch(3)
        branchDao.updateAccumulatedLoss(1, 0.0)
        branchDao.updateAccumulatedLoss(2, 0.0)
        branchDao.updateAccumulatedLoss(3, 0.0)
    }

    fun renameBranch(branchId: Int, newName: String) {
        val currentNames = _branchNames.value ?: emptyMap()
        _branchNames.postValue(currentNames + (branchId to newName))
    }

    suspend fun getBankAmount(): Double? {
        return bankDao.getLatestBank()?.amount
    }

    suspend fun getActiveBets(): List<BetEntity> {
        return getAllBets().filter { it.isWin == null }
    }

    private suspend fun recalculateFlat(bankAmount: Double) {
        val newFlatAmount = bankAmount * 0.01
        listOf(1, 2, 3).forEach { branchId ->
            val branch = branchDao.getBranch(branchId)
            if (branch != null) {
                branchDao.insert(BetBranchEntity(branchId, flatAmount = newFlatAmount, accumulatedLoss = branch.accumulatedLoss))
            }
        }
    }
}

enum class BetResult {
    WIN, LOSS, RETURN
}
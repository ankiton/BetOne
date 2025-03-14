package com.example.betone.viewmodel

import com.example.betone.data.entity.BetEntity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.betone.viewmodel.bet.BetManager
import com.example.betone.viewmodel.branch.BranchManager

class BettingViewModel(
    private val bankManager: BankManager,
    private val betManager: BetManager,
    private val branchManager: BranchManager
) : ViewModel() {
    val currentBetAmount: LiveData<Double> get() = betManager.currentBetAmount
    val currentBank: LiveData<Double> get() = bankManager.currentBank
    val branchNames: LiveData<Map<Int, String>> get() = branchManager.branchNames

    suspend fun initBank(amount: Double) = bankManager.initBank(amount)
    suspend fun isBankInitialized(): Boolean = bankManager.isBankInitialized()
    suspend fun updateBank(amount: Double) = bankManager.updateBank(amount)
    suspend fun calculateBet(branchId: Int, coefficient: Double) = betManager.calculateBet(branchId, coefficient)
    suspend fun placeBet(branchId: Int, coefficient: Double) {
        val currentBankValue = bankManager.currentBank.value ?: return
        val newBank = betManager.placeBet(branchId, coefficient, currentBankValue)
        bankManager.updateCurrentBank(newBank)
    }
    suspend fun resolveBet(betId: Long, result: BetResult) {
        val currentBankValue = bankManager.currentBank.value ?: return
        val newBank = betManager.resolveBet(betId, result, currentBankValue)
        bankManager.updateCurrentBank(newBank)
    }
    suspend fun getPendingLosses(branchId: Int): List<BetEntity> = betManager.getPendingLosses(branchId)
    suspend fun getBetStatus(bet: BetEntity): String = betManager.getBetStatus(bet)
    suspend fun getActiveBet(branchId: Int): BetEntity? = betManager.getActiveBet(branchId)
    suspend fun hasPendingBet(branchId: Int): Boolean = betManager.hasPendingBet(branchId)
    suspend fun getAllBets(): List<BetEntity> = betManager.getAllBets()
    suspend fun getBetsForBranch(branchId: Int): List<BetEntity> = betManager.getBetsForBranch(branchId)
    suspend fun clearHistory() = betManager.clearHistory()
    suspend fun renameBranch(branchId: Int, newName: String) = branchManager.renameBranch(branchId, newName)
    suspend fun getBankAmount(): Double? = bankManager.getBankAmount()
    suspend fun getActiveBets(): List<BetEntity> = betManager.getActiveBets()
}
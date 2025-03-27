package com.example.betone.viewmodel.bet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.betone.data.dao.BetDao
import com.example.betone.data.entity.BetEntity
import com.example.betone.viewmodel.BetResult
import com.example.betone.viewmodel.branch.BranchManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BetManagerImpl(
    private val betDao: BetDao,
    private val branchManager: BranchManager
) : BetManager {
    private val _currentBetAmount = MutableLiveData<Double>()
    override val currentBetAmount: LiveData<Double> = _currentBetAmount

    override suspend fun calculateBet(branchId: Int, coefficient: Double) {
        Log.d("BetManager", "Calculating bet for branch $branchId with coefficient $coefficient")
        if (coefficient !in 1.75..2.3) {
            Log.e("BetManager", "Invalid coefficient: $coefficient")
            _currentBetAmount.postValue(-1.0)
            return
        }

        val branch = branchManager.getBranch(branchId) ?: run {
            Log.e("BetManager", "Branch $branchId not found")
            return
        }

        val betAmount = if (branch.accumulatedLoss == 0.0) {
            // Если нет накопленных убытков, используем флэт
            Log.d("BetManager", "Using flat amount: ${branch.flatAmount}")
            branch.flatAmount
        } else {
            // Формула для расчета ставки после проигрыша
            // Цель: отыграть предыдущий флэт и получить половину флэта в прибыль
            val targetAmount = branch.accumulatedLoss + branch.flatAmount / 2
            val calculatedAmount = targetAmount / (coefficient - 1)
            Log.d("BetManager", "Calculated amount after loss: $calculatedAmount (target: $targetAmount)")
            calculatedAmount
        }

        _currentBetAmount.postValue(betAmount)
    }

    override suspend fun placeBet(branchId: Int, coefficient: Double, currentBank: Double): Double {
        val betAmount = currentBetAmount.value ?: run {
            Log.e("BetManager", "Bet amount is null, cannot place bet")
            return currentBank
        }

        val bet = BetEntity(
            branchId = branchId,
            coefficient = coefficient,
            amount = betAmount,
            isWin = null,
            timestamp = System.currentTimeMillis()
        )

        withContext(Dispatchers.IO) {
            try {
                Log.d("BetManager", "Attempting to insert bet: $bet")
                betDao.insert(bet)
                Log.d("BetManager", "Bet inserted successfully")

                // Проверяем, что ставка действительно сохранилась
                val savedBet = betDao.getBetById(bet.id)
                Log.d("BetManager", "Retrieved saved bet: $savedBet")

                val betsAfterInsert = betDao.getBetsForBranch(branchId)
                Log.d("BetManager", "All bets for branch $branchId after insert: $betsAfterInsert")
            } catch (e: Exception) {
                Log.e("BetManager", "Error inserting bet", e)
                throw e
            }
        }

        return currentBank - betAmount
    }

    override suspend fun resolveBet(betId: Long, result: BetResult, currentBank: Double): Double {
        Log.d("BetManager", "Resolving bet $betId with result $result")
        val bet = betDao.getBetById(betId) ?: run {
            Log.e("BetManager", "Bet $betId not found")
            return currentBank
        }

        val branch = branchManager.getBranch(bet.branchId) ?: run {
            Log.e("BetManager", "Branch ${bet.branchId} not found")
            return currentBank
        }

        return when (result) {
            BetResult.WIN -> {
                val winAmount = bet.amount * bet.coefficient
                Log.d("BetManager", "Bet won: $winAmount")
                betDao.update(bet.copy(isWin = true))
                branchManager.updateAccumulatedLoss(bet.branchId, 0.0)
                currentBank + winAmount
            }
            BetResult.LOSS -> {
                Log.d("BetManager", "Bet lost: ${bet.amount}")
                betDao.update(bet.copy(isWin = false))
                val newAccumulatedLoss = branch.accumulatedLoss + bet.amount
                branchManager.updateAccumulatedLoss(bet.branchId, newAccumulatedLoss)
                currentBank
            }
            BetResult.RETURN -> {
                Log.d("BetManager", "Bet returned: ${bet.amount}")
                betDao.update(bet.copy(isWin = false))
                currentBank + bet.amount
            }
        }
    }

    override suspend fun getPendingLosses(branchId: Int): List<BetEntity> {
        Log.d("BetManager", "Getting pending losses for branch $branchId")
        val bets = betDao.getBetsForBranch(branchId)
        val firstWinIndex = bets.indexOfFirst { it.isWin == true }
        val pendingLosses = if (firstWinIndex == -1) {
            bets.filter { it.isWin != null }
        } else {
            bets.take(firstWinIndex + 1).filter { it.isWin != null }
        }
        Log.d("BetManager", "Found pending losses: $pendingLosses")
        return pendingLosses
    }

    override suspend fun getBetStatus(bet: BetEntity): String {
        Log.d("BetManager", "Getting status for bet: $bet")
        val bets = betDao.getBetsForBranch(bet.branchId)
        val status = when {
            bet.isWin == true -> "Выигрыш"
            bet.isWin == false && bets.any { it.isWin == true && it.timestamp > bet.timestamp } -> "Возврат"
            else -> "Проигрыш"
        }
        Log.d("BetManager", "Bet status: $status")
        return status
    }

    override suspend fun getActiveBet(branchId: Int): BetEntity? {
        Log.d("BetManager", "Getting active bet for branch $branchId")
        val activeBet = betDao.getBetsForBranch(branchId).firstOrNull { it.isWin == null }
        Log.d("BetManager", "Active bet: $activeBet")
        return activeBet
    }

    override suspend fun hasPendingBet(branchId: Int): Boolean {
        return getActiveBet(branchId) != null
    }

    override suspend fun getAllBets(): List<BetEntity> {
        Log.d("BetManager", "Getting all bets")
        val allBets = betDao.getAllBets()
        Log.d("BetManager", "All bets: $allBets")
        return allBets
    }

    override suspend fun getBetsForBranch(branchId: Int): List<BetEntity> {
        Log.d("BetManager", "Getting bets for branch $branchId")
        val bets = betDao.getBetsForBranch(branchId)
        Log.d("BetManager", "Branch bets: $bets")
        return bets
    }

    override suspend fun getActiveBets(): List<BetEntity> {
        return getAllBets().filter { it.isWin == null }
    }

    override suspend fun clearHistory() {
        Log.d("BetManager", "Clearing history")
        betDao.clearBetsForBranch(1)
        betDao.clearBetsForBranch(2)
        betDao.clearBetsForBranch(3)
        branchManager.updateAccumulatedLoss(1, 0.0)
        branchManager.updateAccumulatedLoss(2, 0.0)
        branchManager.updateAccumulatedLoss(3, 0.0)
    }
}
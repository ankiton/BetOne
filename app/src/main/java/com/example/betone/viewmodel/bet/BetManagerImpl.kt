package com.example.betone.viewmodel.bet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.betone.data.dao.BetDao
import com.example.betone.data.entity.BetEntity
import com.example.betone.viewmodel.BetResult
import com.example.betone.viewmodel.branch.BranchManager

class BetManagerImpl(
    private val betDao: BetDao,
    private val branchManager: BranchManager
) : BetManager {
    private val _currentBetAmount = MutableLiveData<Double>()
    override val currentBetAmount: LiveData<Double> = _currentBetAmount

    override suspend fun calculateBet(branchId: Int, coefficient: Double) {
        if (coefficient !in 1.75..2.3) {
            _currentBetAmount.postValue(-1.0)
            return
        }
        val branch = branchManager.getBranch(branchId) ?: return
        val betAmount = if (branch.accumulatedLoss == 0.0) {
            branch.flatAmount
        } else {
            (branch.accumulatedLoss + branch.flatAmount / 2) / (coefficient - 1)
        }
        _currentBetAmount.postValue(betAmount)
    }

    override suspend fun placeBet(branchId: Int, coefficient: Double, currentBank: Double): Double {
        val betAmount = currentBetAmount.value ?: return currentBank
        betDao.insert(
            BetEntity(
                branchId = branchId,
                coefficient = coefficient,
                amount = betAmount,
                isWin = null,
                timestamp = System.currentTimeMillis()
            )
        )
        return currentBank - betAmount
    }

    override suspend fun resolveBet(betId: Long, result: BetResult, currentBank: Double): Double {
        val bet = betDao.getBetById(betId) ?: return currentBank
        return when (result) {
            BetResult.WIN -> {
                val winAmount = bet.amount * bet.coefficient
                betDao.update(bet.copy(isWin = true))
                branchManager.updateAccumulatedLoss(bet.branchId, 0.0)
                currentBank + winAmount
            }
            BetResult.LOSS -> {
                betDao.update(bet.copy(isWin = false))
                val branch = branchManager.getBranch(bet.branchId) ?: return currentBank
                branchManager.updateAccumulatedLoss(bet.branchId, branch.accumulatedLoss + bet.amount)
                currentBank
            }
            BetResult.RETURN -> {
                betDao.update(bet.copy(isWin = false))
                currentBank + bet.amount
            }
        }
    }

    override suspend fun getPendingLosses(branchId: Int): List<BetEntity> {
        val bets = betDao.getBetsForBranch(branchId)
        val firstWinIndex = bets.indexOfFirst { it.isWin == true }
        return if (firstWinIndex == -1) {
            bets.filter { it.isWin != null }
        } else {
            bets.take(firstWinIndex + 1).filter { it.isWin != null }
        }
    }

    override suspend fun getBetStatus(bet: BetEntity): String {
        val bets = betDao.getBetsForBranch(bet.branchId)
        return when {
            bet.isWin == true -> "Выигрыш"
            bet.isWin == false && bets.any { it.isWin == true && it.timestamp > bet.timestamp } -> "Возврат"
            else -> "Проигрыш"
        }
    }

    override suspend fun getActiveBet(branchId: Int): BetEntity? {
        return betDao.getBetsForBranch(branchId).firstOrNull { it.isWin == null }
    }

    override suspend fun hasPendingBet(branchId: Int): Boolean {
        return getActiveBet(branchId) != null
    }

    override suspend fun getAllBets(): List<BetEntity> {
        return betDao.getBetsForBranch(1) + betDao.getBetsForBranch(2) + betDao.getBetsForBranch(3)
    }

    override suspend fun getBetsForBranch(branchId: Int): List<BetEntity> {
        return betDao.getBetsForBranch(branchId)
    }

    override suspend fun getActiveBets(): List<BetEntity> {
        return getAllBets().filter { it.isWin == null }
    }

    override suspend fun clearHistory() {
        betDao.clearBetsForBranch(1)
        betDao.clearBetsForBranch(2)
        betDao.clearBetsForBranch(3)
        branchManager.updateAccumulatedLoss(1, 0.0)
        branchManager.updateAccumulatedLoss(2, 0.0)
        branchManager.updateAccumulatedLoss(3, 0.0)
    }
}
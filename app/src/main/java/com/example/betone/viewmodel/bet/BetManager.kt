package com.example.betone.viewmodel.bet

import androidx.lifecycle.LiveData
import com.example.betone.data.entity.BetEntity
import com.example.betone.viewmodel.BetResult

interface BetManager {
    val currentBetAmount: LiveData<Double>
    suspend fun calculateBet(branchId: Int, coefficient: Double)
    suspend fun placeBet(branchId: Int, coefficient: Double, currentBank: Double): Double
    suspend fun resolveBet(betId: Long, result: BetResult, currentBank: Double): Double
    suspend fun getPendingLosses(branchId: Int): List<BetEntity>
    suspend fun getBetStatus(bet: BetEntity): String
    suspend fun getActiveBet(branchId: Int): BetEntity?
    suspend fun hasPendingBet(branchId: Int): Boolean
    suspend fun getAllBets(): List<BetEntity>
    suspend fun getBetsForBranch(branchId: Int): List<BetEntity>
    suspend fun getActiveBets(): List<BetEntity>
    suspend fun clearHistory()
}
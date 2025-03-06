package com.example.betone.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.betone.data.entity.BetEntity

@Dao
interface BetDao {
    @Insert
    suspend fun insert(bet: BetEntity)

    @Query("SELECT * FROM bets WHERE branchId = :branchId ORDER BY timestamp DESC")
    suspend fun getBetsForBranch(branchId: Int): List<BetEntity>

    @Query("SELECT * FROM bets WHERE branchId = :branchId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestBetForBranch(branchId: Int): BetEntity?

    @Query("DELETE FROM bets WHERE branchId = :branchId")
    suspend fun clearBetsForBranch(branchId: Int)

    @Query("SELECT COUNT(*) FROM bets")
    suspend fun getBetCount(): Int

    @Query("SELECT SUM(amount) FROM bets WHERE branchId = :branchId AND isWin = 1")
    suspend fun getTotalWinsForBranch(branchId: Int): Double?

    @Query("SELECT SUM(amount) FROM bets WHERE branchId = :branchId AND isWin = 0")
    suspend fun getTotalLossesForBranch(branchId: Int): Double?

    @Query("SELECT * FROM bets WHERE id = :betId")
    suspend fun getBetById(betId: Long): BetEntity?
}
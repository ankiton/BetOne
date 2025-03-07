package com.example.betone.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.betone.data.entity.BetEntity

@Dao
interface BetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bet: BetEntity)

    @Update
    suspend fun update(bet: BetEntity) // Новый метод для обновления

    @Query("SELECT * FROM bets WHERE branchId = :branchId ORDER BY timestamp DESC")
    suspend fun getBetsForBranch(branchId: Int): List<BetEntity>

    @Query("DELETE FROM bets WHERE branchId = :branchId")
    suspend fun clearBetsForBranch(branchId: Int)

    @Query("SELECT * FROM bets WHERE branchId = :branchId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestBetForBranch(branchId: Int): BetEntity?

    @Query("SELECT * FROM bets WHERE id = :betId")
    suspend fun getBetById(betId: Long): BetEntity?
}
package com.example.betone.data.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.betone.data.entity.BetEntity

@Dao
interface BetDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bet: BetEntity) {
        Log.d("BetDao", "Inserting bet: $bet")
        insertInternal(bet)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInternal(bet: BetEntity)

    @Update
    suspend fun update(bet: BetEntity) {
        Log.d("BetDao", "Updating bet: $bet")
        updateInternal(bet)
    }

    @Update
    suspend fun updateInternal(bet: BetEntity)

    @Query("SELECT * FROM bets WHERE branchId = :branchId ORDER BY timestamp DESC")
    suspend fun getBetsForBranch(branchId: Int): List<BetEntity> {
        Log.d("BetDao", "Getting bets for branch $branchId")
        return getBetsForBranchInternal(branchId)
    }

    @Query("SELECT * FROM bets WHERE branchId = :branchId ORDER BY timestamp DESC")
    suspend fun getBetsForBranchInternal(branchId: Int): List<BetEntity>

    @Query("DELETE FROM bets WHERE branchId = :branchId")
    suspend fun clearBetsForBranch(branchId: Int) {
        Log.d("BetDao", "Clearing bets for branch $branchId")
        clearBetsForBranchInternal(branchId)
    }

    @Query("DELETE FROM bets WHERE branchId = :branchId")
    suspend fun clearBetsForBranchInternal(branchId: Int)

    @Query("SELECT * FROM bets WHERE branchId = :branchId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestBetForBranch(branchId: Int): BetEntity? {
        Log.d("BetDao", "Getting latest bet for branch $branchId")
        return getLatestBetForBranchInternal(branchId)
    }

    @Query("SELECT * FROM bets WHERE branchId = :branchId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestBetForBranchInternal(branchId: Int): BetEntity?

    @Query("SELECT * FROM bets WHERE id = :betId")
    suspend fun getBetById(betId: Long): BetEntity? {
        Log.d("BetDao", "Getting bet by id $betId")
        return getBetByIdInternal(betId)
    }

    @Query("SELECT * FROM bets WHERE id = :betId")
    suspend fun getBetByIdInternal(betId: Long): BetEntity?

    @Query("SELECT * FROM bets ORDER BY timestamp DESC")
    suspend fun getAllBets(): List<BetEntity> {
        Log.d("BetDao", "Getting all bets")
        return getAllBetsInternal()
    }

    @Query("SELECT * FROM bets ORDER BY timestamp DESC")
    suspend fun getAllBetsInternal(): List<BetEntity>
}
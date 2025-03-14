package com.example.betone.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.betone.data.entity.BetBranchEntity

@Dao
interface BranchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(branch: BetBranchEntity)

    @Query("SELECT * FROM branches WHERE branchId = :branchId")
    suspend fun getBranch(branchId: Int): BetBranchEntity?

    @Query("SELECT * FROM branches")
    suspend fun getAllBranches(): List<BetBranchEntity>

    @Query("UPDATE branches SET accumulatedLoss = :amount WHERE branchId = :branchId")
    suspend fun updateAccumulatedLoss(branchId: Int, amount: Double)

    @Query("DELETE FROM branches")
    suspend fun clearBranches()
}
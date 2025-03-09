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

    @Query("SELECT * FROM bet_branches WHERE branchId = :branchId")
    suspend fun getBranch(branchId: Int): BetBranchEntity?

    @Query("UPDATE bet_branches SET accumulatedLoss = :loss WHERE branchId = :branchId")
    suspend fun updateAccumulatedLoss(branchId: Int, loss: Double)

    @Query("SELECT * FROM bet_branches")
    suspend fun getAllBranches(): List<BetBranchEntity>

    @Query("DELETE FROM bet_branches")
    suspend fun clearBranches()
}
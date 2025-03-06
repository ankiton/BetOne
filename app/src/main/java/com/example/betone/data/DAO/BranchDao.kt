package com.example.betone.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.betone.data.entity.BetBranchEntity

@Dao
interface BranchDao {
    // Вставка ветки (при инициализации)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(branch: BetBranchEntity)

    // Получение данных о ветке по её ID
    @Query("SELECT * FROM bet_branches WHERE branchId = :branchId")
    suspend fun getBranch(branchId: Int): BetBranchEntity?

    // Обновление накопленных проигрышей
    @Query("UPDATE bet_branches SET accumulatedLoss = :loss WHERE branchId = :branchId")
    suspend fun updateAccumulatedLoss(branchId: Int, loss: Double)

    // Получение всех веток (для проверки или отладки)
    @Query("SELECT * FROM bet_branches")
    suspend fun getAllBranches(): List<BetBranchEntity>

    // Очистка веток (для сброса)
    @Query("DELETE FROM bet_branches")
    suspend fun clearBranches()
}
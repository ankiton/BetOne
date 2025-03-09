package com.example.betone.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.betone.data.entity.BankEntity

@Dao
interface BankDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bank: BankEntity)

    @Query("SELECT * FROM bank ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestBank(): BankEntity?

    @Query("SELECT * FROM bank ORDER BY startDate DESC")
    suspend fun getAllBanks(): List<BankEntity>


    @Query("DELETE FROM bank")
    suspend fun clearBank()
}
package com.example.betone.data


import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.betone.data.dao.BankDao
import com.example.betone.data.dao.BetDao
import com.example.betone.data.dao.BranchDao
import com.example.betone.data.entity.BankEntity
import com.example.betone.data.entity.BetBranchEntity
import com.example.betone.data.entity.BetEntity


@Database(entities = [BankEntity::class, BetBranchEntity::class, BetEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao
    abstract fun branchDao(): BranchDao
    abstract fun betDao(): BetDao

    companion object {
        private const val TAG = "AppDatabase"
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            Log.d(TAG, "Attempting to get database instance")
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Creating new database instance")
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "betting_db"
                    ).build()
                    Log.d(TAG, "Database instance created successfully")
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create database", e)
                    throw e
                }
            }
        }
    }

    suspend fun clearAll() {
        bankDao().clearBank()
        branchDao().clearBranches()
        betDao().clearBetsForBranch(1)
        betDao().clearBetsForBranch(2)
        betDao().clearBetsForBranch(3)
    }

    suspend fun initializeDefaultData() {
        val bankDao = bankDao()
        val branchDao = branchDao()
        if (bankDao.getLatestBank() == null) {
            bankDao.insert(BankEntity(amount = 10000.0, startDate = System.currentTimeMillis()))
            branchDao.insert(BetBranchEntity(branchId = 1, flatAmount = 100.0))
            branchDao.insert(BetBranchEntity(branchId = 2, flatAmount = 100.0))
            branchDao.insert(BetBranchEntity(branchId = 3, flatAmount = 100.0))
        }
    }
}
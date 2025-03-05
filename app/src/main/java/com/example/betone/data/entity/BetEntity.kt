package com.example.betone.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bets")
data class BetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val branchId: Int,
    val coefficient: Double,
    val amount: Double,
    val isWin: Boolean,
    val timestamp: Long
)
package com.example.betone.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bet_branches")
data class BetBranchEntity(
    @PrimaryKey val branchId: Int, // 1, 2 или 3
    val flatAmount: Double,
    val accumulatedLoss: Double = 0.0
)

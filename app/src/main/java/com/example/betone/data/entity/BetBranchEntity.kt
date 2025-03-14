package com.example.betone.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "branches")
data class BetBranchEntity(
    @PrimaryKey val branchId: Int,
    val flatAmount: Double,
    val accumulatedLoss: Double = 0.0,
    val name: String = "Ветка $branchId" // Значение по умолчанию
)

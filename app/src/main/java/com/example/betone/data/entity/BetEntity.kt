package com.example.betone.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "bets",
    foreignKeys = [
        ForeignKey(
            entity = BetBranchEntity::class,
            parentColumns = ["branchId"],
            childColumns = ["branchId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class BetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val branchId: Int,
    val coefficient: Double,
    val amount: Double,
    val isWin: Boolean?,
    val timestamp: Long
)
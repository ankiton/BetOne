package com.example.betone.viewmodel.branch

import androidx.lifecycle.LiveData
import com.example.betone.data.entity.BetBranchEntity

interface BranchManager {
    val branchNames: LiveData<Map<Int, String>>
    suspend fun getBranch(branchId: Int): BetBranchEntity?
    suspend fun updateAccumulatedLoss(branchId: Int, amount: Double)
    suspend fun initializeBranches(bankAmount: Double)
    suspend fun recalculateFlat(bankAmount: Double)
    suspend fun renameBranch(branchId: Int, newName: String)
}
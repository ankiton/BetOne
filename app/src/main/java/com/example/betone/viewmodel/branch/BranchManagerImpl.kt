package com.example.betone.viewmodel.branch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.betone.data.dao.BranchDao
import com.example.betone.data.entity.BetBranchEntity

class BranchManagerImpl(
    private val branchDao: BranchDao
) : BranchManager {
    private val _branchNames = MutableLiveData<Map<Int, String>>(mapOf(1 to "Ветка 1", 2 to "Ветка 2", 3 to "Ветка 3"))
    override val branchNames: LiveData<Map<Int, String>> = _branchNames

    override suspend fun getBranch(branchId: Int): BetBranchEntity? {
        return branchDao.getBranch(branchId)
    }

    override suspend fun updateAccumulatedLoss(branchId: Int, amount: Double) {
        val branch = branchDao.getBranch(branchId) ?: return
        branchDao.insert(branch.copy(accumulatedLoss = amount))
    }

    override suspend fun initializeBranches(bankAmount: Double) {
        val flatAmount = bankAmount * 0.01
        listOf(1, 2, 3).forEach { branchId ->
            branchDao.insert(BetBranchEntity(branchId, flatAmount = flatAmount))
        }
    }

    override suspend fun recalculateFlat(bankAmount: Double) {
        val newFlatAmount = bankAmount * 0.01
        listOf(1, 2, 3).forEach { branchId ->
            val branch = branchDao.getBranch(branchId)
            if (branch != null) {
                branchDao.insert(BetBranchEntity(branchId, flatAmount = newFlatAmount, accumulatedLoss = branch.accumulatedLoss))
            }
        }
    }

    override fun renameBranch(branchId: Int, newName: String) {
        val currentNames = _branchNames.value ?: emptyMap()
        _branchNames.postValue(currentNames + (branchId to newName))
    }
}
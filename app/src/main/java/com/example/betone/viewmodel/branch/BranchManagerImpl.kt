package com.example.betone.viewmodel.branch

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.betone.data.dao.BranchDao
import com.example.betone.data.entity.BetBranchEntity

class BranchManagerImpl(
    private val branchDao: BranchDao
) : BranchManager {
    private val _branchNames = MutableLiveData<Map<Int, String>>()
    override val branchNames: LiveData<Map<Int, String>> = _branchNames

    init {
        kotlinx.coroutines.runBlocking {
            val branches = branchDao.getAllBranches()
            val namesMap = branches.associate { it.branchId to it.name }
            _branchNames.postValue(namesMap)
        }
    }

    override suspend fun getBranch(branchId: Int): BetBranchEntity? {
        Log.d("BranchManager", "Getting branch $branchId")
        return branchDao.getBranch(branchId)
    }

    override suspend fun updateAccumulatedLoss(branchId: Int, amount: Double) {
        Log.d("BranchManager", "Updating accumulated loss for branch $branchId to $amount")
        val branch = branchDao.getBranch(branchId) ?: return
        val updatedBranch = branch.copy(accumulatedLoss = amount)
        branchDao.insert(updatedBranch)
        Log.d("BranchManager", "Updated branch: $updatedBranch")
    }

    override suspend fun initializeBranches(bankAmount: Double) {
        Log.d("BranchManager", "Initializing branches with bank amount: $bankAmount")
        val flatAmount = bankAmount * 0.01
        val defaultNames = mapOf(1 to "Ветка 1", 2 to "Ветка 2", 3 to "Ветка 3")
        listOf(1, 2, 3).forEach { branchId ->
            val existingBranch = branchDao.getBranch(branchId)
            if (existingBranch == null) {
                val newBranch = BetBranchEntity(
                    branchId = branchId,
                    flatAmount = flatAmount,
                    name = defaultNames[branchId] ?: "Ветка $branchId"
                )
                Log.d("BranchManager", "Creating new branch: $newBranch")
                branchDao.insert(newBranch)
            } else {
                val updatedBranch = existingBranch.copy(flatAmount = flatAmount)
                Log.d("BranchManager", "Updating existing branch: $updatedBranch")
                branchDao.insert(updatedBranch)
            }
        }
        updateBranchNames()
    }

    override suspend fun recalculateFlat(bankAmount: Double) {
        Log.d("BranchManager", "Recalculating flat amount for bank: $bankAmount")
        val newFlatAmount = bankAmount * 0.01
        listOf(1, 2, 3).forEach { branchId ->
            val branch = branchDao.getBranch(branchId)
            if (branch != null) {
                val updatedBranch = branch.copy(flatAmount = newFlatAmount)
                Log.d("BranchManager", "Updating branch $branchId flat amount to $newFlatAmount")
                branchDao.insert(updatedBranch)
            }
        }
        updateBranchNames()
    }

    override suspend fun renameBranch(branchId: Int, newName: String) {
        Log.d("BranchManager", "Renaming branch $branchId to $newName")
        val branch = branchDao.getBranch(branchId)
        if (branch != null) {
            val updatedBranch = branch.copy(name = newName)
            branchDao.insert(updatedBranch)
            updateBranchNames()
        }
    }

    private suspend fun updateBranchNames() {
        val branches = branchDao.getAllBranches()
        val namesMap = branches.associate { it.branchId to it.name }
        _branchNames.postValue(namesMap)
        Log.d("BranchManager", "Updated branch names: $namesMap")
    }
}
package com.example.betone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.betone.data.dao.BankDao
import com.example.betone.data.entity.BankEntity
import com.example.betone.viewmodel.branch.BranchManager

class BankManagerImpl(
    private val bankDao: BankDao,
    private val branchManager: BranchManager
) : BankManager {
    private val _currentBank = MutableLiveData<Double>()
    override val currentBank: LiveData<Double> = _currentBank

    override suspend fun initBank(amount: Double) {
        val bank = BankEntity(amount = amount, startDate = System.currentTimeMillis())
        bankDao.insert(bank)
        branchManager.initializeBranches(amount)
        _currentBank.postValue(amount)
    }

    override suspend fun isBankInitialized(): Boolean {
        val bank = bankDao.getLatestBank()
        if (bank != null) _currentBank.postValue(bank.amount)
        return bank != null
    }

    override suspend fun updateBank(amount: Double) {
        val latestBank = bankDao.getLatestBank()
        if (latestBank != null) {
            bankDao.insert(BankEntity(id = latestBank.id, amount = amount, startDate = latestBank.startDate))
            branchManager.recalculateFlat(amount)
            _currentBank.postValue(amount)
        } else {
            initBank(amount)
        }
    }

    override suspend fun getBankAmount(): Double? {
        return bankDao.getLatestBank()?.amount
    }

    override fun updateCurrentBank(amount: Double) {
        _currentBank.postValue(amount)
    }
}


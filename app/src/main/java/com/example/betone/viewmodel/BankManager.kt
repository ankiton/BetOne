package com.example.betone.viewmodel

import androidx.lifecycle.LiveData

interface BankManager {
    val currentBank: LiveData<Double>
    suspend fun initBank(amount: Double)
    suspend fun isBankInitialized(): Boolean
    suspend fun updateBank(amount: Double)
    suspend fun getBankAmount(): Double?
    fun updateCurrentBank(amount: Double)
}
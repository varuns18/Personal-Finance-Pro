package com.ramphal.personalfinancepro.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramphal.personalfinancepro.Graph
import com.ramphal.personalfinancepro.data.OnboardingModel
import com.ramphal.personalfinancepro.data.TransactionModel
import com.ramphal.personalfinancepro.data.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AddTransactionViewModel: ViewModel(){

    private val transactionRepository: TransactionRepository = Graph.transactionRepository

    fun addTransaction(transactionModel: TransactionModel){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.addTransaction(transactionModel)
        }
    }

    fun updateTransaction(transactionModel: TransactionModel){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.updateTransaction(transactionModel)
        }
    }

    fun updateOverallBalance(onboardingModel: OnboardingModel){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.updateOnboardingData(onboardingModel)
        }
    }

    fun getTransactionById(transactionId: Long): Flow<TransactionModel> {
        return transactionRepository.getTransactionById(transactionId)
    }

    fun getOverallBalance(): Flow<OnboardingModel>{
        return transactionRepository.getOnboardingData(0)
    }

}
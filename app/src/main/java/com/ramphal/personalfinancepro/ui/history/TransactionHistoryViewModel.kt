package com.ramphal.personalfinancepro.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramphal.personalfinancepro.Graph
import com.ramphal.personalfinancepro.data.OnboardingModel
import com.ramphal.personalfinancepro.data.TransactionModel
import com.ramphal.personalfinancepro.data.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionHistoryViewModel: ViewModel() {
    private val transactionRepository: TransactionRepository = Graph.transactionRepository
    lateinit var getAllTransaction: Flow<List<TransactionModel>>
    lateinit var detailOverview: Flow<OnboardingModel>


    init {
        viewModelScope.launch {
            getAllTransaction = transactionRepository.getAllTransactions()
            detailOverview = Graph.transactionRepository.getOnboardingData(0)
        }
    }

    fun deleteTransaction(transactionModel: TransactionModel){
        viewModelScope.launch(Dispatchers.IO) {
            Graph.transactionRepository.deleteTransaction(transactionModel)
        }
    }

    fun updateOverallBalance(onboardingModel: OnboardingModel){
        viewModelScope.launch(Dispatchers.IO) {
            Graph.transactionRepository.updateOnboardingData(onboardingModel)
        }
    }
}
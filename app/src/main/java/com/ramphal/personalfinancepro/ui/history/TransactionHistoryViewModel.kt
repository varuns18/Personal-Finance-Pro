package com.ramphal.personalfinancepro.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramphal.personalfinancepro.Graph
import com.ramphal.personalfinancepro.data.TransactionModel
import com.ramphal.personalfinancepro.data.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionHistoryViewModel: ViewModel() {
    private val transactionRepository: TransactionRepository = Graph.transactionRepository
    lateinit var getAllTransaction: Flow<List<TransactionModel>>


    init {
        viewModelScope.launch {
            getAllTransaction = transactionRepository.getAllTransactions()
        }
    }
}
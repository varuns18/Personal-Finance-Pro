package com.ramphal.personalfinancepro.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramphal.personalfinancepro.Graph
import com.ramphal.personalfinancepro.data.TransactionModel
import com.ramphal.personalfinancepro.data.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddTransactionViewModel: ViewModel(){

    private val transactionRepository: TransactionRepository = Graph.transactionRepository

    fun addTransaction(transactionModel: TransactionModel){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.addTransaction(transactionModel)
        }
    }

}
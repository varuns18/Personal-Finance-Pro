package com.ramphal.personalfinancepro.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramphal.personalfinancepro.Graph
import com.ramphal.personalfinancepro.Graph.transactionRepository
import com.ramphal.personalfinancepro.data.TransactionModel
import com.ramphal.personalfinancepro.data.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomePageViewModel: ViewModel() {

    lateinit var getThisMonthTransactions: Flow<List<TransactionModel>>


    init {
        viewModelScope.launch {
            getThisMonthTransactions = transactionRepository.getThisMonthTransactions()
        }
    }

    val totalBalance: StateFlow<Double> =
        transactionRepository.totalBalance
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val thisMonthIncome: StateFlow<Double> =
        transactionRepository.thisMonthIncome
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val thisMonthExpense: StateFlow<Double> =
        transactionRepository.thisMonthExpense
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)


}

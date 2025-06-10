package com.ramphal.personalfinancepro.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramphal.personalfinancepro.Graph.transactionRepository
import com.ramphal.personalfinancepro.data.OnboardingModel
import com.ramphal.personalfinancepro.data.TransactionModel
import com.ramphal.personalfinancepro.ui.add.updateBalanceForAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class HomePageViewModel: ViewModel() {

    lateinit var getThisMonthTransactions: Flow<List<TransactionModel>>
    lateinit var getAllTransaction: Flow<List<TransactionModel>>
    lateinit var detailOverview: Flow<OnboardingModel>

    init {
        viewModelScope.launch {
            getThisMonthTransactions = transactionRepository.getThisMonthTransactions()
            getAllTransaction = transactionRepository.getAllTransactions()
            detailOverview = transactionRepository.getOnboardingData(0)
            processDueScheduledTransactions()
        }
    }

    fun deleteTransaction(transactionModel: TransactionModel){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.deleteTransaction(transactionModel)
        }
    }

    fun updateOverallBalance(onboardingModel: OnboardingModel){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.updateOnboardingData(onboardingModel)
        }
    }

    private fun processDueScheduledTransactions() {
        viewModelScope.launch {
            try {
                val dueTransactions = transactionRepository
                    .getCompletedScheduledTransactions(LocalDateTime.now())
                    .firstOrNull()

                if (!dueTransactions.isNullOrEmpty()) {
                    val currentOverallBalance = detailOverview.firstOrNull()
                    currentOverallBalance?.let { onboarding ->
                        var updatedOnboarding = onboarding.copy()
                        for (transaction in dueTransactions) {
                            when (transaction.type) {
                                "Expense" -> {
                                    updatedOnboarding = updateBalanceForAccount(
                                        currentOnboarding = updatedOnboarding,
                                        accountIndex = transaction.from,
                                        amount = (transaction.amount.toDouble() * -1)
                                    )
                                }
                                "Income" -> {
                                    updatedOnboarding = updateBalanceForAccount(
                                        currentOnboarding = updatedOnboarding,
                                        accountIndex = transaction.to, // For income, money goes to 'to' account
                                        amount = transaction.amount.toDouble() // Add the amount
                                    )
                                }
                                "Transfer" -> {
                                    updatedOnboarding = updateBalanceForAccount(
                                        currentOnboarding = updatedOnboarding,
                                        accountIndex = transaction.from,
                                        amount = (transaction.amount.toDouble() * -1)
                                    )
                                    updatedOnboarding = updateBalanceForAccount(
                                        currentOnboarding = updatedOnboarding,
                                        accountIndex = transaction.to,
                                        amount = transaction.amount.toDouble()
                                    )
                                }
                            }
                            val completedTransaction = transaction.copy(isScheduled = false)
                            transactionRepository.updateTransaction(completedTransaction)
                        }
                        transactionRepository.updateOnboardingData(updatedOnboarding)
                    }
                } else {
                    Log.d("HomePageViewModel", "No due scheduled transactions found on ViewModel load.")
                }
            } catch (e: Exception) {
                Log.e("HomePageViewModel", "Error processing due scheduled transactions: ${e.message}", e)
            }
        }
    }

    val thisMonthIncome: StateFlow<Double> =
        transactionRepository.thisMonthIncome
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val thisMonthExpense: StateFlow<Double> =
        transactionRepository.thisMonthExpense
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)


}

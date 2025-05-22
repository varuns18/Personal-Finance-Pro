package com.ramphal.personalfinancepro.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun addTransaction(transactionModel: TransactionModel){
        transactionDao.addTransaction(transactionModel)
    }

    fun getAllTransactions(): Flow<List<TransactionModel>> = transactionDao.getAllTransactions()

    fun getThisMonthTransactions(): Flow<List<TransactionModel>> = transactionDao.getThisMonthTransactions()

    fun getTransactionById(id: Long): Flow<TransactionModel>{
        return transactionDao.getTransactionById(id = id)
    }

    suspend fun updateTransaction(transactionModel: TransactionModel){
        transactionDao.updateTransaction(transactionModel)
    }

    suspend fun deleteTransaction(transactionModel: TransactionModel){
        transactionDao.deleteTransaction(transactionModel)
    }

    val totalBalance: Flow<Double>        = transactionDao.getTotalBalance()
    val thisMonthIncome: Flow<Double>     = transactionDao.getThisMonthIncome()
    val thisMonthExpense: Flow<Double>    = transactionDao.getThisMonthExpense()

}
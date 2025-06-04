package com.ramphal.personalfinancepro.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.ZoneOffset

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

    fun getCategorySpendingInDateRange(
        categoryIndex: Int,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<Double> {
        // Convert LocalDateTime to Unix timestamp in milliseconds
        val startDateMillis = startDate.toInstant(ZoneOffset.UTC).toEpochMilli()
        val endDateMillis = endDate.toInstant(ZoneOffset.UTC).toEpochMilli()
        return transactionDao.getCategorySpendingInDateRange(categoryIndex, startDateMillis, endDateMillis)
    }

    fun getCategoryIncomeInDateRange(
        categoryIndex: Int,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<Double> {
        val startDateMillis = startDate.toInstant(ZoneOffset.UTC).toEpochMilli()
        val endDateMillis = endDate.toInstant(ZoneOffset.UTC).toEpochMilli()
        return transactionDao.getCategoryIncomeInDateRange(categoryIndex, startDateMillis, endDateMillis)
    }

    fun getOverallInDateRange(
        category: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<Double> {
        val startDateMillis = startDate.toInstant(ZoneOffset.UTC).toEpochMilli()
        val endDateMillis = endDate.toInstant(ZoneOffset.UTC).toEpochMilli()
        return transactionDao.getOverallInDateRange(category, startDateMillis, endDateMillis)
    }
}
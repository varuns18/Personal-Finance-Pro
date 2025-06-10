package com.ramphal.personalfinancepro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TransactionDao {

    @Insert(onConflict = IGNORE)
    abstract suspend fun addTransaction(transactionModel: TransactionModel)

    @Insert(onConflict = IGNORE)
    abstract suspend fun addOnboardingData(onboardingModel: OnboardingModel)

    @Delete
    abstract suspend fun deleteTransaction(transactionModel: TransactionModel)

    @Query(value = "Select * from `Transactions-Table`")
    abstract fun getAllTransactions(): Flow<List<TransactionModel>>

    @Query("SELECT * FROM 'Transactions-Table' WHERE isScheduled = 1 AND timestamp <= :currentMillis")
    abstract fun getCompletedScheduledTransactions(currentMillis: Long): Flow<List<TransactionModel>>

    @Query("""
        SELECT IFNULL(SUM(CAST(amount AS REAL)), 0.0)
        FROM `Transactions-Table`
        WHERE type = 'Expense'
        AND strftime('%Y-%m-%d', datetime(timestamp / 1000, 'unixepoch', 'localtime')) = strftime('%Y-%m-%d', 'now', 'localtime')
    """)
    abstract fun getTodaysTotalExpense(): Flow<Double>

    @Query(value = "Select * from `Transactions-Table` where id=:id")
    abstract fun getTransactionById(id: Long): Flow<TransactionModel>

    @Query(value = "Select * from `Onboarding-Table` where id=:id")
    abstract fun getOnboardingData(id: Int): Flow<OnboardingModel>

    @Update(onConflict = IGNORE)
    abstract suspend fun updateTransaction(transactionModel: TransactionModel)

    @Update(onConflict = IGNORE)
    abstract suspend fun updateOnboardingData(onboardingModel: OnboardingModel)

    @Query("""
  SELECT IFNULL(SUM(
    CASE
      WHEN type = 'Income' THEN CAST(amount AS REAL)
      WHEN type = 'Expense' THEN -CAST(amount AS REAL)
      ELSE 0
    END
  ), 0)
  FROM `Transactions-Table`
  WHERE timestamp <= strftime('%s', 'now') * 1000
""")
    abstract fun getTotalBalance(): Flow<Double>


    // 2. This month’s total income
    @Query("""
  SELECT IFNULL(SUM(CAST(amount AS REAL)), 0)
  FROM `Transactions-Table`
  WHERE type = 'Income'
    AND timestamp <= strftime('%s', 'now') * 1000
    AND strftime(
          '%Y-%m',
          datetime(timestamp / 1000, 'unixepoch', 'localtime')
        ) = strftime('%Y-%m', 'now', 'localtime')
""")
    abstract fun getThisMonthIncome(): Flow<Double>


    // 3. This month’s total expense
    @Query("""
  SELECT IFNULL(SUM(CAST(amount AS REAL)), 0)
  FROM `Transactions-Table`
  WHERE type = 'Expense'
    AND timestamp <= strftime('%s', 'now') * 1000
    AND strftime(
          '%Y-%m',
          datetime(timestamp / 1000, 'unixepoch', 'localtime')
        ) = strftime('%Y-%m', 'now', 'localtime')
""")
    abstract fun getThisMonthExpense(): Flow<Double>


    @Query("""
    SELECT *
    FROM `Transactions-Table`
    WHERE strftime(
            '%Y-%m',
            datetime(timestamp / 1000, 'unixepoch', 'localtime')
          )
      = strftime('%Y-%m', 'now', 'localtime')
    ORDER BY timestamp DESC
  """)
    abstract fun getThisMonthTransactions(): Flow<List<TransactionModel>>


    @Query("""
        SELECT IFNULL(SUM(CAST(amount AS REAL)), 0)
        FROM `Transactions-Table`
        WHERE type = 'Expense'
          AND `to` = :categoryIndex
          AND timestamp >= :startDateMillis
          AND timestamp <= :endDateMillis
    """)
    abstract fun getCategorySpendingInDateRange(
        categoryIndex: Int,
        startDateMillis: Long,
        endDateMillis: Long
    ): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(CAST(amount AS REAL)), 0)
        FROM `Transactions-Table`
        WHERE type = 'Income'
          AND `from` = :categoryIndex
          AND timestamp >= :startDateMillis
          AND timestamp <= :endDateMillis
    """)
    abstract fun getCategoryIncomeInDateRange(
        categoryIndex: Int,
        startDateMillis: Long,
        endDateMillis: Long
    ): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(CAST(amount AS REAL)), 0)
        FROM `Transactions-Table`
        WHERE type = :category
          AND timestamp >= :startDateMillis
          AND timestamp <= :endDateMillis
    """)
    abstract fun getOverallInDateRange(
        category: String,
        startDateMillis: Long,
        endDateMillis: Long
    ): Flow<Double>

}
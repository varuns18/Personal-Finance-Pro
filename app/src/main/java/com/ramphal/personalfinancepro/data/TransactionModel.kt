package com.ramphal.personalfinancepro.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "Transactions-Table")
data class TransactionModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val from: Int,
    val to: Int,
    val timestamp: LocalDateTime,
    val amount: String,
    val note: String? = null,
    val isScheduled: Boolean = false
)

@Entity(tableName = "Onboarding-Table")
data class OnboardingModel(
    @PrimaryKey
    val id: Long = 0,
    val preferredCurrencySymbol: String,
    val bankBalance: Double,
    val cashBalance: Double,
    val savingsBalance: Double,
    val hasCreditCard: Boolean = false,
    val creditCardBalance: Double = 0.0,
)

data class CategoryItem(
    val icon: Int,
    val name: String
)


data class AccountItem(
    val icon: Int,
    val name: String,
    val sign: String,
    val currency: String,
    val balance: String,
)
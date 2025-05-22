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
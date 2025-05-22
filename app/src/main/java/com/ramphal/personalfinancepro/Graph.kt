package com.ramphal.personalfinancepro

import android.content.Context
import androidx.room.Room
import com.ramphal.personalfinancepro.data.TransactionDataBase
import com.ramphal.personalfinancepro.data.TransactionRepository
import kotlin.jvm.java

object Graph {
    lateinit var dataBase: TransactionDataBase

    val transactionRepository by lazy {
        TransactionRepository(dataBase.TransactionDao())
    }


    fun provide(context: Context){
        dataBase = Room.databaseBuilder(
            context,
            TransactionDataBase::class.java,
            "transaction_database.db"
        ).build()
    }

}
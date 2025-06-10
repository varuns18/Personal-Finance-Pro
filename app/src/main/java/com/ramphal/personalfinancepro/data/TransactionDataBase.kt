package com.ramphal.personalfinancepro.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TransactionModel::class, OnboardingModel::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TransactionDataBase: RoomDatabase(){

    abstract fun TransactionDao(): TransactionDao

}
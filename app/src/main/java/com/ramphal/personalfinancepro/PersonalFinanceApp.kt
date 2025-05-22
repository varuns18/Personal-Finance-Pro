package com.ramphal.personalfinancepro

import android.app.Application

class PersonalFinanceApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(context = this)
    }

}
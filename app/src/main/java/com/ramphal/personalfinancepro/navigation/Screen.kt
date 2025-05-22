package com.ramphal.personalfinancepro.navigation

sealed class Screen(val route: String) {
    object homePage: Screen(route = "HomePageView")
    object addTransaction: Screen(route = "AddTransactionView")
    object transactionHistory: Screen(route = "TransactionHistoryView")
    object navigationBarM3: Screen(route = "NavigationBarM3")
}
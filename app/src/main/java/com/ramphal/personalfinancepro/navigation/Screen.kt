package com.ramphal.personalfinancepro.navigation

sealed class Screen(val route: String) {
    object splash: Screen(route = "SplashScreen")
    object homePage: Screen(route = "HomePageView")
    object addTransaction: Screen(route = "AddTransactionView")
    object transactionHistory: Screen(route = "TransactionHistoryView")
    object chartPage: Screen(route = "GraphPageView")
    object settings: Screen(route = "SettingsView")
    object onboarding: Screen(route = "OnboardingView")
}
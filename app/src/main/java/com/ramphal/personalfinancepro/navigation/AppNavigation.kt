package com.ramphal.personalfinancepro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ramphal.personalfinancepro.ui.NavigationBarM3
import com.ramphal.personalfinancepro.ui.add.AddTransactionView
import com.ramphal.personalfinancepro.ui.add.AddTransactionViewModel
import com.ramphal.personalfinancepro.ui.history.TransactionHistoryView
import com.ramphal.personalfinancepro.ui.history.TransactionHistoryViewModel
import com.ramphal.personalfinancepro.ui.home.HomePageView
import com.ramphal.personalfinancepro.ui.home.HomePageViewModel

@Composable
fun AppNavigation(
    homePageViewModel: HomePageViewModel = viewModel<HomePageViewModel>(),
    addTransactionViewModel: AddTransactionViewModel = viewModel<AddTransactionViewModel>(),
    transactionHistoryViewModel: TransactionHistoryViewModel = viewModel<TransactionHistoryViewModel>(),
    navController: NavHostController = rememberNavController(),
){
    val scope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = Screen.navigationBarM3.route
    ){
        composable(route = Screen.navigationBarM3.route) {
            NavigationBarM3(
                navController = navController,
                homePageViewModel = homePageViewModel,
                addTransactionViewModel = addTransactionViewModel,
                transactionHistoryViewModel = transactionHistoryViewModel,
                scope = scope
            )
        }
        composable(route = Screen.addTransaction.route) {
            AddTransactionView(navController = navController, viewModel = addTransactionViewModel, scope = scope)
        }
        composable(route = Screen.transactionHistory.route) {
            TransactionHistoryView(
                viewModel = transactionHistoryViewModel,
                navController = navController,
            )
        }
//        composable(route = Screen.addTransaction.route + "/{id}",
//            arguments = listOf(
//                navArgument(name = "id") {
//                    type = NavType.LongType
//                    defaultValue = 0L
//                    nullable = false
//                }
//            )
//        ) {backStackEntry ->
//            val id = if(backStackEntry.arguments != null) backStackEntry.arguments!!.getLong("id") else 0L
//            AddTransactionView(id = id, viewModel = addTransactionViewModel, navController = navController)
//        }
    }
}
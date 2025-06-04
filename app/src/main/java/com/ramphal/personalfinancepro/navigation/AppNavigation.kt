package com.ramphal.personalfinancepro.navigation

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ramphal.personalfinancepro.R
import com.ramphal.personalfinancepro.ui.add.AddTransactionView
import com.ramphal.personalfinancepro.ui.add.AddTransactionViewModel
import com.ramphal.personalfinancepro.ui.analytics.GraphPageView
import com.ramphal.personalfinancepro.ui.analytics.GraphPageViewModel
import com.ramphal.personalfinancepro.ui.history.TransactionHistoryView
import com.ramphal.personalfinancepro.ui.history.TransactionHistoryViewModel
import com.ramphal.personalfinancepro.ui.home.ExtendedFABM3
import com.ramphal.personalfinancepro.ui.home.HomePageView
import com.ramphal.personalfinancepro.ui.home.HomePageViewModel
import com.ramphal.personalfinancepro.ui.settings.SettingsView
import com.ramphal.personalfinancepro.ui.settings.SettingsViewModel
import androidx.compose.runtime.collectAsState // Import collectAsState
import com.ramphal.personalfinancepro.ui.settings.AmountFormattingSettings
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    // Keep other ViewModels as they are, or if you prefer to instantiate them centrally, do so.
    homePageViewModel: HomePageViewModel = viewModel<HomePageViewModel>(),
    addTransactionViewModel: AddTransactionViewModel = viewModel<AddTransactionViewModel>(),
    transactionHistoryViewModel: TransactionHistoryViewModel = viewModel<TransactionHistoryViewModel>(),
    graphPageViewModel: GraphPageViewModel = viewModel<GraphPageViewModel>(),
    navController: NavHostController = rememberNavController(),
){
    val scope = rememberCoroutineScope()
    var selectedItem: Int by remember { mutableIntStateOf(0) }
    var bottomBarVisibility by remember { mutableStateOf(true) }

    // --- Instantiate SettingsViewModel and collect its state once here ---
    val application = LocalContext.current.applicationContext as Application
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(application)
    )

    // Collect the individual StateFlows as Compose State
    val currentCurrencyCode by settingsViewModel.currentCurrencyCode.collectAsState()
    val currentAmountFormat by settingsViewModel.currentAmountFormat.collectAsState()
    val currentDateFormat by settingsViewModel.currentDateFormatPattern.collectAsState()

    // Access the fixed values directly from the ViewModel instance
    val fixedDecimalPlaces = settingsViewModel.fixedDecimalPlaces
    val fixedCurrencyPosition = settingsViewModel.fixedCurrencyPosition
    val snackbarHostState = remember { SnackbarHostState() }


    // Create the AmountFormattingSettings object to pass around
    val amountFormattingSettings = remember(currentCurrencyCode, currentAmountFormat) { // Remember for stability
        AmountFormattingSettings(
            preferredCurrencyCode = currentCurrencyCode,
            currencyPosition = fixedCurrencyPosition,
            amountFormatType = currentAmountFormat,
            decimalPlaces = fixedDecimalPlaces
        )
    }
    // --- End SettingsViewModel setup ---


    val barItems = listOf(
        BarItem(
            title = "Home",
            selectedIcon = ImageVector.vectorResource(R.drawable.home_filled_24px),
            unselectedIcon = ImageVector.vectorResource(R.drawable.home_outline_24px),
            route = Screen.homePage.route
        ),
        BarItem(
            title = "Analytics",
            selectedIcon = ImageVector.vectorResource(R.drawable.ic_pie_chart_filled_24px),
            unselectedIcon = ImageVector.vectorResource(R.drawable.ic_pie_chart_outline_24px),
            route = Screen.chartPage.route
        ),
        BarItem(
            title = "Records",
            selectedIcon = ImageVector.vectorResource(R.drawable.ic_receipt_filled_24px),
            unselectedIcon = ImageVector.vectorResource(R.drawable.ic_receipt_outline_24px),
            route = Screen.transactionHistory.route
        ),
        BarItem(
            title = "Settings",
            selectedIcon = ImageVector.vectorResource(R.drawable.settings_filled_24px),
            unselectedIcon = ImageVector.vectorResource(R.drawable.settings_outline_24px),
            route = Screen.settings.route
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (selectedItem == 0){
                ExtendedFABM3(expanded = true, onClick = { navController.navigate(Screen.addTransaction.route) })
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = bottomBarVisibility) {
                NavigationBottomBar(navController = navController, items = barItems)
            }
        }
    ){padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.homePage.route
        ){

            composable(route = Screen.homePage.route) {
                HomePageView(
                    navController = navController,
                    viewModel = homePageViewModel,
                    modifier = Modifier.padding(padding),
                    onSeeAllClick = {navController.navigate(Screen.transactionHistory.route)},
                    amountFormattingSettings = amountFormattingSettings ,
                )
                bottomBarVisibility = true
                selectedItem = 0
            }

            composable(route = Screen.chartPage.route) {
                // Pass the amountFormattingSettings to GraphPageView
                GraphPageView(
                    modifier = Modifier.padding(padding),
                    viewModel = graphPageViewModel,
                    amountFormattingSettings = amountFormattingSettings // Pass the settings
                )
                bottomBarVisibility = true
                selectedItem = 1
            }

            composable(route = Screen.addTransaction.route) {
                // Pass the amountFormattingSettings to AddTransactionView
                AddTransactionView(
                    navController = navController,
                    viewModel = addTransactionViewModel,
                    scope = scope,
                    amountFormattingSettings = amountFormattingSettings,
                    currentDateFormat = currentDateFormat
                )
                bottomBarVisibility = false
            }
            composable(route = Screen.transactionHistory.route) {
                // Pass the amountFormattingSettings to TransactionHistoryView
                TransactionHistoryView(
                    viewModel = transactionHistoryViewModel,
                    navController = navController,
                    modifier = Modifier.padding(padding),
                    amountFormattingSettings = amountFormattingSettings,
                    currentDateFormat = currentDateFormat
                )
                bottomBarVisibility = true
                selectedItem = 2
            }
            composable(route = Screen.settings.route) {
                SettingsView(
                    navController = navController,
                    modifier = Modifier.padding(padding),
                    settingsViewModel = settingsViewModel // Pass the ViewModel here
                )
                bottomBarVisibility = true
                selectedItem = 3
            }
        }
    }
}

@Composable
fun NavigationBottomBar(
    navController: NavController,
    items: List<BarItem>
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    BottomAppBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (currentRoute == item.route){
                        Icon(imageVector = item.selectedIcon, contentDescription = item.title)
                    }else{
                        Icon(imageVector = item.unselectedIcon, contentDescription = item.title)
                    }
                },
                label = { Text(text = item.title) },
                alwaysShowLabel = true,
            )
        }
    }
}
package com.ramphal.personalfinancepro.navigation

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.runtime.collectAsState
import com.ramphal.personalfinancepro.Graph.transactionRepository
import com.ramphal.personalfinancepro.ui.onboarding.OnboardingView
import com.ramphal.personalfinancepro.ui.onboarding.OnboardingViewModel
import com.ramphal.personalfinancepro.ui.settings.AmountFormattingSettings
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun AppNavigation(
    homePageViewModel: HomePageViewModel = viewModel<HomePageViewModel>(),
    addTransactionViewModel: AddTransactionViewModel = viewModel<AddTransactionViewModel>(),
    transactionHistoryViewModel: TransactionHistoryViewModel = viewModel<TransactionHistoryViewModel>(),
    graphPageViewModel: GraphPageViewModel = viewModel<GraphPageViewModel>(),
    navController: NavHostController = rememberNavController(),
){
    val scope = rememberCoroutineScope()
    var bottomBarVisibility by remember { mutableStateOf(true) }
    var floatingTabVisibility by remember { mutableStateOf(false) }

    val application = LocalContext.current.applicationContext as Application
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(application))
    val onboardingViewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory(application))

    val currentCurrencyCode by settingsViewModel.currentCurrencyCode.collectAsState()
    val currentAmountFormat by settingsViewModel.currentAmountFormat.collectAsState()
    val currentDateFormat by settingsViewModel.currentDateFormatPattern.collectAsState()
    val isLoadingCurrency by settingsViewModel.isLoadingCurrency.collectAsState()
    val isOnboardingComplete = onboardingViewModel.isOnboardingComplete()

    val fixedDecimalPlaces = settingsViewModel.fixedDecimalPlaces
    val fixedCurrencyPosition = settingsViewModel.fixedCurrencyPosition
    val snackbarHostState = remember { SnackbarHostState() }


    val amountFormattingSettings = remember(currentCurrencyCode, currentAmountFormat) {
        AmountFormattingSettings(
            preferredCurrencyCode = currentCurrencyCode,
            currencyPosition = fixedCurrencyPosition,
            amountFormatType = currentAmountFormat,
            decimalPlaces = fixedDecimalPlaces
        )
    }

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
        floatingActionButton = {
            AnimatedVisibility(floatingTabVisibility) {
                ExtendedFABM3(expanded = true, onClick = { navController.navigate(Screen.addTransaction.route) })
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = bottomBarVisibility) {
                NavigationBottomBar(navController = navController, items = barItems)
            }
        }
    ){padding ->
        NavHost(
            navController = navController,
            startDestination = if (isOnboardingComplete) Screen.homePage.route else Screen.onboarding.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(route = Screen.onboarding.route) {
                OnboardingView(
                    onSetupComplete = {
                        onboardingViewModel.setOnboardingComplete(isComplete = true)
                        scope.launch {
                            val onboardingData = transactionRepository.getOnboardingData(0).firstOrNull()
                            onboardingData?.let {
                                settingsViewModel.setCurrencyCode(it.preferredCurrencySymbol)
                            }
                        }
                        navController.navigate(Screen.homePage.route) {
                            popUpTo(Screen.onboarding.route) { inclusive = true }
                        }
                    },
                    onboardingViewModel = onboardingViewModel,
                )
                bottomBarVisibility = false
                floatingTabVisibility = false
            }

            composable(route = Screen.homePage.route) {
                HomePageView(
                    viewModel = homePageViewModel,
                    onSeeAllClick = {navController.navigate(Screen.transactionHistory.route)},
                    amountFormattingSettings = amountFormattingSettings,
                    onEditClick = { id ->
                        navController.navigate("${Screen.addTransaction.route}?transactionId=${id}")
                    },
                    message = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = it,
                                withDismissAction = true
                            )
                        }
                    }
                )
                bottomBarVisibility = true
                floatingTabVisibility = true
            }

            composable(route = Screen.chartPage.route) {
                GraphPageView(
                    viewModel = graphPageViewModel,
                    amountFormattingSettings = amountFormattingSettings
                )
                bottomBarVisibility = true
                floatingTabVisibility = false
            }

            // --- UPDATED: Define route for AddTransaction to accept optional transactionId ---
            composable(
                route = "${Screen.addTransaction.route}?transactionId={transactionId}",
                arguments = listOf(
                    navArgument("transactionId") {
                        type = NavType.LongType
                        defaultValue = -1L // Default value for new transactions
                    }
                )
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: -1L
                AddTransactionView(
                    navController = navController,
                    viewModel = addTransactionViewModel,
                    transactionId = transactionId,
                    amountFormattingSettings = amountFormattingSettings,
                    currentDateFormat = currentDateFormat,
                    message = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = it,
                                withDismissAction = true
                            )
                        }
                    }
                )
                bottomBarVisibility = false
                floatingTabVisibility = false
            }
            // --- END UPDATED ADD TRANSACTION ROUTE ---

            composable(route = Screen.transactionHistory.route) {
                TransactionHistoryView(
                    viewModel = transactionHistoryViewModel,
                    navController = navController,
                    amountFormattingSettings = amountFormattingSettings,
                    currentDateFormat = currentDateFormat,
                    onEditClick = { id ->
                        navController.navigate("${Screen.addTransaction.route}?transactionId=${id}")
                    },
                    message = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = it,
                                withDismissAction = true
                            )
                        }
                    }
                )
                bottomBarVisibility = true
                floatingTabVisibility = false
            }
            composable(route = Screen.settings.route) {
                SettingsView(
                    navController = navController,
                    settingsViewModel = settingsViewModel,
                )
                bottomBarVisibility = true
                floatingTabVisibility = false
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
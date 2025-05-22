package com.ramphal.personalfinancepro.ui

import com.ramphal.personalfinancepro.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import com.ramphal.personalfinancepro.navigation.Screen
import com.ramphal.personalfinancepro.ui.add.AddTransactionViewModel
import com.ramphal.personalfinancepro.ui.history.TransactionHistoryView
import com.ramphal.personalfinancepro.ui.history.TransactionHistoryViewModel
import com.ramphal.personalfinancepro.ui.home.ExtendedFABM3
import com.ramphal.personalfinancepro.ui.home.HomePageView
import com.ramphal.personalfinancepro.ui.home.HomePageViewModel
import kotlinx.coroutines.CoroutineScope


@Composable
fun NavigationBarM3(
    homePageViewModel: HomePageViewModel,
    addTransactionViewModel: AddTransactionViewModel,
    transactionHistoryViewModel: TransactionHistoryViewModel,
    navController: NavHostController,
    scope: CoroutineScope
) {
    var selectedItem: Int by remember { mutableIntStateOf(0) }
    val barItems = listOf(
        BarItem(
            title = "Home",
            selectedIcon = ImageVector.vectorResource(R.drawable.home_filled_24px),
            unselectedIcon = ImageVector.vectorResource(R.drawable.home_outline_24px),
            route = "home"
        ),
        BarItem(
            title = "Analytics",
            selectedIcon = ImageVector.vectorResource(R.drawable.ic_pie_chart_filled_24px),
            unselectedIcon = ImageVector.vectorResource(R.drawable.ic_pie_chart_outline_24px),
            route = "contacts"
        ),
        BarItem(
            title = "Records",
            selectedIcon = ImageVector.vectorResource(R.drawable.ic_receipt_filled_24px),
            unselectedIcon = ImageVector.vectorResource(R.drawable.ic_receipt_outline_24px),
            route = "shop"
        ),
        BarItem(
            title = "Settings",
            selectedIcon = ImageVector.vectorResource(R.drawable.settings_filled_24px),
            unselectedIcon = ImageVector.vectorResource(R.drawable.settings_outline_24px),
            route = "setting"
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (selectedItem == 0){
                ExtendedFABM3(expanded = true, onClick = { navController.navigate(Screen.addTransaction.route) })
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(0))
            ) {
                barItems.forEachIndexed { index, barItem ->
                    val selected = selectedItem == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            selectedItem = index
                            /* navigate to selected route */
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) barItem.selectedIcon else barItem.unselectedIcon,
                                contentDescription = barItem.title
                            )
                        },
                        label = { Text(text = barItem.title) }
                    )
                }
            }
        }
    ){innerpadding ->
        ContentScreen(
            modifier = Modifier.padding(innerpadding),
            selectedIndex = selectedItem,
            viewModel = homePageViewModel,
            addTransactionViewModel = addTransactionViewModel,
            transactionHistoryViewModel = transactionHistoryViewModel,
            navController = navController,
            scope = scope,
            onSeeAllClick = {selectedItem = 2}
        )
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    viewModel: HomePageViewModel,
    addTransactionViewModel: AddTransactionViewModel,
    transactionHistoryViewModel: TransactionHistoryViewModel,
    navController: NavHostController,
    scope: CoroutineScope,
    onSeeAllClick: () -> Unit
) {
    when(selectedIndex){
        0-> HomePageView(
            navController = navController,
            viewModel = viewModel,
            modifier = modifier,
            onSeeAllClick = onSeeAllClick
        )
        1-> GraphPageView()
        2-> TransactionHistoryView(
            viewModel = transactionHistoryViewModel,
            navController = navController,
            modifier = modifier
        )
    }
}

data class BarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)
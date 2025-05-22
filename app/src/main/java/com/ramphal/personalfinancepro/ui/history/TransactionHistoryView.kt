package com.ramphal.personalfinancepro.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ramphal.personalfinancepro.Constant
import com.ramphal.personalfinancepro.R
import com.ramphal.personalfinancepro.data.TransactionModel
import com.ramphal.personalfinancepro.ui.home.CustomTopAppBar
import com.ramphal.personalfinancepro.ui.home.TransitionItem
import com.ramphal.personalfinancepro.ui.theme.myFont
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionHistoryView(
    viewModel: TransactionHistoryViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
){val transactions by viewModel.getAllTransaction.collectAsState(initial = emptyList())
    val now = remember { LocalDateTime.now() }
    val (upcoming, completed) = remember(transactions, now) {
        transactions.partition { it.timestamp.isAfter(now) }
    }
    val upcomingSorted = remember(upcoming) { upcoming.sortedBy { it.timestamp } }
    val completedSorted = remember(completed) { completed.sortedByDescending { it.timestamp } }
    val lazyListState = rememberLazyListState()

    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1500L); isLoading = false }

    Surface(modifier = modifier.fillMaxSize().padding(bottom = 8.dp)) {
        AnimatedVisibility(
            visible = isLoading,
            exit = fadeOut(tween(300))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ContainedLoadingIndicator(modifier = Modifier.size(80.dp))
            }
        }

        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(tween(500)) + slideInVertically(
                animationSpec = tween(500),
                initialOffsetY = { it / 3 }
            )
        ) {
            Column(Modifier.fillMaxSize()) {
                CustomTopAppBar()

                // Secondary tabs
                var tabIndex by remember { mutableIntStateOf(0) }
                val tabs = listOf("Completed", "Upcoming")

                Card(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .fillMaxSize(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    SecondaryTabRow(
                        selectedTabIndex = tabIndex,
                        containerColor = Color.Transparent
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = tabIndex == index,
                                onClick = { tabIndex = index },
                                text = { Text(title, fontFamily = myFont, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                    when (tabIndex) {
                        0 -> {
                            if (completedSorted.isEmpty()) {
                                NoTransactionsMessage()
                            } else {
                                LazyColumn(state = lazyListState) {
                                    items(completedSorted, key = { it.id }) { txn ->
                                        TransactionHistoryItem(txn)
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (upcomingSorted.isEmpty()) {
                                NoTransactionsMessage()
                            } else {
                                LazyColumn(state = lazyListState) {
                                    items(upcomingSorted, key = { it.id }) { txn ->
                                        TransactionHistoryItem(txn)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoTransactionsMessage() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No Transactions Found",
            fontFamily = myFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TransactionHistoryItem(transactionList: TransactionModel) {
    TransitionItem(
        modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
        title = when (transactionList.type) {
            "Expense" -> Constant.categories[transactionList.to].name
            "Income" -> Constant.incomeCat[transactionList.from].name
            else -> "Self Transferred"
        },
        amount = transactionList.amount,
        icon = when (transactionList.type) {
            "Expense" -> Constant.categories[transactionList.to].icon
            "Income" -> Constant.incomeCat[transactionList.from].icon
            else -> R.drawable.self_transfer_24px
        },
        date = transactionList.timestamp.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
        color = if (transactionList.type == "Expense") colorResource(R.color.red_contrast)
        else colorResource(R.color.green_contrast),
        sign = if (transactionList.type == "Expense") "-" else if (transactionList.type == "Income") "+" else ""
    )
}

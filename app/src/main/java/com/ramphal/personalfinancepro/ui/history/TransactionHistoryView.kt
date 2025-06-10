package com.ramphal.personalfinancepro.ui.history

import android.R.attr.onClick
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ramphal.personalfinancepro.Constant
import com.ramphal.personalfinancepro.R
import com.ramphal.personalfinancepro.data.OnboardingModel
import com.ramphal.personalfinancepro.data.TransactionModel
import com.ramphal.personalfinancepro.ui.add.updateBalanceForAccount
import com.ramphal.personalfinancepro.ui.home.CustomTopAppBar
import com.ramphal.personalfinancepro.ui.home.TransactionDetailBottomSheet
import com.ramphal.personalfinancepro.ui.home.TransitionItem
import com.ramphal.personalfinancepro.ui.settings.AmountFormattingSettings
import com.ramphal.personalfinancepro.ui.theme.myFont
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionHistoryView(
    viewModel: TransactionHistoryViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    amountFormattingSettings: AmountFormattingSettings,
    currentDateFormat: String,
    onEditClick: (Long) -> Unit,
    message: (String) -> Unit
){
    val transactions by viewModel.getAllTransaction.collectAsState(initial = emptyList())
    val now = remember { LocalDateTime.now() }
    val (upcoming, completed) = remember(transactions, now) {
        transactions.partition { it.timestamp.isAfter(now) }
    }
    val upcomingSorted = remember(upcoming) { upcoming.sortedBy { it.timestamp } }
    val completedSorted = remember(completed) { completed.sortedByDescending { it.timestamp } }
    val lazyListState = rememberLazyListState()
    val showDetailScreen = remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionModel?>(null) }

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionModel?>(null) }

    val detailOverview by viewModel.detailOverview.collectAsState(
        initial = OnboardingModel(
            id = 0,
            preferredCurrencySymbol = "$",
            bankBalance = 0.0,
            cashBalance = 0.0,
            savingsBalance = 0.0,
            hasCreditCard = false,
            creditCardBalance = 0.0
        )
    )

    if (showDetailScreen.value && selectedTransaction != null) {
        TransactionDetailBottomSheet(
            transaction = selectedTransaction!!, // Pass the stored transaction details
            settings = amountFormattingSettings,
            showBottomSheet = showDetailScreen, // Pass the MutableState for internal control
            onDismissRequest = {
                showDetailScreen.value = false    // Hide the sheet on dismiss
                selectedTransaction = null        // Clear the selected transaction
            },
            onEditClick = { tx ->
                // Handle edit action for the given transaction
                showDetailScreen.value = false // Hide sheet after action
                selectedTransaction = null
                onEditClick(tx.id)
            },
            onDeleteClick = { tx ->
                showDetailScreen.value = false // Assuming showDetailScreen controls a bottom sheet
                selectedTransaction = null // Clear selected transaction for the sheet
                transactionToDelete = tx
                showDeleteConfirmationDialog = true
            }
        )
    }

    if (showDeleteConfirmationDialog && transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmationDialog = false
                transactionToDelete = null
            },
            icon = { Icon(painter = painterResource(R.drawable.ic_delete_24px), contentDescription = "delete") },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction permanently?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { tx ->
                            when (tx.type) {
                                "Expense" -> {
                                    val onboardingModel = detailOverview // assuming detailOverview is accessible here
                                    var updatedOnboardingModel = onboardingModel.copy()
                                    updatedOnboardingModel = updateBalanceForAccount(
                                        currentOnboarding = updatedOnboardingModel,
                                        accountIndex = tx.from,
                                        amount = tx.amount.toDouble() // Add back the amount deducted for expense
                                    )
                                    viewModel.updateOverallBalance(updatedOnboardingModel)
                                }

                                "Income" -> {
                                    val onboardingModel = detailOverview
                                    var updatedOnboardingModel = onboardingModel.copy()
                                    updatedOnboardingModel = updateBalanceForAccount(
                                        currentOnboarding = updatedOnboardingModel,
                                        accountIndex = tx.to,
                                        amount = (tx.amount.toDouble() * -1) // Subtract the amount added for income
                                    )
                                    viewModel.updateOverallBalance(updatedOnboardingModel)
                                }

                                else -> { // Transfer
                                    val onboardingModel = detailOverview
                                    var updatedOnboardingModel = onboardingModel.copy()
                                    updatedOnboardingModel = updateBalanceForAccount(
                                        currentOnboarding = updatedOnboardingModel,
                                        accountIndex = tx.from,
                                        amount = tx.amount.toDouble() // Add back to 'from' account
                                    )
                                    updatedOnboardingModel = updateBalanceForAccount(
                                        currentOnboarding = updatedOnboardingModel,
                                        accountIndex = tx.to,
                                        amount = (tx.amount.toDouble() * -1) // Subtract from 'to' account
                                    )
                                    viewModel.updateOverallBalance(updatedOnboardingModel)
                                }
                            }
                            viewModel.deleteTransaction(tx)
                        }
                        showDeleteConfirmationDialog = false
                        transactionToDelete = null
                        message("Transaction deleted successfully")
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        transactionToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(modifier = modifier.fillMaxSize().padding(bottom = 8.dp)) {
        Column(Modifier.fillMaxSize()) {
            CustomTopAppBar("Transaction History")

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
                                    TransactionHistoryItem(txn, amountFormattingSettings, currentDateFormat, onClick = {
                                        selectedTransaction = txn
                                        showDetailScreen.value = true
                                    })
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
                                    TransactionHistoryItem(txn, amountFormattingSettings, currentDateFormat, onClick = {
                                        selectedTransaction = txn
                                        showDetailScreen.value = true
                                    })
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
private fun TransactionHistoryItem(
    transactionList: TransactionModel,
    amountFormattingSettings: AmountFormattingSettings,
    currentDateFormat: String,
    onClick: () -> Unit
) {
    TransitionItem(
        settings = amountFormattingSettings,
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
        date = formatDateTime(transactionList.timestamp, dateFormatPattern = currentDateFormat),
        color = if (transactionList.type == "Expense") colorResource(R.color.red_contrast)
        else if (transactionList.type == "Income") colorResource(R.color.green_contrast)
        else MaterialTheme.colorScheme.primary,
        sign = if (transactionList.type == "Expense") "-" else if (transactionList.type == "Income") "+" else "",
        onClick = onClick
    )
}

fun formatDateTime(
    dateTime: LocalDateTime,
    dateFormatPattern: String,
    locale: Locale = Locale.getDefault() // Optional: allows specifying locale
): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern(dateFormatPattern, locale)
        dateTime.format(formatter)
    } catch (e: IllegalArgumentException) {
        // Handle invalid pattern: log error, or return a default/raw string
        println("Error formatting date: Invalid pattern '$dateFormatPattern'. Original error: ${e.message}")
        dateTime.toString() // Fallback to default string representation
    } catch (e: DateTimeParseException) {
        println("Error formatting date: Failed to parse with pattern '$dateFormatPattern'. Original error: ${e.message}")
        dateTime.toString() // Fallback
    }
}

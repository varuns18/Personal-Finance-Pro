package com.ramphal.personalfinancepro.ui.home

// import java.util.Currency // No longer needed if not using Currency.getInstance()
import android.R.id.message
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramphal.personalfinancepro.Constant
import com.ramphal.personalfinancepro.R
import com.ramphal.personalfinancepro.data.OnboardingModel
import com.ramphal.personalfinancepro.data.TransactionModel
import com.ramphal.personalfinancepro.ui.add.updateBalanceForAccount
import com.ramphal.personalfinancepro.ui.settings.AmountFormatType
import com.ramphal.personalfinancepro.ui.settings.AmountFormattingSettings
import com.ramphal.personalfinancepro.ui.theme.myFont
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomePageView(
    viewModel: HomePageViewModel,
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit,
    amountFormattingSettings: AmountFormattingSettings,
    onEditClick: (Long) -> Unit,
    message: (String) -> Unit
) {

    val thisMonthIncome   by viewModel.thisMonthIncome.collectAsState()
    val thisMonthExpense  by viewModel.thisMonthExpense.collectAsState()
    val allTransactions   by viewModel.getAllTransaction.collectAsState(initial = emptyList())
    val thisMonthTransactions by viewModel.getThisMonthTransactions.collectAsState(initial = emptyList())
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

    val showDetailScreen = remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionModel?>(null) }

    val lazyListState = rememberLazyListState()
    val isExpanded by remember { derivedStateOf { lazyListState.firstVisibleItemIndex == 0 } }

    val now = remember { LocalDateTime.now() }

    val upcomingAll = remember(allTransactions, now) {
        allTransactions.filter { it.timestamp.isAfter(now) }
    }
    val upcomingSorted = remember(upcomingAll) {
        upcomingAll.sortedBy { it.timestamp }
    }

    val completedThisMonth = remember(thisMonthTransactions, now) {
        thisMonthTransactions.filter { !it.timestamp.isAfter(now) }
    }
    val completedSorted = remember(completedThisMonth) {
        completedThisMonth.sortedByDescending { it.timestamp }
    }


    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionModel?>(null) }

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


    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState
        ) {
            stickyHeader {
                CustomTopAppBar()
            }
            stickyHeader {
                // Modified: Added 'settings' to remember key
                BalanceCard(settings = amountFormattingSettings, context = LocalContext.current, detailOverview = detailOverview)
            }
            stickyHeader {
                // Modified: Added 'settings' to remember key
                IncomeAndExpenseCard(thisMonthExpense = thisMonthExpense, thisMonthIncome = thisMonthIncome, settings = amountFormattingSettings)
            }
            item {
                if (upcomingSorted.isNotEmpty()){
                    Card(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp)
                        ){
                            Text(
                                text = "Upcoming Transactions",
                                fontSize = 16.sp,
                                fontFamily = myFont,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                            TextButton(
                                onClick = onSeeAllClick,
                                modifier = Modifier.align(Alignment.CenterEnd),
                            ) {
                                Text(
                                    text = "See all",
                                    fontFamily = myFont,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_keyboard_arrow_right_24px),
                                    contentDescription = null,
                                    modifier = Modifier.padding(0.dp)
                                )
                            }
                        }
                        upcomingSorted.forEach{ transactionList ->
                            TransitionItem(
                                settings = amountFormattingSettings,
                                modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                                title = if (transactionList.type == "Expense") {
                                    Constant.categories[transactionList.to].name
                                } else if (transactionList.type == "Income") {
                                    Constant.incomeCat[transactionList.from].name
                                } else {
                                    "Self Transferred"
                                },
                                amount = transactionList.amount,
                                icon = if (transactionList.type == "Expense") {
                                    Constant.categories[transactionList.to].icon
                                } else if (transactionList.type == "Income") {
                                    Constant.incomeCat[transactionList.from].icon
                                } else {
                                    R.drawable.self_transfer_24px
                                },
                                date = transactionList.timestamp.format(DateTimeFormatter.ofPattern("dd MMMM")),
                                color = if (transactionList.type == "Expense") {
                                    colorResource(R.color.red_contrast)
                                } else if (transactionList.type == "Income") {
                                    colorResource(R.color.green_contrast)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                sign = if (transactionList.type == "Expense") "-" else if (transactionList.type == "Income") "+" else "",
                                onClick = {
                                    selectedTransaction = transactionList
                                    showDetailScreen.value = true
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (completedSorted.isNotEmpty()){
                    Card(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp)
                        ){
                            Text(
                                text = "This Month’s Transactions",
                                fontSize = 16.sp,
                                fontFamily = myFont,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                            TextButton(
                                onClick = onSeeAllClick,
                                modifier = Modifier.align(Alignment.CenterEnd),
                            ) {
                                Text(
                                    text = "See all",
                                    fontFamily = myFont,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_keyboard_arrow_right_24px),
                                    contentDescription = null,
                                    modifier = Modifier.padding(0.dp)
                                )
                            }
                        }
                        completedSorted.forEach{ transactionList ->
                            TransitionItem(
                                settings = amountFormattingSettings,
                                modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                                title = if (transactionList.type == "Expense") {
                                    Constant.categories[transactionList.to].name
                                } else if (transactionList.type == "Income") {
                                    Constant.incomeCat[transactionList.from].name
                                } else {
                                    "Self Transferred"
                                },
                                amount = transactionList.amount,
                                icon = if (transactionList.type == "Expense") {
                                    Constant.categories[transactionList.to].icon
                                } else if (transactionList.type == "Income") {
                                    Constant.incomeCat[transactionList.from].icon
                                } else {
                                    R.drawable.self_transfer_24px
                                },
                                date = transactionList.timestamp.format(DateTimeFormatter.ofPattern("dd MMMM")),
                                color = if (transactionList.type == "Expense") {
                                    colorResource(R.color.red_contrast)
                                } else if (transactionList.type == "Income") {
                                    colorResource(R.color.green_contrast)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                sign = if (transactionList.type == "Expense") "-" else "+",
                                onClick = {
                                    selectedTransaction = transactionList
                                    showDetailScreen.value = true
                                }
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun BalanceCard(
    settings: AmountFormattingSettings,
    context: Context,
    detailOverview: OnboardingModel // Assuming OnboardingModel holds the balance details
){
    var showAccountDetails by remember { mutableStateOf(false) }

    val totalBalance = remember(detailOverview) {
        detailOverview.bankBalance +
                detailOverview.cashBalance +
                detailOverview.savingsBalance +
                if (detailOverview.hasCreditCard) detailOverview.creditCardBalance else 0.0
    }

    val (sign, symbol, amount) = remember(totalBalance, settings) { // Added totalBalance as a key
        formatAmountComponents(
            totalBalance,
            settings = settings,
        )
    }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 8.dp, end = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ){
                    Text(
                        text = "Overall Balance",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = myFont,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomPrice(sign = if (totalBalance == 0.0) "" else sign ,
                        currency = symbol,
                        amount   = amount,
                        36.sp,
                        36.sp
                    )
                    // Conditionally display account details
                    AnimatedVisibility(showAccountDetails) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            AccountItemDisplay(
                                settings = settings,
                                title = Constant.accountItems[0].name,
                                amount = detailOverview.bankBalance,
                                icon = Constant.accountItems[0].icon
                            )
                            AccountItemDisplay(
                                settings = settings,
                                title = Constant.accountItems[1].name,
                                amount = detailOverview.savingsBalance,
                                icon = Constant.accountItems[1].icon
                            )
                            AccountItemDisplay(
                                settings = settings,
                                title = Constant.accountItems[2].name,
                                amount = detailOverview.cashBalance,
                                icon = Constant.accountItems[2].icon
                            )
                            if (detailOverview.hasCreditCard){
                                AccountItemDisplay(
                                    settings = settings,
                                    title = Constant.accountItems[3].name,
                                    amount = detailOverview.creditCardBalance,
                                    icon = Constant.accountItems[3].icon
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                        }
                    }
                }
                FilledIconButton(
                    onClick = {
                        showAccountDetails = !showAccountDetails
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd),
                    shape = RoundedCornerShape(12.dp)
                ){
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            if (showAccountDetails) R.drawable.ic_arrow_up_24px
                            else R.drawable.ic_arrow_up_24px
                        ),
                        modifier = Modifier
                            .rotate(
                                if (showAccountDetails) 0f
                                else 180f
                            )
                            .padding(top = 4.dp),
                        contentDescription = "Toggle Account Details"
                    )
                }
            }
        }
    }
}

@Composable
fun AccountItemDisplay(
    settings: AmountFormattingSettings,
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    icon: Int
) {
    // Format the account's currentBalance (which is a String) to Double for formatting
    val balanceAsDouble = remember(amount) { amount }
    val (sign, symbol, amount) = remember(balanceAsDouble, settings) {
        formatAmountComponents(
            balanceAsDouble,
            settings = settings
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp), // Adjust padding as needed
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                fontFamily = myFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal // Changed to Normal for less emphasis than title
            )
        }
        CustomPrice(
            sign = sign, // Assuming sign from AccountItem is always '+' for balance
            currency = symbol,
            amount = amount,
            cFontSize = 16.sp,
            aFontSize = 16.sp
        )
    }
}

@Composable
private fun IncomeAndExpenseCard(
    thisMonthExpense: Double,
    thisMonthIncome: Double,
    settings: AmountFormattingSettings
) {
    // Modified: Added 'settings' to the remember keys
    val (signE, symbolE, amountE) = remember(thisMonthExpense, settings) {
        formatAmountComponents(
            thisMonthExpense,
            settings = settings,
        )
    }
    // Modified: Added 'settings' to the remember keys
    val (signI, symbolI, amountI) = remember(thisMonthIncome, settings) {
        formatAmountComponents(
            thisMonthIncome,
            settings = settings,
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            RowCardItem(
                modifier = Modifier
                    .padding()
                    .width(0.dp)
                    .weight(1f),
                title = "Monthly Income",
                amount = amountI,
                sign = if (thisMonthIncome == 0.0) "" else "+",
                currency = symbolI
            )
            RowCardItem(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(0.dp)
                    .weight(1f),
                title = "Monthly Expense",
                amount = amountE,
                sign = if (thisMonthExpense == 0.0) "" else "-",
                currency = symbolE
            )
        }
    }
}


@Composable
fun RowCardItem(
    modifier: Modifier,
    title: String,
    amount: String,
    sign: String,
    currency: String
){
    Card(modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ){
            Text(
                text = title,
                fontSize = 16.sp,
                fontFamily = myFont,
                fontWeight = FontWeight.Bold
            )
            CustomPrice(
                sign = sign,
                currency = currency,
                amount = amount,
                cFontSize = 18.sp,
                aFontSize = 18.sp
            )
        }
    }
}

@Composable
fun TransitionItem(
    sign: String,
    modifier: Modifier,
    title: String,
    amount: String, // Keep this as String if that's how it comes in
    icon: Int,
    date: String,
    color: Color,
    onClick: () -> Unit,
    settings: AmountFormattingSettings
){
    // Modified: Added 'settings' to the remember keys and renamed local 'amount' variable
    val (sign2, symbol, formattedAmount) = remember(amount, settings) {
        formatAmountComponents(
            amount.toDouble(),
            settings = settings,
        )
    }
    Column(
        modifier = Modifier.clickable(
            onClick = onClick
        )
    ) {
        HorizontalDivider(Modifier.padding(bottom = 8.dp))
        Row(
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedIconButton(
                onClick = {},
                modifier = Modifier.size(40.dp),
                border = BorderStroke(
                    width = 2.dp,
                    color = color
                )
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = color
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontFamily = myFont
                    )
                    CustomPrice(
                        sign     = sign,
                        currency = symbol,
                        amount   = formattedAmount, // Use the new formattedAmount
                        modifier = Modifier
                    )
                }


                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = date,
                    fontSize = 12.sp,
                    fontFamily = myFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CustomTopAppBar(
    title: String = "Personal Finance Pro",
    modifier: Modifier = Modifier.padding(8.dp)
){
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
    ){
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = modifier
                .wrapContentHeight()
                .fillMaxWidth(),
        ) {
            Box{
                Text(
                    text = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontFamily = myFont,
                    fontWeight = FontWeight.Normal
                )
                Column(
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp, bottom = 8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_app_icon),
                        contentDescription = "App logo",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailBottomSheet(
    transaction: TransactionModel,
    settings: AmountFormattingSettings,
    showBottomSheet: MutableState<Boolean>, // Use MutableState to control visibility
    onDismissRequest: () -> Unit,
    onEditClick: (TransactionModel) -> Unit,
    onDeleteClick: (TransactionModel) -> Unit
) {
    if (showBottomSheet.value) { // Only show if state is true
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), // Full expansion
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Transaction Type & Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = transaction.type, // e.g., "Expense", "Income", "Transfer"
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = myFont,
                        fontWeight = FontWeight.Bold,
                        color = when (transaction.type) {
                            "Expense" -> colorResource(R.color.red_contrast)
                            "Income" -> colorResource(R.color.green_contrast)
                            "Transfer" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    val (sign, symbol, amount) = remember(transaction.amount, settings) {
                        formatAmountComponents(transaction.amount.toDouble(), settings)
                    }

                    CustomPrice(
                        sign = when (transaction.type) {
                            "Expense" -> "-"
                            "Income" -> "+"
                            else -> ""
                        },
                        currency = symbol,
                        amount = amount,
                        cFontSize = 28.sp,
                        aFontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Transaction Details List
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Date
                    DetailRow(
                        icon = R.drawable.ic_calendar_24px,
                        label = "Date",
                        value = transaction.timestamp.format(DateTimeFormatter.ofPattern("dd MMMM, yyyy")) // Customize date format
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // From Account/Category (for Income/Expense)
                    if (transaction.type == "Income") {
                        val categoryName = Constant.incomeCat.getOrNull(transaction.from)?.name ?: "N/A"
                        DetailRow(
                            icon = Constant.incomeCat.getOrNull(transaction.from)?.icon ?: R.drawable.ic_income_24px,
                            label = "Source",
                            value = categoryName
                        )
                    } else { // Expense or Transfer
                        val accountName = Constant.accountItems.getOrNull(transaction.from)?.name ?: "N/A"
                        DetailRow(
                            icon = Constant.accountItems.getOrNull(transaction.from)?.icon ?: R.drawable.ic_bank_account_24px,
                            label = "From",
                            value = accountName
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // To Account/Category (for Expense/Transfer)
                    if (transaction.type == "Expense") {
                        val categoryName = Constant.categories.getOrNull(transaction.to)?.name ?: "N/A"
                        DetailRow(
                            icon = Constant.categories.getOrNull(transaction.to)?.icon ?: R.drawable.ic_grocery_24px,
                            label = "Spent On",
                            value = categoryName
                        )
                    } else { // Income or Transfer
                        val accountName = Constant.accountItems.getOrNull(transaction.to)?.name ?: "N/A"
                        DetailRow(
                            icon = Constant.accountItems.getOrNull(transaction.to)?.icon ?: R.drawable.ic_bank_account_24px,
                            label = "To",
                            value = accountName
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Note (if available)
                    transaction.note?.takeIf { it.isNotBlank() }?.let {
                        DetailRow(
                            icon = R.drawable.ic_note_24px,
                            label = "Note",
                            value = it
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit Button
                    Button(
                        onClick = { onEditClick(transaction) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_edit_24px),
                            contentDescription = "Edit",
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Edit",
                            fontFamily = myFont,
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // Delete Button
                    OutlinedButton(
                        onClick = { onDeleteClick(transaction) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_delete_24px),
                            contentDescription = "Delete",
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Delete",
                            fontFamily = myFont,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // Padding for bottom sheet close handle
            }
        }
    }
}

/**
 * A helper composable for displaying a single detail row (icon, label, value).
 */
@Composable
private fun DetailRow(
    icon: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null, // Content description for accessibility if needed
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = myFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = myFont,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun ExtendedFABM3(
    expanded: Boolean,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_add_24px), contentDescription = null) },
        text = { Text(text = "New Entry")},
        expanded = expanded,
        modifier = Modifier.padding(16.dp),
    )
}

@Composable
fun CustomPrice(
    sign: String = "-",
    currency: String,
    amount: String,
    cFontSize: TextUnit = 16.sp,
    aFontSize: TextUnit = 16.sp,
    modifier: Modifier = Modifier
){
    Row(modifier = modifier) {
        Text(
            text = sign,
            fontFamily = myFont,
            fontSize = cFontSize,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.width(1.dp))
        Text(
            text = currency,
            fontFamily = myFont,
            fontSize = cFontSize,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.width(1.dp))
        Text(
            text = amount,
            fontFamily = myFont,
            fontSize = aFontSize,
            fontWeight = FontWeight.Normal
        )
    }
}

// MODIFIED formatAmountComponents
fun formatAmountComponents(
    rawAmount: Double,
    settings: AmountFormattingSettings
): Triple<String, String, String> {
    val sign = if (rawAmount < 0) "-" else "+"

    // Directly get the symbol from the available options using the stored preferredCurrencyCode (which is the symbol)
    val displaySymbol = Constant.getAvailableCurrencyOptions()
        .firstOrNull { it.symbol == settings.preferredCurrencyCode }
        ?.symbol
        ?: settings.preferredCurrencyCode // Fallback to the symbol itself if for some reason it's not in the list

    val pattern = when (settings.amountFormatType) {
        AmountFormatType.INTERNATIONAL -> "#,##0"
        AmountFormatType.INDIAN -> "##,##,##0"
        AmountFormatType.NONE -> "#0"
    }
    val decimalPart = if (settings.decimalPlaces > 0) {
        "." + "#".repeat(settings.decimalPlaces)
    } else {
        ""
    }

    val formatter = DecimalFormat(pattern + decimalPart)
    val amountString = formatter.format(abs(rawAmount))
    return Triple(sign, displaySymbol, amountString)
}
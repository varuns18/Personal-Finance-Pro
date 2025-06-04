package com.ramphal.personalfinancepro.ui.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.navigation.NavHostController
import com.ramphal.personalfinancepro.Constant
import com.ramphal.personalfinancepro.Constant.fallbackSymbols
import com.ramphal.personalfinancepro.R
import com.ramphal.personalfinancepro.ui.settings.AmountFormatType
import com.ramphal.personalfinancepro.ui.settings.AmountFormattingSettings
import com.ramphal.personalfinancepro.ui.theme.myFont
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Currency
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomePageView(
    navController: NavHostController,
    viewModel: HomePageViewModel,
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit,
    amountFormattingSettings: AmountFormattingSettings,
) {

    val totalBalance      by viewModel.totalBalance.collectAsState()
    val thisMonthIncome   by viewModel.thisMonthIncome.collectAsState()
    val thisMonthExpense  by viewModel.thisMonthExpense.collectAsState()
    val allTransactions   by viewModel.getAllTransaction.collectAsState(initial = emptyList())
    val thisMonthTransactions by viewModel.getThisMonthTransactions.collectAsState(initial = emptyList())

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
                BalanceCard(totalBalance = totalBalance, settings = amountFormattingSettings, context = LocalContext.current)
            }
            stickyHeader {
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
                                sign = if (transactionList.type == "Expense") "-" else "+",
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
    totalBalance: Double,
    settings: AmountFormattingSettings,
    context: android.content.Context
){
    val (sign, symbol, amount) = remember(totalBalance) {
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
                        text = "Current Balance",
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
                }
                FilledIconButton(
                    onClick = {
                        Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                ){
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_forward_24px),
                        contentDescription = "Current Balance"
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeAndExpenseCard(
    thisMonthExpense: Double,
    thisMonthIncome: Double,
    settings: AmountFormattingSettings
) {
    val (signE, symbolE, amountE) = remember(thisMonthExpense) {
        formatAmountComponents(
            thisMonthExpense,
            settings = settings,
        )
    }
    val (signI, symbolI, amountI) = remember(thisMonthIncome) {
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
    amount: String,
    icon: Int,
    date: String,
    color: Color,
    settings: AmountFormattingSettings
){
    val (sign2, symbol, amount) = remember(amount) {
        formatAmountComponents(
            amount.toDouble(),
            settings = settings,
        )
    }
    Column {
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
                        amount   = amount,
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
    title: String = "Personal Finance Pro"
){
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
    ){
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Box(
                modifier = Modifier.padding(16.dp)
            ) {
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
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_account_circle_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(26.dp)
                )
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_account_circle_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(26.dp)
                )
            }
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

fun formatAmountComponents(
    rawAmount: Double,
    settings: AmountFormattingSettings // Updated to take AmountFormattingSettings object
): Triple<String, String, String> {
    val sign = if (rawAmount < 0) "-" else "+"
    val symbol = fallbackSymbols[settings.preferredCurrencyCode] ?:
    try {
        Currency.getInstance(settings.preferredCurrencyCode).symbol
    } catch (e: IllegalArgumentException) {
        settings.preferredCurrencyCode // Fallback to the code itself if symbol not found
    }
    val pattern = when (settings.amountFormatType) {
        AmountFormatType.INTERNATIONAL -> "#,##0"    // e.g., 1,234,567
        AmountFormatType.INDIAN -> "##,##,##0"       // e.g., 12,34,567
        AmountFormatType.NONE -> "#0"                // e.g., 1234567 (no separators)
    }
    val decimalPart = if (settings.decimalPlaces > 0) {
        "." + "#".repeat(settings.decimalPlaces)
    } else {
        ""
    }

    // Initialize DecimalFormat with the determined pattern
    val formatter = DecimalFormat(pattern + decimalPart)
    val amountString = formatter.format(abs(rawAmount))
    return Triple(sign, symbol, amountString)
}



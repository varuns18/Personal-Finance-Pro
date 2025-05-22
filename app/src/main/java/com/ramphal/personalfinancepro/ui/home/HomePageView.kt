package com.ramphal.personalfinancepro.ui.home

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ramphal.personalfinancepro.Constant
import com.ramphal.personalfinancepro.R
import com.ramphal.personalfinancepro.ui.theme.PersonalFinanceProTheme
import com.ramphal.personalfinancepro.ui.theme.ReceivedColor
import com.ramphal.personalfinancepro.ui.theme.myFont
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomePageView(
    navController: NavHostController,
    viewModel: HomePageViewModel,
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit
) {

    val totalBalance      by viewModel.totalBalance.collectAsState()
    val thisMonthIncome   by viewModel.thisMonthIncome.collectAsState()
    val thisMonthExpense  by viewModel.thisMonthExpense.collectAsState()
    val transactions      by viewModel.getThisMonthTransactions.collectAsState(initial = emptyList())

    val lazyListState = rememberLazyListState()
    val isExpanded by remember { derivedStateOf { lazyListState.firstVisibleItemIndex == 0 } }

    // Define “now” as a LocalDateTime
    val now = remember { LocalDateTime.now() }

    val (upcoming, completed) = remember(transactions, now) {
        transactions.partition { it.timestamp.isAfter(now) }
    }

    val upcomingSorted = remember(upcoming) {
        upcoming.sortedBy { it.timestamp }             // soonest first
    }
    val completedSorted = remember(completed) {
        completed.sortedByDescending { it.timestamp }  // most recent first
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
                BalanceCard(totalBalance = totalBalance)
            }
            stickyHeader {
                IncomeAndExpenseCard(thisMonthExpense = thisMonthExpense, thisMonthIncome = thisMonthIncome)
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
    totalBalance: Double
){
    val (sign, symbol, amount) = remember(totalBalance) {
        formatAmountComponents(
            totalBalance,
            locale = Locale.getDefault(),   // or Locale("en","IN") if you want INR formatting
            currencyCode = Currency.getInstance(Locale.getDefault()).currencyCode
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
                    onClick = {},
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
private fun IncomeAndExpenseCard(thisMonthExpense: Double, thisMonthIncome: Double) {
    val (signE, symbolE, amountE) = remember(thisMonthExpense) {
        formatAmountComponents(
            thisMonthExpense,
            locale = Locale.getDefault(),   // or Locale("en","IN") if you want INR formatting
            currencyCode = Currency.getInstance(Locale.getDefault()).currencyCode
        )
    }
    val (signI, symbolI, amountI) = remember(thisMonthIncome) {
        formatAmountComponents(
            thisMonthIncome,
            locale = Locale.getDefault(),   // or Locale("en","IN") if you want INR formatting
            currencyCode = Currency.getInstance(Locale.getDefault()).currencyCode
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
    color: Color
){
    val (sign2, symbol, amount) = remember(amount) {
        formatAmountComponents(
            amount.toDouble(),
            locale = Locale.getDefault(),   // or Locale("en","IN") if you want INR formatting
            currencyCode = Currency.getInstance(Locale.getDefault()).currencyCode
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
fun CustomTopAppBar(){
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
                    text = "Personal Finance Pro",
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
    locale: Locale = Locale.getDefault(),
    currencyCode: String = Currency.getInstance(locale).currencyCode
): Triple<String, String, String> {

    val sign = if (rawAmount < 0) "-" else "+"

    val symbol = "₹"

    val formatter = DecimalFormat("#,##,##0.##")
    val amountString = formatter.format(abs(rawAmount))

    return Triple(sign, symbol, amountString)
}

@Preview(showBackground = true)
@Composable
fun TransitionItemPreview() {
    PersonalFinanceProTheme {
        TransitionItem(
            sign = "+",
            modifier = Modifier,
            title = "Java Developer",
            amount = "26000",
            icon = Constant.categories[7].icon,
            date = "06 May",
            color = ReceivedColor
        )
    }
}



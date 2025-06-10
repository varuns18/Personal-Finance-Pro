package com.ramphal.personalfinancepro.ui.analytics

import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.ramphal.personalfinancepro.R
import com.ramphal.personalfinancepro.ui.home.CustomPrice
import com.ramphal.personalfinancepro.ui.home.CustomTopAppBar
import com.ramphal.personalfinancepro.ui.home.formatAmountComponents
import com.ramphal.personalfinancepro.ui.settings.AmountFormattingSettings
import com.ramphal.personalfinancepro.ui.theme.blueColor
import com.ramphal.personalfinancepro.ui.theme.greenColor
import com.ramphal.personalfinancepro.ui.theme.myFont
import com.ramphal.personalfinancepro.ui.theme.redColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Data class for a single slice of the pie chart.
 */
data class PieChartSliceData(
    val value: Float,
    val description: String,
    val color: Color
)

@Composable
fun GraphPageView(
    modifier: Modifier = Modifier,
    viewModel: GraphPageViewModel,
    amountFormattingSettings: AmountFormattingSettings
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            CustomTopAppBar("Analytics")
            MonthlyOverallByCategorySection(viewModel, amountFormattingSettings = amountFormattingSettings)
            MonthlySpendingByCategorySection(viewModel, amountFormattingSettings = amountFormattingSettings)
            MonthlyIncomeByCategorySection(viewModel = viewModel, amountFormattingSettings = amountFormattingSettings)
        }
    }
}

@Composable
fun MonthlyIncomeByCategorySection(
    viewModel: GraphPageViewModel,
    amountFormattingSettings: AmountFormattingSettings,
) {
    val currentMonth by viewModel.currentIncomeChartMonth.collectAsState()
    val categoryIncome by viewModel.monthlyCategoryIncome.collectAsState()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM yyyy") } // Changed to include year

    LaunchedEffect(Unit) {
        viewModel.refreshSpendingData()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Monthly Income From",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                // fontFamily = myFont, // Removed if not defined globally
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                FilledIconButton(
                    onClick = {
                        viewModel.moveToPreviousMonthForSpending()
                    }
                ) {
                    Icon(painterResource(R.drawable.ic_arrow_back_24px), contentDescription = "Previous Month")
                }
                Text(
                    text = currentMonth.format(dateFormatter),
                    // fontFamily = myFont, // Removed if not defined globally
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val today = remember { LocalDateTime.now() }
                val isCurrentMonth = currentMonth.year == today.year && currentMonth.month == today.month

                FilledIconButton(
                    enabled = !isCurrentMonth, // Disable if it's the current month
                    onClick = {
                        viewModel.moveToNextMonthForSpending()
                    }
                ) {
                    Icon(painterResource(R.drawable.ic_arrow_forward_24px), contentDescription = "Next Month")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            IncomePieChart(categorySpending = categoryIncome, surfaceColor = surfaceColor, amountFormattingSettings = amountFormattingSettings)
            Spacer(modifier = Modifier.height(16.dp))
            CustomPieChartLegend(
                amountFormattingSettings = amountFormattingSettings,
                chartData = categoryIncome.map { (name, value) ->
                    val color = when (name) {
                        "Salary" -> Color(0xFF8BC34A)    // Light Green
                        "Business" -> Color(0xFFFF9800)  // Orange
                        "Rental" -> Color(0xFF795548)    // Brown
                        "Interest" -> Color(0xFF00BCD4)  // Cyan
                        "Dividend" -> Color(0xFF673AB7)  // Deep Purple
                        "Capital" -> Color(0xFFFDD835)   // Yellow
                        "Gifts" -> Color(0xFFAA00FF)     // Vivid Magenta
                        else -> Color(0xFF64B5F6)        // Light Blue as default
                    }
                    PieChartSliceData(value.toFloat(), name, color)
                }.filter { it.value > 0 }
            )
        }
    }
}

@Composable
fun MonthlySpendingByCategorySection(
    viewModel: GraphPageViewModel,
    amountFormattingSettings: AmountFormattingSettings,
) {
    val currentMonth by viewModel.currentSpendingChartMonth.collectAsState()
    val categorySpending by viewModel.monthlyCategorySpendings.collectAsState()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM yyyy") } // Changed to include year

    LaunchedEffect(Unit) {
        viewModel.refreshSpendingData()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Monthly Spending On",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                // fontFamily = myFont, // Removed if not defined globally
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                FilledIconButton(
                    onClick = {
                        viewModel.moveToPreviousMonthForSpending()
                    }
                ) {
                    Icon(painterResource(R.drawable.ic_arrow_back_24px), contentDescription = "Previous Month")
                }
                Text(
                    text = currentMonth.format(dateFormatter),
                    fontFamily = myFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val today = remember { LocalDateTime.now() }
                val isCurrentMonth = currentMonth.year == today.year && currentMonth.month == today.month

                FilledIconButton(
                    enabled = !isCurrentMonth, // Disable if it's the current month
                    onClick = {
                        viewModel.moveToNextMonthForSpending()
                    }
                ) {
                    Icon(painterResource(R.drawable.ic_arrow_forward_24px), contentDescription = "Next Month")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SpendingPieChart(categorySpending = categorySpending, surfaceColor = surfaceColor, amountFormattingSettings = amountFormattingSettings)
            Spacer(modifier = Modifier.height(16.dp))
            CustomPieChartLegend(
                amountFormattingSettings = amountFormattingSettings,
                cSign = "-",
                chartData = categorySpending.map { (name, value) ->
                    // Re-use the color logic from SpendingPieChart
                    val color = when (name) {
                        "Groceries" -> Color(0xFF4CAF50)
                        "Shopping" -> Color(0xFFF44336)
                        "Bills" -> Color(0xFF2196F3)
                        "Fuel" -> Color(0xFFFFEB3B)
                        "Pets" -> Color(0xFF9C27B0)
                        "Restaurant" -> Color(0xFFFF9800)
                        "Alcohol" -> Color(0xFF795548)
                        "Travel" -> Color(0xFF00BCD4)
                        "Child Care" -> Color(0xFFE91E63)
                        "Insurance" -> Color(0xFF00897B)
                        "Subscription" -> Color(0xFF673AB7)
                        "Education" -> Color(0xFFFFC107)
                        "Electronics" -> Color(0xFF3F51B5)
                        "Healthcare" -> Color(0xFF8BC34A)
                        "Investments" -> Color(0xFFD32F2F)
                        "Gifts" -> Color(0xFF1976D2)
                        "Loan" -> Color(0xFFAA00FF)
                        "Rent" -> Color(0xFF388E3C)
                        "Savings" -> Color(0xFFD84315)
                        "Taxes" -> Color(0xFF303F9F)
                        else -> Color(0xFF64B5F6)
                    }
                    PieChartSliceData(value.toFloat(), name, color)
                }.filter { it.value > 0 }
            )
        }
    }
}

@Composable
fun IncomePieChart(categorySpending: Map<String, Double>, surfaceColor: Int, amountFormattingSettings: AmountFormattingSettings) {
    val pieChartData = remember(categorySpending) {
        categorySpending.map { (categoryName, amount) ->
            val color = when (categoryName) {
                "Salary" -> Color(0xFF8BC34A)    // Light Green
                "Business" -> Color(0xFFFF9800)  // Orange
                "Rental" -> Color(0xFF795548)    // Brown
                "Interest" -> Color(0xFF00BCD4)  // Cyan
                "Dividend" -> Color(0xFF673AB7)  // Deep Purple
                "Capital" -> Color(0xFFFDD835)   // Yellow
                "Gifts" -> Color(0xFFAA00FF)     // Vivid Magenta
                else -> Color(0xFF64B5F6)        // Light Blue as default
            }
            PieChartSliceData(amount.toFloat(), categoryName, color)
        }.filter { it.value > 0 }
    }

    if (pieChartData.isNotEmpty()) {
        Column(modifier = Modifier.height(350.dp).fillMaxWidth()) {
            ReusablePieChart(
                cSign = "+",
                chartData = pieChartData,
                holeColor = surfaceColor,
                modifier = Modifier.fillMaxWidth(),
                amountFormattingSettings = amountFormattingSettings,
                selectedLabelTextStyle = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
fun SpendingPieChart(categorySpending: Map<String, Double>, surfaceColor: Int, amountFormattingSettings: AmountFormattingSettings) {
    val pieChartData = remember(categorySpending) {
        categorySpending.map { (categoryName, amount) ->
            val color = when (categoryName) {
                "Groceries" -> Color(0xFF4CAF50)
                "Shopping" -> Color(0xFFF44336)
                "Bills" -> Color(0xFF2196F3)
                "Fuel" -> Color(0xFFFFEB3B)
                "Pets" -> Color(0xFF9C27B0)
                "Restaurant" -> Color(0xFFFF9800)
                "Alcohol" -> Color(0xFF795548)
                "Travel" -> Color(0xFF00BCD4)
                "Child Care" -> Color(0xFFE91E63)
                "Insurance" -> Color(0xFF00897B)
                "Subscription" -> Color(0xFF673AB7)
                "Education" -> Color(0xFFFFC107)
                "Electronics" -> Color(0xFF3F51B5)
                "Healthcare" -> Color(0xFF8BC34A)
                "Investments" -> Color(0xFFD32F2F)
                "Gifts" -> Color(0xFF1976D2)
                "Loan" -> Color(0xFFAA00FF)
                "Rent" -> Color(0xFF388E3C)
                "Savings" -> Color(0xFFD84315)
                "Taxes" -> Color(0xFF303F9F)
                else -> Color(0xFF64B5F6)
            }
            PieChartSliceData(amount.toFloat(), categoryName, color)
        }.filter { it.value > 0 }
    }

    if (pieChartData.isNotEmpty()) {
        Column(modifier = Modifier.height(350.dp).fillMaxWidth()) {
            ReusablePieChart(
                cSign = "+",
                chartData = pieChartData,
                holeColor = surfaceColor,
                modifier = Modifier.fillMaxWidth(), // Chart fills its parent card
                amountFormattingSettings = amountFormattingSettings,
                selectedLabelTextStyle = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
fun MonthlyOverallByCategorySection(
    viewModel: GraphPageViewModel,
    amountFormattingSettings: AmountFormattingSettings
) {
    val currentMonth by viewModel.currentOverallChartMonth.collectAsState()
    val categorySpending by viewModel.monthlyOverall.collectAsState()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM yyyy") } // Changed to include year

    LaunchedEffect(Unit) {
        viewModel.refreshSpendingData()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Monthly Financial Overview",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                // fontFamily = myFont, // Removed if not defined globally
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                FilledIconButton(
                    onClick = {
                        viewModel.moveToPreviousMonthForSpending()
                    }
                ) {
                    Icon(painterResource(R.drawable.ic_arrow_back_24px), contentDescription = "Previous Month")
                }
                Text(
                    text = currentMonth.format(dateFormatter),
                    // fontFamily = myFont, // Removed if not defined globally
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val today = remember { LocalDateTime.now() }
                val isCurrentMonth = currentMonth.year == today.year && currentMonth.month == today.month

                FilledIconButton(
                    enabled = !isCurrentMonth, // Disable if it's the current month
                    onClick = {
                        viewModel.moveToNextMonthForSpending()
                    }
                ) {
                    Icon(painterResource(R.drawable.ic_arrow_forward_24px), contentDescription = "Next Month")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OverallPieChart(categoryOverall = categorySpending, surfaceColor = surfaceColor, amountFormattingSettings = amountFormattingSettings)
            Spacer(modifier = Modifier.height(16.dp))
            CustomPieChartLegend(
                amountFormattingSettings = amountFormattingSettings,
                cSign = "",
                chartData = categorySpending.map { (name, value) ->
                    // Re-use the color logic from SpendingPieChart
                    val color = when (name) {
                        "Income" -> greenColor
                        "Expense" -> redColor
                        else -> blueColor
                    }
                    PieChartSliceData(value.toFloat(), name, color)
                }.filter { it.value > 0 }
            )
        }
    }
}

@Composable
fun OverallPieChart(categoryOverall: Map<String, Double>, surfaceColor: Int, amountFormattingSettings: AmountFormattingSettings) {
    val pieChartData = remember(categoryOverall) {
        categoryOverall.map { (categoryName, amount) ->
            val color = when (categoryName) {
                "Income" -> greenColor
                "Expense" -> redColor
                else -> blueColor
            }
            PieChartSliceData(amount.toFloat(), categoryName, color)
        }.filter { it.value > 0 }
    }

    if (pieChartData.isNotEmpty()) {
        Column(modifier = Modifier.height(350.dp).fillMaxWidth()) {
            ReusablePieChart(
                chartData = pieChartData,
                holeColor = surfaceColor,
                modifier = Modifier.fillMaxWidth(), // Chart fills its parent card
                amountFormattingSettings = amountFormattingSettings,
                selectedLabelTextStyle = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
fun ReusablePieChart(
    cSign: String = "",
    chartData: List<PieChartSliceData>,
    holeColor: Int,
    modifier: Modifier = Modifier,
    selectedLabelTextStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    amountFormattingSettings: AmountFormattingSettings
) {

    if (chartData.isEmpty()) {
        Text("No data to display", style = MaterialTheme.typography.bodySmall)
        return
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        var selectedEntryLabel by remember { mutableStateOf<String?>(null) }
        var selectedEntryValue by remember { mutableStateOf<Float?>(null) }
        val chart = remember { mutableStateOf<PieChart?>(null) }
        var currentlyHighlightedIndex by remember { mutableStateOf<Int?>(-1) }

        AndroidView(
            factory = { context ->
                PieChart(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    description.isEnabled = false
                    isDrawHoleEnabled = true
                    setHoleColor(holeColor)
                    legend.isEnabled = false // <--- IMPORTANT: Disable the default legend
                    setDrawEntryLabels(false)

                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(e: Entry?, h: Highlight?) {
                            if (e is PieEntry) {
                                val index = h?.x?.toInt() ?: -1
                                if (index == currentlyHighlightedIndex) {
                                    highlightValue(null) // Deselect
                                    selectedEntryLabel = null
                                    selectedEntryValue = null
                                    currentlyHighlightedIndex = -1
                                } else {
                                    selectedEntryLabel = e.label
                                    selectedEntryValue = e.value
                                    highlightValue(h) // Select
                                    currentlyHighlightedIndex = index
                                }
                            } else {
                                highlightValue(null)
                                selectedEntryLabel = null
                                selectedEntryValue = null
                                currentlyHighlightedIndex = -1
                            }
                        }

                        override fun onNothingSelected() {
                            // Do nothing on outside click
                        }
                    })
                    chart.value = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { ch ->
                chart.value = ch
                val entries = chartData.map { PieEntry(it.value, it.description) }
                val colors = chartData.map { it.color.toArgb() }

                val dataSet = PieDataSet(entries, "")
                dataSet.colors = colors
                dataSet.sliceSpace = 2f
                dataSet.valueTextSize = 18f
                dataSet.valueTypeface = Typeface.DEFAULT_BOLD
                dataSet.setDrawValues(false)

                val pieData = PieData(dataSet)
                ch.data = pieData

                // Initial selection of the highest value
                if (entries.isNotEmpty() && currentlyHighlightedIndex == -1) {
                    val maxValueEntry = entries.maxByOrNull { it.y }
                    maxValueEntry?.let {
                        val index = entries.indexOf(it).toFloat()
                        val highlight = Highlight(index, 0, -1)
                        ch.highlightValue(highlight)
                        selectedEntryLabel = it.label
                        selectedEntryValue = it.y
                        currentlyHighlightedIndex = index.toInt()
                    }
                } else if (currentlyHighlightedIndex != -1 && currentlyHighlightedIndex!! < entries.size) {
                    ch.highlightValue(Highlight(currentlyHighlightedIndex!!.toFloat(), 0, -1))
                    val highlightedEntry = entries[currentlyHighlightedIndex!!]
                    selectedEntryLabel = highlightedEntry.label
                    selectedEntryValue = highlightedEntry.y
                } else {
                    ch.highlightValues(null)
                    selectedEntryLabel = null
                    selectedEntryValue = null
                    currentlyHighlightedIndex = -1
                }
                ch.invalidate()
            }
        )

        if (selectedEntryLabel != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = selectedEntryLabel ?: "",
                    style = selectedLabelTextStyle,
                    textAlign = TextAlign.Center,
                    fontFamily = myFont
                )
                val (sign, currencySymbol, formattedAmount) = formatAmountComponents(selectedEntryValue?.toDouble() ?: 0.0, settings = amountFormattingSettings)
                CustomPrice(
                    sign = cSign,
                    currency = currencySymbol,
                    amount = formattedAmount,
                    cFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    aFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                )
            }
        }
    }
}

@Composable
fun CustomPieChartLegend(
    chartData: List<PieChartSliceData>,
    modifier: Modifier = Modifier,
    cSign: String = "+",
    amountFormattingSettings: AmountFormattingSettings
) {

    val sortedData = chartData.sortedByDescending { it.value }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Display items in 2 columns
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp).heightIn(max = 700.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(sortedData) { slice ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(slice.color, RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${slice.description}: ",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = myFont,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val (sign, currencySymbol, formattedAmount) = formatAmountComponents(slice.value.toDouble(), settings = amountFormattingSettings)
                    CustomPrice(
                        sign = cSign,
                        currency = currencySymbol,
                        amount = formattedAmount,
                        cFontSize = MaterialTheme.typography.bodySmall.fontSize,
                        aFontSize = MaterialTheme.typography.bodySmall.fontSize,
                    )
                }
            }
        }
    }
}
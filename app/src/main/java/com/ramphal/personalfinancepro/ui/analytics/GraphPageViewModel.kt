package com.ramphal.personalfinancepro.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramphal.personalfinancepro.Constant.OverallCat
import com.ramphal.personalfinancepro.Constant.categories
import com.ramphal.personalfinancepro.Constant.incomeCat
import com.ramphal.personalfinancepro.Graph.transactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters


class GraphPageViewModel: ViewModel() {

    private val _currentSpendingChartMonth = MutableStateFlow(LocalDateTime.now())
    val currentSpendingChartMonth: StateFlow<LocalDateTime> = _currentSpendingChartMonth.asStateFlow()
    private val _currentIncomeChartMonth = MutableStateFlow(LocalDateTime.now())
    val currentIncomeChartMonth: StateFlow<LocalDateTime> = _currentIncomeChartMonth.asStateFlow()
    private val _currentOverallChartMonth = MutableStateFlow(LocalDateTime.now())
    val currentOverallChartMonth: StateFlow<LocalDateTime> = _currentOverallChartMonth.asStateFlow()

    fun moveToPreviousMonthForSpending() {
        _currentSpendingChartMonth.value = _currentSpendingChartMonth.value.minusMonths(1)
        _currentIncomeChartMonth.value = _currentIncomeChartMonth.value.minusMonths(1)
        _currentOverallChartMonth.value = _currentOverallChartMonth.value.minusMonths(1)
    }

    fun moveToNextMonthForSpending() {
        _currentSpendingChartMonth.value = _currentSpendingChartMonth.value.plusMonths(1)
        _currentIncomeChartMonth.value = _currentIncomeChartMonth.value.plusMonths(1)
        _currentOverallChartMonth.value = _currentOverallChartMonth.value.plusMonths(1)
    }

    fun refreshSpendingData() {
        _currentSpendingChartMonth.value = _currentSpendingChartMonth.value
        _currentIncomeChartMonth.value = _currentIncomeChartMonth.value
        _currentOverallChartMonth.value = _currentOverallChartMonth.value
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyCategorySpendings: StateFlow<Map<String, Double>> =
        _currentSpendingChartMonth.flatMapLatest { month ->
            // Calculate start and end of the selected month
            val startOfMonth = month.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay()
            val endOfMonth = month.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(23, 59, 59, 999999999) // End of day

            // Create a list of Flows, one for each category's spending
            val categorySpendingFlows = categories.mapIndexed { index, categoryItem ->
                transactionRepository.getCategorySpendingInDateRange(
                    categoryIndex = index,
                    startDate = startOfMonth,
                    endDate = endOfMonth
                ).map { totalSpending ->
                    categoryItem.name to totalSpending // Pair category name with its spending
                }
            }

            // Combine all category spending flows into a single Flow of Map
            combine(categorySpendingFlows) { resultsArray ->
                resultsArray.toMap() // Convert array of pairs to a Map
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyCategoryIncome: StateFlow<Map<String, Double>> =
        _currentIncomeChartMonth.flatMapLatest { month ->
            // Calculate start and end of the selected month
            val startOfMonth = month.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay()
            val endOfMonth = month.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(23, 59, 59, 999999999) // End of day

            val categoryIncomeFlows = incomeCat.mapIndexed { index, categoryItem ->
                transactionRepository.getCategoryIncomeInDateRange(
                    categoryIndex = index,
                    startDate = startOfMonth,
                    endDate = endOfMonth
                ).map { totalSpending ->
                    categoryItem.name to totalSpending // Pair category name with its spending
                }
            }

            // Combine all category spending flows into a single Flow of Map
            combine(categoryIncomeFlows) { resultsArray ->
                resultsArray.toMap() // Convert array of pairs to a Map
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyOverall: StateFlow<Map<String, Double>> =
        _currentOverallChartMonth.flatMapLatest { month ->
            // Calculate start and end of the selected month
            val startOfMonth = month.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay()
            val endOfMonth = month.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(23, 59, 59, 999999999) // End of day

            val OverallFlows = OverallCat.mapIndexed { index, categoryItem ->
                transactionRepository.getOverallInDateRange(
                    category = categoryItem.name,
                    startDate = startOfMonth,
                    endDate = endOfMonth
                ).map { totalOverall ->
                    categoryItem.name to totalOverall // Pair category name with its spending
                }
            }

            // Combine all category spending flows into a single Flow of Map
            combine(OverallFlows) { resultsArray ->
                resultsArray.toMap() // Convert array of pairs to a Map
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )



}



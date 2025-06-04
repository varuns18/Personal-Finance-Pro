package com.ramphal.personalfinancepro.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.SharedPreferences
import androidx.core.content.edit
import com.ramphal.personalfinancepro.Constant.KEY_AMOUNT_FORMAT
import com.ramphal.personalfinancepro.Constant.KEY_CURRENCY_CODE
import com.ramphal.personalfinancepro.Constant.KEY_DATE_FORMAT_PATTERN
import com.ramphal.personalfinancepro.Constant.PREFS_NAME

// Assume these are somewhere in your ViewModel or shared preferences
enum class CurrencyDisplayPosition {
    PREFIX, SUFFIX
}

enum class AmountFormatType {
    INTERNATIONAL, INDIAN, NONE
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE)

    // Using MutableStateFlow for reactive UI updates
    private val _currentCurrencyCode = MutableStateFlow(
        sharedPreferences.getString(KEY_CURRENCY_CODE, "INR") ?: "INR"
    )
    val currentCurrencyCode: StateFlow<String> = _currentCurrencyCode.asStateFlow()

    // Fixed values, not changeable by user, hence no MutableStateFlow for these
    val fixedCurrencyPosition: CurrencyDisplayPosition = CurrencyDisplayPosition.PREFIX
    val fixedDecimalPlaces: Int = 2

    private val _currentAmountFormat = MutableStateFlow(
        AmountFormatType.valueOf(
            sharedPreferences.getString(KEY_AMOUNT_FORMAT, AmountFormatType.INTERNATIONAL.name)
                ?: AmountFormatType.INTERNATIONAL.name
        )
    )
    val currentAmountFormat: StateFlow<AmountFormatType> = _currentAmountFormat.asStateFlow()

    private val _currentDateFormatPattern = MutableStateFlow(
        sharedPreferences.getString(KEY_DATE_FORMAT_PATTERN, "dd/MM/yyyy") ?: "dd/MM/yyyy"
    )
    val currentDateFormatPattern: StateFlow<String> = _currentDateFormatPattern.asStateFlow()

    fun setCurrencyCode(newCode: String) {
        viewModelScope.launch {
            _currentCurrencyCode.value = newCode
            sharedPreferences.edit { putString(KEY_CURRENCY_CODE, newCode) }
        }
    }

    fun setAmountFormat(newFormat: AmountFormatType) {
        viewModelScope.launch {
            _currentAmountFormat.value = newFormat
            sharedPreferences.edit { putString(KEY_AMOUNT_FORMAT, newFormat.name) }
        }
    }

    fun setDateFormatPattern(newPattern: String) {
        viewModelScope.launch {
            _currentDateFormatPattern.value = newPattern
            sharedPreferences.edit { putString(KEY_DATE_FORMAT_PATTERN, newPattern) }
        }
    }

    // Factory to create ViewModel with Application context
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class AmountFormattingSettings(
    val preferredCurrencyCode: String,
    val currencyPosition: CurrencyDisplayPosition,
    val amountFormatType: AmountFormatType,
    val decimalPlaces: Int
)
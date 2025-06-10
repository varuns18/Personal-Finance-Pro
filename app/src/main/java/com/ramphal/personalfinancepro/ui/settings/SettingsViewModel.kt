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
import android.util.Log
import com.ramphal.personalfinancepro.Constant.KEY_AMOUNT_FORMAT
import com.ramphal.personalfinancepro.Constant.KEY_CURRENCY_CODE
import com.ramphal.personalfinancepro.Constant.KEY_DATE_FORMAT_PATTERN
import com.ramphal.personalfinancepro.Constant.PREFS_NAME
import com.ramphal.personalfinancepro.Graph
import kotlinx.coroutines.flow.firstOrNull

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

    private val _isLoadingCurrency = MutableStateFlow(true)
    val isLoadingCurrency: StateFlow<Boolean> = _isLoadingCurrency.asStateFlow()

    private val _currentCurrencyCode = MutableStateFlow("₹")
    val currentCurrencyCode: StateFlow<String> = _currentCurrencyCode.asStateFlow()

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
        sharedPreferences.getString(KEY_DATE_FORMAT_PATTERN, "dd MMM, yyyy") ?: "dd MMM, yyyy"
    )
    val currentDateFormatPattern: StateFlow<String> = _currentDateFormatPattern.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _isLoadingCurrency.value = true
            try {
                val savedCurrency = sharedPreferences.getString(KEY_CURRENCY_CODE, null)

                if (savedCurrency != null) {
                    _currentCurrencyCode.value = savedCurrency
                    Log.d("SettingsViewModel", "Currency loaded from SharedPreferences: $savedCurrency")
                } else {
                    val onboardingData = Graph.transactionRepository.getOnboardingData(0).firstOrNull()
                    if (onboardingData != null) {
                        _currentCurrencyCode.value = onboardingData.preferredCurrencySymbol
                        Log.d("SettingsViewModel", "Currency loaded from DB (onboarding): ${onboardingData.preferredCurrencySymbol}")
                        // Save this to SharedPreferences immediately so it's prioritized next time
                        sharedPreferences.edit { putString(KEY_CURRENCY_CODE, onboardingData.preferredCurrencySymbol) }
                    } else {
                        Log.d("SettingsViewModel", "No saved currency found. Using default: ${_currentCurrencyCode.value}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error loading settings: ${e.message}")
            } finally {
                _isLoadingCurrency.value = false
            }
        }
    }

    /**
     * Updates the current currency code, saves it to SharedPreferences,
     * and also updates the OnboardingModel in the database for consistency.
     */
    fun setCurrencyCode(newCode: String) {
        viewModelScope.launch {
            _currentCurrencyCode.value = newCode
            // 1. Save the new currency to SharedPreferences (primary user setting storage)
            sharedPreferences.edit { putString(KEY_CURRENCY_CODE, newCode) }
            Log.d("SettingsViewModel", "Currency updated and saved to SharedPreferences: $newCode")

            // 2. Retrieve existing OnboardingModel, update its preferredCurrencySymbol, and save back to DB
            try {
                val onboardingData = Graph.transactionRepository.getOnboardingData(0).firstOrNull()
                if (onboardingData != null) {
                    // Create a copy with the updated preferredCurrencySymbol
                    val updatedOnboardingData = onboardingData.copy(preferredCurrencySymbol = newCode)
                    Graph.transactionRepository.updateOnboardingData(updatedOnboardingData)
                    Log.d("SettingsViewModel", "Onboarding data updated in DB with new currency: $newCode")
                } else {
                    Log.w("SettingsViewModel", "Onboarding data not found in DB to update currency. ID 0 might not exist.")
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error updating onboarding data in DB: ${e.message}")
            }
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

/**
 * Data class to encapsulate all formatting settings for currency and amounts.
 * This is used to pass consolidated settings to UI components.
 */
data class AmountFormattingSettings(
    val preferredCurrencyCode: String,
    val currencyPosition: CurrencyDisplayPosition,
    val amountFormatType: AmountFormatType,
    val decimalPlaces: Int
)
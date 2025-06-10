package com.ramphal.personalfinancepro.ui.onboarding


import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ramphal.personalfinancepro.Constant.getAvailableCurrencyOptions
import com.ramphal.personalfinancepro.Graph.transactionRepository
import com.ramphal.personalfinancepro.data.OnboardingModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Enum to identify which balance field is being updated
enum class BalanceField {
    BANK, CASH, SAVINGS, CREDIT_CARD
}

// Data class to represent the UI state of the onboarding screen
data class OnboardingUiState(
    val preferredCurrencySymbol: String = "$",
    val bankBalanceInput: String = "",
    val cashBalanceInput: String = "",
    val savingsBalanceInput: String = "",
    val hasCreditCard: Boolean = false,
    val creditCardBalanceInput: String = "",
    val isSaving: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val errorMessage: String? = null, // This is the general error message
    // New fields for individual input validation errors
    val bankBalanceError: String? = null,
    val cashBalanceError: String? = null,
    val savingsBalanceError: String? = null,
    val creditCardBalanceError: String? = null
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREF_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(PREF_NAME, Application.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setOnboardingComplete(isComplete: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_ONBOARDING_COMPLETE, isComplete) }
    }

    fun isOnboardingComplete(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    // Function to clear the general error message, called by the UI after display
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- Event Handlers ---

    fun onCurrencySymbolChange(symbol: String) {
        _uiState.update { it.copy(preferredCurrencySymbol = symbol) }
    }

    fun onBalanceInputChange(field: BalanceField, value: String) {
        // Allow only digits and one decimal point
        val filteredValue = value.filter { it.isDigit() || it == '.' }
        if (filteredValue.count { it == '.' } <= 1) {
            _uiState.update { currentState ->
                val (newInput, newError) = validateBalanceInput(filteredValue, field) // Validate as user types
                when (field) {
                    BalanceField.BANK -> currentState.copy(bankBalanceInput = newInput, bankBalanceError = newError)
                    BalanceField.CASH -> currentState.copy(cashBalanceInput = newInput, cashBalanceError = newError)
                    BalanceField.SAVINGS -> currentState.copy(savingsBalanceInput = newInput, savingsBalanceError = newError)
                    BalanceField.CREDIT_CARD -> currentState.copy(creditCardBalanceInput = newInput, creditCardBalanceError = newError)
                }
            }
        }
    }

    fun onCreditCardToggle(hasCard: Boolean) {
        _uiState.update { it.copy(hasCreditCard = hasCard) }
        // Clear credit card balance and its error if toggle is off
        if (!hasCard) {
            _uiState.update { it.copy(creditCardBalanceInput = "", creditCardBalanceError = null) }
        }
    }

    // --- Business Logic ---

    fun completeOnboarding(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null, showSuccessMessage = false) }

        viewModelScope.launch {
            // Re-validate all fields on submission to ensure final data integrity
            // Parse here, but only if validation passes for non-null access later
            val bankBalanceRaw = uiState.value.bankBalanceInput
            val cashBalanceRaw = uiState.value.cashBalanceInput
            val savingsBalanceRaw = uiState.value.savingsBalanceInput
            val creditCardBalanceRaw = uiState.value.creditCardBalanceInput

            val errors = mutableListOf<String>()

            // Full validation for each field
            val bankError = validateOnSubmit(bankBalanceRaw, "Bank balance")
            if (bankError != null) errors.add(bankError)
            val cashError = validateOnSubmit(cashBalanceRaw, "Cash balance")
            if (cashError != null) errors.add(cashError)
            val savingsError = validateOnSubmit(savingsBalanceRaw, "Savings balance")
            if (savingsError != null) errors.add(savingsError)

            // Credit card balance validation only if credit card is enabled
            if (uiState.value.hasCreditCard) {
                val creditCardError = validateOnSubmit(creditCardBalanceRaw, "Credit card balance")
                if (creditCardError != null) errors.add(creditCardError)
            }


            // Update individual errors for UI feedback (This is good, but doesn't stop the flow if errors are present)
            _uiState.update {
                it.copy(
                    bankBalanceError = validateBalanceInput(bankBalanceRaw, BalanceField.BANK).second,
                    cashBalanceError = validateBalanceInput(cashBalanceRaw, BalanceField.CASH).second,
                    savingsBalanceError = validateBalanceInput(savingsBalanceRaw, BalanceField.SAVINGS).second,
                    creditCardBalanceError = if (uiState.value.hasCreditCard) validateBalanceInput(creditCardBalanceRaw, BalanceField.CREDIT_CARD).second else null
                )
            }


            if (errors.isNotEmpty()) {
                // If errors exist, it sets errorMessage and returns early.
                _uiState.update { it.copy(isSaving = false, errorMessage = "Please correct the following issues:\n" + errors.joinToString("\n")) }
                return@launch // THIS PREVENTS onSuccess() from being called
            }

            // Only parse to Double AFTER all validations have passed
            val bankBalance = bankBalanceRaw.toDouble()
            val cashBalance = cashBalanceRaw.toDouble()
            val savingsBalance = savingsBalanceRaw.toDouble()
            val creditCardBalance = if (uiState.value.hasCreditCard) creditCardBalanceRaw.toDouble() else 0.0


            try {
                transactionRepository.addOnboardingData(
                    OnboardingModel(
                        id = 0,
                        preferredCurrencySymbol = uiState.value.preferredCurrencySymbol,
                        bankBalance = bankBalance,
                        cashBalance = cashBalance,
                        savingsBalance = savingsBalance,
                        hasCreditCard = uiState.value.hasCreditCard,
                        creditCardBalance = (creditCardBalance) * -1
                    )
                )
                _uiState.update { it.copy(isSaving = false, showSuccessMessage = true, errorMessage = null) }
                onSuccess() // Callback to trigger navigation
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save data: Please try again. (${e.localizedMessage ?: "Unknown error"})"
                    )
                }
            }
        }
    }

    private fun validateBalanceInput(input: String, field: BalanceField): Pair<String, String?> {
        val error: String? = when {
            input.isEmpty() -> {
                when (field) {
                    // Make initial balances mandatory, credit card optional if toggle is off
                    BalanceField.BANK, BalanceField.CASH, BalanceField.SAVINGS -> "Cannot be empty."
                    BalanceField.CREDIT_CARD -> if (uiState.value.hasCreditCard) "Cannot be empty." else null // Only required if card is enabled
                }
            }
            input.toDoubleOrNull() == null -> "Invalid number."
            else -> null // No error
        }
        return input to error
    }

    // This validation is used specifically for the final submission
    private fun validateOnSubmit(input: String, fieldName: String): String? {
        return when {
            input.isEmpty() -> "$fieldName cannot be empty."
            input.toDoubleOrNull() == null -> "$fieldName must be a valid number."
            else -> null
        }
    }

    fun getSelectedCurrencyDisplayName(symbol: String): String {
        val selectedOption = getAvailableCurrencyOptions().find { it.symbol == symbol }
        return selectedOption?.let { "${it.name} (${it.symbol})" } ?: symbol
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OnboardingViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
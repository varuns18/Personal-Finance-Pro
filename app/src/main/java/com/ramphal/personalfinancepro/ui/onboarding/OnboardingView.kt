package com.ramphal.personalfinancepro.ui.onboarding

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramphal.personalfinancepro.Constant.CurrencyOption
import com.ramphal.personalfinancepro.Constant.getAvailableCurrencyOptions
import com.ramphal.personalfinancepro.R
import com.ramphal.personalfinancepro.ui.home.CustomTopAppBar
import com.ramphal.personalfinancepro.ui.theme.myFont
import kotlinx.coroutines.launch


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingView(
    onSetupComplete: () -> Unit, // Callback for successful setup completion
    onboardingViewModel: OnboardingViewModel,
) {
    val uiState by onboardingViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Add rememberCoroutineScope

    // Effect to show snackbar for general errors
    // Use LaunchedEffect to react to changes in errorMessage
    if (uiState.errorMessage != null) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage!!, // !! because we checked for null
                duration = SnackbarDuration.Long // Show for a long duration
            )
            onboardingViewModel.clearErrorMessage() // Clear the error after showing
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    AddOnboardingTopAppBar(onSaveClick = {
                        onboardingViewModel.completeOnboarding(onSuccess = onSetupComplete)
                    })
                }

                // Currency Preference
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Your Preferred Currency",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = myFont,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            CurrencySymbolSetting(
                                selectedCurrencySymbol = uiState.preferredCurrencySymbol,
                                onCurrencySymbolSelected = onboardingViewModel::onCurrencySymbolChange,
                                availableCurrencyOptions = getAvailableCurrencyOptions(),
                                getSelectedCurrencyDisplayName = onboardingViewModel::getSelectedCurrencyDisplayName
                            )
                        }
                    }
                }


                // Initial Balances
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Your Current Balances",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = myFont,
                                modifier = Modifier.padding(bottom = 4.dp) // Adjusted spacing
                            )
                            Text(
                                text = "Enter your starting balances for an accurate beginning.",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = myFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            BalanceInputField(
                                label = "Bank Balance",
                                value = uiState.bankBalanceInput,
                                onValueChange = { onboardingViewModel.onBalanceInputChange(BalanceField.BANK, it) },
                                currencySymbol = uiState.preferredCurrencySymbol,
                                isError = uiState.bankBalanceError != null,
                                errorMessage = uiState.bankBalanceError
                            )
                            BalanceInputField(
                                label = "Cash Balance",
                                value = uiState.cashBalanceInput,
                                onValueChange = { onboardingViewModel.onBalanceInputChange(BalanceField.CASH, it) },
                                currencySymbol = uiState.preferredCurrencySymbol,
                                isError = uiState.cashBalanceError != null,
                                errorMessage = uiState.cashBalanceError
                            )
                            BalanceInputField(
                                label = "Savings Balance",
                                value = uiState.savingsBalanceInput,
                                onValueChange = { onboardingViewModel.onBalanceInputChange(BalanceField.SAVINGS, it) },
                                currencySymbol = uiState.preferredCurrencySymbol,
                                isError = uiState.savingsBalanceError != null,
                                errorMessage = uiState.savingsBalanceError
                            )
                        }
                    }
                }

                // Credit Card Section
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Credit Card Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = myFont,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Do you have a Credit Card?",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = myFont
                                )
                                Switch(
                                    checked = uiState.hasCreditCard,
                                    onCheckedChange = onboardingViewModel::onCreditCardToggle
                                )
                            }

                            if (uiState.hasCreditCard) {
                                Spacer(modifier = Modifier.height(16.dp))
                                BalanceInputField(
                                    label = "Credit Card Balance",
                                    value = uiState.creditCardBalanceInput,
                                    onValueChange = { onboardingViewModel.onBalanceInputChange(BalanceField.CREDIT_CARD, it) },
                                    currencySymbol = uiState.preferredCurrencySymbol,
                                    isNegativeBalance = true,
                                    isError = uiState.creditCardBalanceError != null,
                                    errorMessage = uiState.creditCardBalanceError
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ... (CurrencySymbolSetting and BalanceInputField remain the same)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySymbolSetting(
    selectedCurrencySymbol: String,
    onCurrencySymbolSelected: (String) -> Unit,
    availableCurrencyOptions: List<CurrencyOption>,
    getSelectedCurrencyDisplayName: (String) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    val currentDisplaySelection = getSelectedCurrencyDisplayName(selectedCurrencySymbol)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Currency Symbol", style = MaterialTheme.typography.bodyLarge, fontFamily = myFont)
        Spacer(Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentDisplaySelection,
                onValueChange = { /* Read-only */ },
                readOnly = true,
                label = { Text("Select Currency", fontFamily = myFont) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor() // Crucial for anchoring the dropdown to the TextField
                    .fillMaxWidth()
            )

            // *** CORRECTED: Use ExposedDropdownMenu instead of DropdownMenu ***
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                // Remove manual offset and modifier.width. ExposedDropdownMenu handles this automatically.
                // offset = DpOffset(x = 0.dp, y = with(density) { rowSize.height.toDp() }),
                // modifier = Modifier.width(with(density) { rowSize.width.toDp() })
            ) {
                // No need for a Column here; ExposedDropdownMenu directly takes its content.
                availableCurrencyOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text("${option.name} (${option.symbol})", fontFamily = myFont) },
                        onClick = {
                            onCurrencySymbolSelected(option.symbol) // Still return only the symbol
                            expanded = false
                        },
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

// --- Reusable Composable for Numeric Balance Input ---
@Composable
fun BalanceInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    currencySymbol: String,
    isNegativeBalance: Boolean = false,
    isError: Boolean = false, // New parameter for error state
    errorMessage: String? = null // New parameter for error message
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val filteredValue = newValue.filter { it.isDigit() || it == '.' }
            if (filteredValue.count { it == '.' } <= 1) {
                onValueChange(filteredValue)
            }
        },
        label = { Text(label, fontFamily = myFont) },
        leadingIcon = { Text(currencySymbol, fontFamily = myFont) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = isError, // Apply error state
        supportingText = { // Display error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = myFont
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun AddOnboardingTopAppBar(onSaveClick: () -> Unit) {
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
                .padding(start = 8.dp, end = 8.dp),
        ) {
            Box(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = "Set Up Your Profile",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontFamily = myFont,
                    fontWeight = FontWeight.Normal
                )
                Column(
                    modifier = Modifier.align(Alignment.CenterStart).padding(bottom = 8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_app_icon),
                        contentDescription = "App logo",
                        modifier = Modifier.size(48.dp)
                    )
                }
                FilledIconButton(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(58.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_save_24px),
                        contentDescription = null
                    )
                }
            }
        }
    }
}
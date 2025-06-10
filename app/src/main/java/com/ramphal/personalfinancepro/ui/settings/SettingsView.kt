package com.ramphal.personalfinancepro.ui.settings


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramphal.personalfinancepro.Constant
import com.ramphal.personalfinancepro.ui.home.CustomTopAppBar
import com.ramphal.personalfinancepro.ui.theme.myFont
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel
) {
    // Collect StateFlows as Compose State
    val currentCurrencyCode by settingsViewModel.currentCurrencyCode.collectAsState()
    val currentAmountFormat by settingsViewModel.currentAmountFormat.collectAsState()
    val currentDateFormatPattern by settingsViewModel.currentDateFormatPattern.collectAsState()

    // Access fixed values directly from ViewModel
    val fixedCurrencyPosition = settingsViewModel.fixedCurrencyPosition
    val fixedDecimalPlaces = settingsViewModel.fixedDecimalPlaces

    // Helper function to get the display name for the selected currency symbol
    val getSelectedCurrencyDisplayName: (String) -> String = { symbol ->
        Constant.getAvailableCurrencyOptions()
            .firstOrNull { it.symbol == symbol }
            ?.let { "${it.name} (${it.symbol})" }
            ?: symbol // Fallback to just the symbol if not found
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        item {
            CustomTopAppBar("Settings")
        }

        item {
            Column(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)
            ) {

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Currency Settings",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // REPLACE CurrencySelectionSetting with CurrencySymbolSetting
                        CurrencySymbolSetting(
                            selectedCurrencySymbol = currentCurrencyCode,
                            onCurrencySymbolSelected = settingsViewModel::setCurrencyCode,
                            availableCurrencyOptions = Constant.getAvailableCurrencyOptions(),
                            getSelectedCurrencyDisplayName = getSelectedCurrencyDisplayName
                        )

                        // Currency Symbol Position (fixed)
                        FixedCurrencyPositionSetting(
                            fixedPosition = fixedCurrencyPosition
                        )
                    }
                }

                // Amount Formatting Card
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Amount Formatting",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Thousand Separator Style
                        AmountFormatSetting(
                            selectedFormat = currentAmountFormat,
                            onFormatSelected = settingsViewModel::setAmountFormat // Pass ViewModel function
                        )

                        // Number of Decimal Places (fixed)
                        FixedDecimalPlacesSetting(
                            fixedDecimalPlaces = fixedDecimalPlaces
                        )
                    }
                }

                // Date Formatting Card
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Date Formatting",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Date Format Pattern
                        DateFormatSetting(
                            selectedPattern = currentDateFormatPattern,
                            onPatternSelected = settingsViewModel::setDateFormatPattern // Pass ViewModel function
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySymbolSetting(
    selectedCurrencySymbol: String,
    onCurrencySymbolSelected: (String) -> Unit,
    availableCurrencyOptions: List<Constant.CurrencyOption>, // Use Constant.CurrencyOption
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

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
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

// --- Reusable Composable for Numeric Balance Input (kept as is, likely in a utility file) ---
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
fun FixedCurrencyPositionSetting(fixedPosition: CurrencyDisplayPosition) {
    val displayNames = mapOf(
        CurrencyDisplayPosition.PREFIX to "Symbol before amount ($100)",
        CurrencyDisplayPosition.SUFFIX to "Symbol after amount (100€)"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Currency Symbol Position", style = MaterialTheme.typography.bodyLarge, fontFamily = myFont) // Added fontFamily
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = displayNames[fixedPosition] ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Position", fontFamily = myFont) }, // Added fontFamily
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
    Spacer(Modifier.height(16.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountFormatSetting(
    selectedFormat: AmountFormatType,
    onFormatSelected: (AmountFormatType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val formatOptions = AmountFormatType.values()
    val displayExamples = mapOf(
        AmountFormatType.INTERNATIONAL to "1,234,567.89 (International)",
        AmountFormatType.INDIAN to "12,34,567.89 (Indian Lakhs/Crores)",
        AmountFormatType.NONE to "1234567.89 (No Separators)"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Thousand Separator Style", style = MaterialTheme.typography.bodyLarge, fontFamily = myFont) // Added fontFamily
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = displayExamples[selectedFormat] ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Format", fontFamily = myFont) }, // Added fontFamily
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                formatOptions.forEach { format ->
                    DropdownMenuItem(
                        text = { Text(displayExamples[format] ?: format.name, fontFamily = myFont) }, // Added fontFamily
                        onClick = {
                            onFormatSelected(format)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
fun FixedDecimalPlacesSetting(fixedDecimalPlaces: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Number of Decimal Places", style = MaterialTheme.typography.bodyLarge, fontFamily = myFont) // Added fontFamily
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = fixedDecimalPlaces.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Decimals", fontFamily = myFont) }, // Added fontFamily
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
    Spacer(Modifier.height(16.dp))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFormatSetting(
    selectedPattern: String,
    onPatternSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatOptions = listOf(
        "dd/MM/yyyy", // e.g., 04/06/2025
        "MM/dd/yyyy", // e.g., 06/04/2025
        "yyyy-MM-dd", // e.g., 2025-06-04
        "dd MMM, yyyy", // e.g., 04 Jun, 2025 (Corrected pattern)
        "MMM dd, yyyy" // e.g., Jun 04, 2025 (Corrected pattern)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Date Display Format", style = MaterialTheme.typography.bodyLarge, fontFamily = myFont) // Added fontFamily
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = try {
                    val exampleDate = java.time.LocalDate.of(2025, 6, 4)
                    exampleDate.format(DateTimeFormatter.ofPattern(selectedPattern))
                } catch (e: Exception) {
                    selectedPattern
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Format", fontFamily = myFont) }, // Added fontFamily
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                dateFormatOptions.forEach { pattern ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${
                                    java.time.LocalDate.of(2025, 6, 4)
                                        .format(DateTimeFormatter.ofPattern(pattern))
                                } ($pattern)", fontFamily = myFont // Added fontFamily
                            )
                        },
                        onClick = {
                            onPatternSelected(pattern)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}
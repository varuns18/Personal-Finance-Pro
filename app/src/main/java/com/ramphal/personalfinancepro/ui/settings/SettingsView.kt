package com.ramphal.personalfinancepro.ui.settings

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Import this
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramphal.personalfinancepro.Constant.fallbackSymbols
import com.ramphal.personalfinancepro.ui.home.CustomTopAppBar
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier,
    settingsViewModel: SettingsViewModel
) {
    // Collect StateFlows as Compose State
    val currentCurrencyCode by settingsViewModel.currentCurrencyCode.collectAsState()
    val currentAmountFormat by settingsViewModel.currentAmountFormat.collectAsState()
    val currentDateFormatPattern by settingsViewModel.currentDateFormatPattern.collectAsState()

    // Access fixed values directly from ViewModel
    val fixedCurrencyPosition = settingsViewModel.fixedCurrencyPosition
    val fixedDecimalPlaces = settingsViewModel.fixedDecimalPlaces

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

                        // Default Currency
                        CurrencySelectionSetting(
                            selectedCurrencyCode = currentCurrencyCode,
                            onCurrencySelected = settingsViewModel::setCurrencyCode // Pass ViewModel function
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
            }

            // Date Formatting Card
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionSetting(
    selectedCurrencyCode: String,
    onCurrencySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currencyOptions = fallbackSymbols.keys.toList()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Default Currency", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            val selectedCurrencySymbol = fallbackSymbols[selectedCurrencyCode] ?: ""
            OutlinedTextField(
                value = "$selectedCurrencyCode ($selectedCurrencySymbol)",
                onValueChange = {},
                readOnly = true,
                label = { Text("Currency") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                currencyOptions.forEach { currencyCode ->
                    val currencySymbol = fallbackSymbols[currencyCode] ?: ""
                    DropdownMenuItem(
                        text = { Text("$currencyCode ($currencySymbol)") },
                        onClick = {
                            onCurrencySelected(currencyCode)
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
fun FixedCurrencyPositionSetting(fixedPosition: CurrencyDisplayPosition) {
    val displayNames = mapOf(
        CurrencyDisplayPosition.PREFIX to "Symbol before amount ($100)",
        CurrencyDisplayPosition.SUFFIX to "Symbol after amount (100€)"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Currency Symbol Position", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = displayNames[fixedPosition] ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Position") },
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
        Text("Thousand Separator Style", style = MaterialTheme.typography.bodyLarge)
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
                label = { Text("Format") },
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
                        text = { Text(displayExamples[format] ?: format.name) },
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
        Text("Number of Decimal Places", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = fixedDecimalPlaces.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Decimals") },
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
        Text("Date Display Format", style = MaterialTheme.typography.bodyLarge)
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
                label = { Text("Format") },
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
                                } ($pattern)"
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
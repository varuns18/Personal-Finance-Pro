package com.ramphal.personalfinancepro.ui.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.ramphal.personalfinancepro.Constant
import com.ramphal.personalfinancepro.R
import com.ramphal.personalfinancepro.data.OnboardingModel
import com.ramphal.personalfinancepro.data.TransactionModel
import com.ramphal.personalfinancepro.ui.history.formatDateTime
import com.ramphal.personalfinancepro.ui.home.CustomPrice
import com.ramphal.personalfinancepro.ui.home.formatAmountComponents
import com.ramphal.personalfinancepro.ui.settings.AmountFormattingSettings
import com.ramphal.personalfinancepro.ui.theme.myFont
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionView(
    viewModel: AddTransactionViewModel,
    navController: NavHostController,
    amountFormattingSettings: AmountFormattingSettings,
    currentDateFormat: String,
    message: (String) -> Unit,
    transactionId: Long
) {
    var editModeData: TransactionModel? = null
    if (transactionId != -1L){
        editModeData = viewModel.getTransactionById(transactionId).collectAsState(initial = null).value
    }
    val lazyListState = rememberLazyListState()
    var selectedIndex by remember { mutableIntStateOf(0) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateSelected by remember { mutableStateOf(LocalDateTime.now())}
    var showDatePicker by remember { mutableStateOf(false) }
    var overallBalance = viewModel.getOverallBalance().collectAsState(
        initial = OnboardingModel(
            id = 0,
            preferredCurrencySymbol = "$",
            bankBalance = 0.0,
            cashBalance = 0.0,
            savingsBalance = 0.0,
            hasCreditCard = false,
            creditCardBalance = 0.0
        )
    )

    var transactionFromIndex by remember { mutableIntStateOf(0) }
    var transactionFromName by remember { mutableStateOf("Account") }
    var transactionFromIcon by remember { mutableIntStateOf(R.drawable.ic_bank_account_24px) }

    var transactionToIndex by remember { mutableIntStateOf(-1) }
    var transactionToName by remember { mutableStateOf("Category") }
    var transactionToIcon by remember { mutableIntStateOf(R.drawable.ic_category_24px) }

    var openCatBottomSheet by remember { mutableStateOf(false) }
    var openBankBottomSheet by remember { mutableStateOf(false) }
    var openBankBottomSheet2 by remember { mutableStateOf(false) }
    var openIncomeCatBottomSheet by remember { mutableStateOf(false) }
    val catBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardManager = LocalSoftwareKeyboardController.current

    LaunchedEffect(selectedIndex) {
        if (selectedIndex == 0) {
            transactionFromIndex = 0
            transactionFromName = "Bank"
            transactionFromIcon = R.drawable.ic_bank_account_24px
            transactionToIndex = -1
            transactionToName = "Category"
            transactionToIcon = R.drawable.ic_category_24px
        } else if (selectedIndex == 1) {
            transactionFromIndex = -1
            transactionFromName = "Category"
            transactionFromIcon = R.drawable.ic_category_24px
            transactionToIndex = 0
            transactionToName = "Bank"
            transactionToIcon = R.drawable.ic_bank_account_24px
        } else {
            transactionFromIndex = 0
            transactionFromName = "Bank"
            transactionFromIcon = R.drawable.ic_bank_account_24px
            transactionToIndex = 0
            transactionToName = "Bank"
            transactionToIcon = R.drawable.ic_bank_account_24px
        }
    }


    var initialized by remember { mutableStateOf(false) }
    LaunchedEffect(editModeData) {
        if (transactionId != -1L && editModeData != null && !initialized) {
            amount = editModeData.amount
            note = editModeData.note ?: ""
            dateSelected = editModeData.timestamp
            selectedIndex = if (editModeData.type == "Expense") 0 else if (editModeData.type == "Income") 1 else 2
            transactionFromIndex = editModeData.from
            transactionToIndex = editModeData.to
            if (selectedIndex == 0){
                transactionFromName = Constant.accountItems[transactionFromIndex].name
                transactionToName = Constant.categories[transactionToIndex].name
                transactionFromIcon = Constant.accountItems[transactionFromIndex].icon
                transactionToIcon = Constant.categories[transactionToIndex].icon
            }else if (selectedIndex == 1){
                transactionFromName = Constant.incomeCat[transactionFromIndex].name
                transactionToName = Constant.accountItems[transactionToIndex].name
                transactionFromIcon = Constant.incomeCat[transactionFromIndex].icon
                transactionToIcon = Constant.accountItems[transactionToIndex].icon
            } else{
                transactionFromName = Constant.accountItems[transactionFromIndex].name
                transactionToName = Constant.accountItems[transactionToIndex].name
                transactionFromIcon = Constant.accountItems[transactionFromIndex].icon
                transactionToIcon = Constant.accountItems[transactionToIndex].icon
            }
        }
    }

    val datePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Picker)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            dateSelected = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                        }

                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("CANCEL")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
    ){
        LazyColumn(
            state = lazyListState
        ) {
            stickyHeader {
                AddTransactionTopAppBar(
                    onBackClick = { navController.navigateUp() },
                    onSaveClick = {
                        if (transactionId == -1L){
                            savaNewTransaction(
                                amount = amount,
                                keyboardManager = keyboardManager,
                                message = message,
                                selectedIndex = selectedIndex,
                                transactionFromIndex = transactionFromIndex,
                                transactionToIndex = transactionToIndex,
                                dateSelected = dateSelected,
                                navController = navController,
                                overallBalance = overallBalance,
                                viewModel = viewModel,
                                note = note
                            )
                        }else {
                            updateTransaction(
                                amount = amount,
                                keyboardManager = keyboardManager,
                                message = message,
                                selectedIndex = selectedIndex,
                                transactionFromIndex = transactionFromIndex,
                                transactionToIndex = transactionToIndex,
                                dateSelected = dateSelected,
                                navController = navController,
                                overallBalance = overallBalance,
                                viewModel = viewModel,
                                note = note,
                                editModeData = editModeData
                            )
                        }

                    }
                )
            }
            stickyHeader {
                Card(
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Transaction type",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = myFont,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        SingleChoiceSegmentedButton(
                            selectedIndex = selectedIndex,
                            onSelectionChanged = {
                                selectedIndex = it
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Amount",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = myFont,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )
                        AmountBox(
                            amount = amount,
                            focusManager = focusManager,
                            focusRequester = focusRequester,
                            onAmountChange = {amount = it},
                            amountFormattingSettings = amountFormattingSettings
                        )
                        Text(
                            text = if (selectedIndex == 0) "Spent from" else if(selectedIndex == 1) "Received from" else "from (self transfer)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = myFont,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )
                        TransactionFrom(
                            transactionFromName = transactionFromName,
                            transactionFromIcon = transactionFromIcon,
                            transactionToName = transactionToName,
                            transactionToIcon = transactionToIcon,
                            onActClick = {
                                keyboardManager?.hide()
                                if (selectedIndex == 0) openBankBottomSheet = true
                                else if (selectedIndex == 1) openIncomeCatBottomSheet = true
                                else openBankBottomSheet = true
                            },
                            onCatClick = {
                                keyboardManager?.hide()
                                if (selectedIndex == 0) openCatBottomSheet = true
                                else if (selectedIndex == 1) openBankBottomSheet = true
                                else openBankBottomSheet2 = true
                            },
                        )
                        Text(
                            text = "Date",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = myFont,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )
                        DateSelected(date = formatDateTime(dateSelected, dateFormatPattern = currentDateFormat), onDateClick = {
                            keyboardManager?.hide()
                            showDatePicker = true
                        })
                        Text(
                            text = "Note",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = myFont,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )
                        NoteBox(
                            note = note,
                            onNoteChange = {
                                note = it
                            }
                        )
                        if (openCatBottomSheet){
                            CategoryBottomSheet(
                                bottomSheetState = catBottomSheetState,
                                onDismissRequest = { openCatBottomSheet = false },
                                onItemClick = {
                                    transactionToIndex = it
                                    transactionToIcon = Constant.categories[it].icon
                                    transactionToName = Constant.categories[it].name
                                    openCatBottomSheet = false
                                }
                            )
                        }
                        if (openBankBottomSheet){
                            BankBottomSheet(
                                showCreditOption = {
                                    if (selectedIndex == 0) true
                                    else false
                                },
                                bottomSheetState = catBottomSheetState,
                                onDismissRequest = { openBankBottomSheet = false },
                                onItemClick = {
                                    if (selectedIndex == 0) {
                                        transactionFromIndex = it
                                        transactionFromIcon = Constant.accountItems[it].icon
                                        transactionFromName = Constant.accountItems[it].name
                                    } else if (selectedIndex == 1) {
                                        transactionToIndex = it
                                        transactionToIcon = Constant.accountItems[it].icon
                                        transactionToName = Constant.accountItems[it].name
                                    } else {
                                        transactionFromIndex = it
                                        transactionFromIcon = Constant.accountItems[it].icon
                                        transactionFromName = Constant.accountItems[it].name
                                    }
                                    openBankBottomSheet = false
                                },
                                overallBalance = overallBalance.value,
                                settings = amountFormattingSettings
                            )
                        }
                        if (openBankBottomSheet2){
                            BankBottomSheet(
                                showCreditOption = {
                                    false
                                },
                                bottomSheetState = catBottomSheetState,
                                onDismissRequest = { openBankBottomSheet2 = false },
                                onItemClick = {
                                    transactionToIndex = it
                                    transactionToIcon = Constant.accountItems[it].icon
                                    transactionToName = Constant.accountItems[it].name
                                    openBankBottomSheet2 = false
                                },
                                showSavingOption = { false },
                                overallBalance = overallBalance.value,
                                settings = amountFormattingSettings
                            )
                        }
                        if (openIncomeCatBottomSheet){
                            IncomeCategoryBottomSheet(
                                bottomSheetState = catBottomSheetState,
                                onDismissRequest = {openIncomeCatBottomSheet = false},
                                onItemClick = {
                                    transactionFromIndex = it
                                    transactionFromIcon = Constant.incomeCat[it].icon
                                    transactionFromName = Constant.incomeCat[it].name
                                    openIncomeCatBottomSheet = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeCategoryBottomSheet(
    bottomSheetState: SheetState,
    onDismissRequest: () -> Unit,
    onItemClick: (Int) -> Unit
){
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = onDismissRequest,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
                Text(text = "Select income category", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ){
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            state = rememberLazyGridState()
        ) {
            items(Constant.incomeCat.size){
                IncomeCatGridItem(
                    icon = Constant.incomeCat[it].icon,
                    text = Constant.incomeCat[it].name,
                    onItemClick = { onItemClick(it) }
                )
            }
        }
    }
}

@Composable
fun IncomeCatGridItem(
    icon: Int,
    text: String,
    onItemClick: () -> Unit
){
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable(
                enabled = true,
                onClick = onItemClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FilledTonalIconButton(
            onClick = onItemClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = text
            )
        }
        Text(
            text = text,
            fontFamily = myFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankBottomSheet(
    showCreditOption: () -> Boolean = { false },
    showSavingOption: () -> Boolean = { false },
    bottomSheetState: SheetState,
    onDismissRequest: () -> Unit,
    onItemClick: (Int) -> Unit,
    overallBalance: OnboardingModel,
    settings: AmountFormattingSettings
){
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = onDismissRequest,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
                Text(text = "Select payment mode", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ){
        Column(
            modifier = Modifier.padding(start = 4.dp, end = 8.dp)
        ) {
            BankGridItem(
                icon = Constant.accountItems[0].icon,
                text = Constant.accountItems[0].name,
                balance = overallBalance.bankBalance,
                onItemClick = { onItemClick(0) },
                settings = settings
            )
            if (showSavingOption()){
                BankGridItem(
                    icon = Constant.accountItems[1].icon,
                    text = Constant.accountItems[1].name,
                    balance = overallBalance.savingsBalance,
                    onItemClick = { onItemClick(1) },
                    settings = settings
                )
            }
            BankGridItem(
                icon = Constant.accountItems[2].icon,
                text = Constant.accountItems[2].name,
                balance = overallBalance.cashBalance,
                onItemClick = { onItemClick(2) },
                settings = settings
            )
            if (showCreditOption()){
                BankGridItem(
                    icon = Constant.accountItems[3].icon,
                    text = Constant.accountItems[3].name,
                    balance = overallBalance.creditCardBalance,
                    onItemClick = { onItemClick(3) },
                    settings = settings
                )
            }
        }
    }
}

@Composable
fun BankGridItem(
    icon: Int,
    text: String,
    onItemClick: () -> Unit,
    balance: Double,
    settings: AmountFormattingSettings
){
    val (sign, symbol, amount) = remember(settings) { // Added totalBalance as a key
        formatAmountComponents(
            balance,
            settings = settings,
        )
    }
    Row(
        modifier = Modifier
            .padding(8.dp)
            .clickable(
                enabled = true,
                onClick = onItemClick
            )
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(
            onClick = onItemClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = text
            )
        }
        Text(
            text = text,
            fontFamily = myFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(start = 8.dp)
                .width(0.dp)
                .weight(1f),
            textAlign = TextAlign.Left
        )
        CustomPrice(
            sign = sign,
            currency = symbol,
            amount = amount,
            cFontSize = 16.sp,
            aFontSize = 16.sp,
            modifier = Modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBottomSheet(
    bottomSheetState: SheetState,
    onDismissRequest: () -> Unit,
    onItemClick: (Int) -> Unit
){
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = onDismissRequest,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
                Text(text = "Select spend category", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ){
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            state = rememberLazyGridState()
        ) {
            items(Constant.categories.size){
                GridItem(
                    icon = Constant.categories[it].icon,
                    text = Constant.categories[it].name,
                    onItemClick = { onItemClick(it) }
                )
            }
        }
    }
}

@Composable
fun GridItem(
    icon: Int,
    text: String,
    onItemClick: () -> Unit
){
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable(
                enabled = true,
                onClick = onItemClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FilledTonalIconButton(
            onClick = onItemClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = text
            )
        }
        Text(
            text = text,
            fontFamily = myFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun NoteBox(
    note: String,
    onNoteChange: (String) -> Unit
){
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .height(150.dp),
        shape = RoundedCornerShape(16.dp),
        textStyle = TextStyle(
            fontFamily = myFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    )
}

@Composable
fun DateSelected(
    date: String,
    onDateClick: () -> Unit
){
    OutlinedButton(
        onClick = onDateClick,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ){
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_calendar_24px),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = date.toString(),
            fontFamily = myFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier
                .width(0.dp)
                .weight(1f),
            textAlign = TextAlign.Left
        )
    }
}

@Composable
fun AmountBox(
    amount: String,
    focusManager: FocusManager,
    focusRequester: FocusRequester,
    onAmountChange: (String) -> Unit,
    amountFormattingSettings: AmountFormattingSettings
){
    val (sign, symbol, notamount) = remember(200) {
        formatAmountComponents(
            200.5,
            settings = amountFormattingSettings,
        )
    }
    val haptic = LocalHapticFeedback.current
    var shakeState by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = amount,
        onValueChange = { new ->
            if (new.length <= 9) {
                onAmountChange(new)
                shakeState = false
            } else {
                // Trigger haptic feedback
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                // Trigger shake animation
                shakeState = true
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        prefix = {
            Text(
                symbol,
                fontFamily = myFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp
            )
        },
        textStyle = TextStyle(
            fontFamily = myFont,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        ),
        maxLines = 1,
    )
}

@Composable
fun SingleChoiceSegmentedButton(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit
) {
    // The two options, with their drawable IDs
    val options = listOf(
        "Expense"  to R.drawable.send_24px,
        "Income" to R.drawable.receive_24px,
        "Transfer" to R.drawable.self_transfer_24px
    )

    MultiChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        options.forEachIndexed { index, (label, iconRes) ->
            val checked = (index == selectedIndex)

            // build a shape that is square on the “midnight” side
            val segmentShape = when (index) {
                0 -> RoundedCornerShape(
                    topStart     = 16.dp,
                    bottomStart  = 16.dp,
                    topEnd       = 0.dp,
                    bottomEnd    = 0.dp
                )
                options.lastIndex -> RoundedCornerShape(
                    topStart     = 0.dp,
                    bottomStart  = 0.dp,
                    topEnd       = 16.dp,
                    bottomEnd    = 16.dp
                )
                else -> RoundedCornerShape(0.dp) // for more than two items, fully square in the middle
            }

            SegmentedButton(
                shape           = segmentShape,
                checked         = checked,
                onCheckedChange = { onSelectionChanged(index) },
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor    = MaterialTheme.colorScheme.primary,
                    activeContentColor      = MaterialTheme.colorScheme.onPrimary,
                    activeBorderColor       = MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor  = Color.Transparent,
                    inactiveContentColor    = MaterialTheme.colorScheme.onSurfaceVariant,
                    inactiveBorderColor     = MaterialTheme.colorScheme.outline
                ),
                icon = {
                    if (checked){
                        Icon(
                            imageVector = ImageVector.vectorResource(id = iconRes),
                            contentDescription = "$label selected",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text     = label,
                        style    = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 6.dp),
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = myFont,
                    )
                },
                modifier = Modifier.height(54.dp),
            )
        }
    }
}


@Composable
fun TransactionFrom(
    onCatClick: () -> Unit,
    onActClick: () -> Unit,
    transactionFromName: String,
    transactionFromIcon: Int,
    transactionToName: String,
    transactionToIcon: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Button(
            onClick = onActClick,
            modifier = Modifier
                .width(0.dp)
                .height(54.dp)
                .weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(transactionFromIcon),
                contentDescription = null,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = transactionFromName,
                fontFamily = myFont,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(text = " to ", fontFamily = myFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Button(
            onClick = onCatClick,
            modifier = Modifier
                .width(0.dp)
                .height(54.dp)
                .weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(transactionToIcon),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = transactionToName, fontFamily = myFont, fontWeight = FontWeight.SemiBold)
        }
    }
}


@Composable
fun AddTransactionTopAppBar(onSaveClick: () -> Unit, onBackClick: () -> Unit) {
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
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = "Add Transaction",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontFamily = myFont,
                    fontWeight = FontWeight.Normal
                )
                FilledTonalIconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back_24px),
                        contentDescription = null
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

fun updateBalanceForAccount(
    currentOnboarding: OnboardingModel,
    accountIndex: Int,
    amount: Double // This amount can be positive (for additions) or negative (for deductions)
): OnboardingModel {
    return when (accountIndex) {
        0 -> currentOnboarding.copy(bankBalance = currentOnboarding.bankBalance + amount)
        1 -> currentOnboarding.copy(savingsBalance = currentOnboarding.savingsBalance + amount)
        2 -> currentOnboarding.copy(cashBalance = currentOnboarding.cashBalance + amount)
        3 -> currentOnboarding.copy(creditCardBalance = currentOnboarding.creditCardBalance + amount)
        else -> currentOnboarding // Fallback for unexpected index; consider throwing an error if strict
    }
}

fun updateTransaction(
    amount: String,
    keyboardManager: SoftwareKeyboardController?,
    message: (String) -> Unit,
    selectedIndex: Int,
    transactionFromIndex: Int,
    transactionToIndex: Int,
    dateSelected: LocalDateTime,
    navController: NavController,
    overallBalance: State<OnboardingModel>, // Consider passing overallBalance as a ViewModel state
    viewModel: AddTransactionViewModel, // This function should ideally be part of the ViewModel
    note: String,
    editModeData: TransactionModel?,
) {
    val now = LocalDateTime.now()
    val parsedAmount = amount.toDoubleOrNull()

    // --- 1. Initial Input Validation ---
    if (amount.isEmpty()) {
        keyboardManager?.hide()
        message("Please enter amount")
        return // Exit early
    } else if (parsedAmount == null || parsedAmount <= 0.0) {
        keyboardManager?.hide()
        message("Please enter a valid amount greater than 0")
        return // Exit early
    }

    // Ensure we are in edit mode, otherwise this function's purpose is not met.
    if (editModeData == null) {
        message("Error: No transaction data to edit.")
        return
    }

    val newTransactionType = when (selectedIndex) {
        0 -> "Expense"
        1 -> "Income"
        else -> "Transfer"
    }

    // --- 2. Type-Specific Validation ---
    when (newTransactionType) {
        "Expense" -> {
            if (transactionToIndex == -1) { // 'to' is category for Expense
                keyboardManager?.hide()
                message("Please select category")
                return
            }
            if (transactionFromIndex == 9 && transactionToIndex == 1) {
                keyboardManager?.hide()
                message("Please select different category or different account")
                return
            }
            if (transactionFromIndex == 10 && transactionToIndex == 4) {
                keyboardManager?.hide()
                message("Please select different category or different account")
                return
            }
        }
        "Income" -> {
            if (transactionFromIndex == -1) { // 'from' is category for Income
                keyboardManager?.hide()
                message("Please select category")
                return
            }
        }
        "Transfer" -> {
            if (transactionFromIndex == -1 || transactionToIndex == -1) {
                keyboardManager?.hide()
                message("Please select both 'From' and 'To' accounts")
                return
            } else if (transactionFromIndex == transactionToIndex) {
                keyboardManager?.hide()
                message("'From' and 'To' accounts cannot be the same")
                return
            }
        }
    }

    val newIsScheduled = dateSelected.isAfter(now)
    var updatedOverallBalance = overallBalance.value.copy()

    // --- 3. Revert Old Transaction's Balance Impact (if it was a completed transaction) ---
    val oldTxWasCompleted = !editModeData.isScheduled
    if (oldTxWasCompleted) {
        updatedOverallBalance = reverseBalanceImpact(updatedOverallBalance, editModeData)
    }

    // --- 4. Apply New Transaction's Balance Impact (if the new transaction is completed) ---
    if (!newIsScheduled) { // Only apply if the new transaction is not scheduled for the future
        updatedOverallBalance = applyBalanceImpact(
            currentOnboarding = updatedOverallBalance,
            type = newTransactionType,
            fromIndex = transactionFromIndex,
            toIndex = transactionToIndex,
            amount = parsedAmount
        )
    }

    // --- 5. Update Overall Balance in Database (if any changes were made that affect balance) ---
    // Update balance if the old transaction was completed OR if the new transaction is completed
    val shouldUpdateOverallBalanceDb = oldTxWasCompleted || !newIsScheduled
    if (shouldUpdateOverallBalanceDb) {
        viewModel.updateOverallBalance(updatedOverallBalance)
    }

    // --- 6. Create Updated Transaction Model and Save to Database ---
    val transactionToSave = TransactionModel(
        id = editModeData.id, // Keep the original ID
        type = newTransactionType,
        from = transactionFromIndex,
        to = transactionToIndex,
        timestamp = dateSelected,
        amount = amount, // Use the string amount
        note = note.trim(),
        isScheduled = newIsScheduled // Set based on the new date
    )

    // Call ViewModel to update the transaction in the database
    viewModel.updateTransaction(transactionToSave) // This should be a suspend function in ViewModel

    // --- 7. Final User Feedback and Navigation ---
    keyboardManager?.hide()
    if (transactionToSave.isScheduled) {
        message("Transaction scheduled for ${dateSelected.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}")
    } else {
        message("Transaction updated successfully!")
    }
    navController.navigateUp() // Go back to the previous screen


}

fun reverseBalanceImpact(currentOnboarding: OnboardingModel, oldTx: TransactionModel): OnboardingModel {
    var updatedModel = currentOnboarding.copy()
    val oldAmount = oldTx.amount.toDouble()
    when (oldTx.type) {
        "Expense" -> {
            // Old Expense deducted from 'from' account. Add it back.
            updatedModel = updateBalanceForAccount(updatedModel, oldTx.from, oldAmount)
        }
        "Income" -> {
            // Old Income added to 'to' account. Deduct it.
            updatedModel = updateBalanceForAccount(updatedModel, oldTx.to, -oldAmount)
        }
        "Transfer" -> {
            // Old Transfer deducted from 'from' and added to 'to'.
            // Reverse: add back to 'from', deduct from 'to'.
            updatedModel = updateBalanceForAccount(updatedModel, oldTx.from, oldAmount)
            updatedModel = updateBalanceForAccount(updatedModel, oldTx.to, -oldAmount)
        }
    }
    return updatedModel
}

fun applyBalanceImpact(
    currentOnboarding: OnboardingModel,
    type: String,
    fromIndex: Int,
    toIndex: Int,
    amount: Double
): OnboardingModel {
    var updatedModel = currentOnboarding.copy()
    when (type) {
        "Expense" -> {
            // Expense: deduct from 'from' account
            updatedModel = updateBalanceForAccount(updatedModel, fromIndex, -amount)
        }
        "Income" -> {
            // Income: add to 'to' account
            updatedModel = updateBalanceForAccount(updatedModel, toIndex, amount)
        }
        "Transfer" -> {
            // Transfer: deduct from 'from', add to 'to'
            updatedModel = updateBalanceForAccount(updatedModel, fromIndex, -amount)
            updatedModel = updateBalanceForAccount(updatedModel, toIndex, amount)
        }
    }
    return updatedModel
}

fun savaNewTransaction(
    amount: String,
    keyboardManager:  SoftwareKeyboardController?,
    message: (String) -> Unit,
    selectedIndex: Int,
    transactionFromIndex: Int,
    transactionToIndex: Int,
    dateSelected: LocalDateTime,
    navController: NavController,
    overallBalance: State<OnboardingModel>,
    viewModel: AddTransactionViewModel,
    note: String,
){
    val now = LocalDateTime.now()
    val parsedAmount = amount.toDoubleOrNull()
    if (amount.isEmpty()) {
        keyboardManager?.hide()
        message("Please enter amount")
    } else if (parsedAmount == null || parsedAmount <= 0.0) {
        keyboardManager?.hide()
        message("Please enter a valid amount greater than 0")
    } else{
        when (selectedIndex) {
            0 -> { // --- Expense ---
                if (transactionToIndex == -1) { // 'to' is category for Expense
                    keyboardManager?.hide()
                    message("Please select category")
                } else if (transactionFromIndex == 9 && transactionToIndex == 1){
                    keyboardManager?.hide()
                    message("Please select different category or different account")
                } else if (transactionFromIndex == 10 && transactionToIndex == 4){
                    keyboardManager?.hide()
                    message("Please select different category or different account")
                } else {
                    if (dateSelected.isAfter(now)) {
                        // --- UPCOMING Expense ---
                        viewModel.addTransaction(
                            TransactionModel(
                                type = "Expense",
                                from = transactionFromIndex,
                                to = transactionToIndex,
                                timestamp = dateSelected,
                                amount = parsedAmount.toString(),
                                note = note.trim(),
                                isScheduled = true
                            )
                        )
                        keyboardManager?.hide()
                        message("Expense scheduled for ${dateSelected.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}")
                        navController.navigateUp()
                    } else {
                        // --- COMPLETED Expense ---
                        val onboardingModel = overallBalance.value // Get current balance
                        var updatedOnboardingModel = onboardingModel.copy()

                        updatedOnboardingModel = updateBalanceForAccount(
                            currentOnboarding = updatedOnboardingModel,
                            accountIndex = transactionFromIndex,
                            amount = -parsedAmount // Deduct from the 'from' account
                        )
                        viewModel.updateOverallBalance(updatedOnboardingModel)

                        viewModel.addTransaction(
                            TransactionModel(
                                type = "Expense",
                                from = transactionFromIndex,
                                to = transactionToIndex,
                                timestamp = dateSelected,
                                amount = parsedAmount.toString(),
                                note = note.trim(),
                                isScheduled = false
                            )
                        )
                        keyboardManager?.hide()
                        message("Expense added successfully!")
                        navController.navigateUp()
                    }
                }
            }

            1 -> { // --- Income ---
                if (transactionFromIndex == -1) { // 'from' is category for Income
                    keyboardManager?.hide()
                    message("Please select category")
                } else {
                    if (dateSelected.isAfter(now)) {
                        // --- UPCOMING Income ---
                        viewModel.addTransaction(
                            TransactionModel(
                                type = "Income", // Type remains "Income"
                                from = transactionFromIndex, // Category for income
                                to = transactionToIndex, // Account where income is added
                                timestamp = dateSelected,
                                amount = parsedAmount.toString(), // Use the parsed Double amount
                                note = note.trim(),
                                isScheduled = true // Mark as scheduled
                            )
                        )
                        keyboardManager?.hide()
                        message("Income scheduled for ${dateSelected.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}")
                        navController.navigateUp()
                    } else {
                        // --- COMPLETED Income ---
                        val onboardingModel = overallBalance.value // Get current balance
                        var updatedOnboardingModel = onboardingModel.copy()

                        updatedOnboardingModel = updateBalanceForAccount(
                            currentOnboarding = updatedOnboardingModel,
                            accountIndex = transactionToIndex, // Add to the 'to' account
                            amount = parsedAmount // Add the amount
                        )
                        viewModel.updateOverallBalance(updatedOnboardingModel)

                        viewModel.addTransaction(
                            TransactionModel(
                                type = "Income",
                                from = transactionFromIndex,
                                to = transactionToIndex,
                                timestamp = dateSelected,
                                amount = parsedAmount.toString(), // Use the parsed Double amount
                                note = note.trim(),
                                isScheduled = false // Not scheduled
                            )
                        )
                        keyboardManager?.hide()
                        message("Income added successfully!")
                        navController.navigateUp()
                    }
                }
            }

            else -> {
                if (transactionFromIndex == transactionToIndex) {
                    keyboardManager?.hide()
                    message("'From' and 'To' accounts cannot be the same")
                } else {
                    if (dateSelected.isAfter(now)) {
                        // --- UPCOMING Income ---
                        viewModel.addTransaction(
                            TransactionModel(
                                type = "Transfer", // Type is "Transfer"
                                from = transactionFromIndex,
                                to = transactionToIndex,
                                timestamp = dateSelected, // Transfer date
                                amount = parsedAmount.toString(), // Use the parsed Double amount
                                note = note.trim(),
                                isScheduled = true // Mark as scheduled
                            )
                        )
                        keyboardManager?.hide()
                        message(
                            "Self Transfer scheduled for ${
                                dateSelected.format(
                                    DateTimeFormatter.ofPattern("dd MMM yyyy")
                                )
                            }"
                        )
                        navController.navigateUp()
                    } else {
                        // --- Transfers are typically immediate and not scheduled in this flow ---
                        // If you want scheduled transfers, you'd apply similar date checks as above.
                        val onboardingModel =
                            overallBalance.value // Get current balance
                        var updatedOnboardingModel = onboardingModel.copy()

                        // Deduct from 'From' account
                        updatedOnboardingModel = updateBalanceForAccount(
                            currentOnboarding = updatedOnboardingModel,
                            accountIndex = transactionFromIndex,
                            amount = -parsedAmount
                        )
                        // Add to 'To' account
                        updatedOnboardingModel = updateBalanceForAccount(
                            currentOnboarding = updatedOnboardingModel,
                            accountIndex = transactionToIndex,
                            amount = parsedAmount
                        )
                        viewModel.updateOverallBalance(updatedOnboardingModel)

                        viewModel.addTransaction(
                            TransactionModel(
                                type = "Transfer", // Type is "Transfer"
                                from = transactionFromIndex,
                                to = transactionToIndex,
                                timestamp = dateSelected, // Transfer date
                                amount = parsedAmount.toString(), // Use the parsed Double amount
                                note = note.trim(),
                                isScheduled = false // Transfers are assumed immediate unless explicitly handled otherwise
                            )
                        )
                        keyboardManager?.hide()
                        message("Transfer added successfully!")
                        navController.navigateUp()
                    }
                }
            }
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun AddTransactionPreview() {
//    PersonalFinanceProTheme {
//        AddTransactionView(id, addTransactionViewModel, navController)
//    }
//}
package com.ramphal.personalfinancepro.ui.add

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ramphal.personalfinancepro.Constant
import com.ramphal.personalfinancepro.R
import com.ramphal.personalfinancepro.data.TransactionModel
import com.ramphal.personalfinancepro.ui.home.CustomPrice
import com.ramphal.personalfinancepro.ui.theme.myFont
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionView(
    viewModel: AddTransactionViewModel,
    navController: NavHostController,
    scope: CoroutineScope
) {
    val lazyListState = rememberLazyListState()
    var selectedIndex by remember { mutableIntStateOf(0) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateSelected by remember { mutableStateOf(LocalDateTime.now())}
    var showDatePicker by remember { mutableStateOf(false) }


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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex == 0){
            transactionFromIndex = 0
            transactionFromName = "Bank"
            transactionFromIcon = R.drawable.ic_bank_account_24px
            transactionToIndex = -1
            transactionToName = "Category"
            transactionToIcon = R.drawable.ic_category_24px
        } else if (selectedIndex == 1){
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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ){innerPadding->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            state = lazyListState
        ) {
            stickyHeader {
                AddTransactionTopAppBar(
                    onBackClick = { navController.navigateUp() },
                    onSaveClick = {
                    if (selectedIndex == 0){
                        if (amount.isEmpty()) {
                            scope.launch {
                                keyboardManager?.hide()
                                snackbarHostState.showSnackbar(
                                    message = "Please enter amount",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }else if (transactionToIndex == -1){
                            scope.launch {
                                keyboardManager?.hide()
                                snackbarHostState.showSnackbar(message = "Please select category", duration = SnackbarDuration.Short)
                            }
                        }else{
                            viewModel.addTransaction(
                                TransactionModel(
                                    type = if (selectedIndex == 0) "Expense" else if (selectedIndex == 1) "Income" else "Transfer",
                                    from = transactionFromIndex,
                                    to = transactionToIndex,
                                    timestamp = dateSelected,
                                    amount = amount,
                                    note = note,
                                )
                            )
                            navController.navigateUp()
                        }
                    } else if (selectedIndex == 1){
                        if (amount.isEmpty()) {
                            scope.launch {
                                keyboardManager?.hide()
                                snackbarHostState.showSnackbar(
                                    message = "Please enter amount",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }else if (transactionFromIndex == -1){
                            scope.launch {
                                keyboardManager?.hide()
                                snackbarHostState.showSnackbar(message = "Please select category", duration = SnackbarDuration.Short)
                            }
                        }else{
                            viewModel.addTransaction(
                                TransactionModel(
                                    type = if (selectedIndex == 0) "Expense" else if (selectedIndex == 1) "Income" else "Transfer",
                                    from = transactionFromIndex,
                                    to = transactionToIndex,
                                    timestamp = dateSelected,
                                    amount = amount,
                                    note = note
                                )
                            )
                            navController.navigateUp()
                        }
                    } else{
                        if (amount.isEmpty()) {
                            scope.launch {
                                keyboardManager?.hide()
                                snackbarHostState.showSnackbar(
                                    message = "Please enter amount",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }else{
                            viewModel.addTransaction(
                                TransactionModel(
                                    type = if (selectedIndex == 0) "Expense" else if (selectedIndex == 1) "Income" else "Transfer",
                                    from = transactionFromIndex,
                                    to = transactionToIndex,
                                    timestamp = dateSelected,
                                    amount = amount,
                                    note = note
                                )
                            )
                            navController.navigateUp()
                        }
                    }
                })
            }
            stickyHeader {
                Card(
                    modifier = Modifier.padding(8.dp)
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
                            onAmountChange = {amount = it},
                            focusManager = focusManager,
                            focusRequester = focusRequester,
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
                        DateSelected(date = dateSelected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), onDateClick = {
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
                                bottomSheetState = catBottomSheetState,
                                onDismissRequest = { openBankBottomSheet = false },
                                onItemClick = {
                                    if (selectedIndex == 0){
                                        transactionFromIndex = it
                                        transactionFromIcon = Constant.accountItems[it].icon
                                        transactionFromName = Constant.accountItems[it].name
                                    } else if (selectedIndex == 1){
                                        transactionToIndex = it
                                        transactionToIcon = Constant.accountItems[it].icon
                                        transactionToName = Constant.accountItems[it].name
                                    } else{
                                        transactionFromIndex = it
                                        transactionFromIcon = Constant.accountItems[it].icon
                                        transactionFromName = Constant.accountItems[it].name
                                    }
                                    openBankBottomSheet = false
                                }
                            )
                        }
                        if (openBankBottomSheet2){
                            BankBottomSheet(
                                bottomSheetState = catBottomSheetState,
                                onDismissRequest = { openBankBottomSheet2 = false },
                                onItemClick = {
                                    transactionToIndex = it
                                    transactionToIcon = Constant.accountItems[it].icon
                                    transactionToName = Constant.accountItems[it].name
                                    openBankBottomSheet2 = false
                                }
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
                Text(text = "Select payment mode", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ){
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier.padding(start = 4.dp, end = 8.dp)
        ) {
            items(Constant.accountItems.size){
                BankGridItem(
                    icon = Constant.accountItems[it].icon,
                    text = Constant.accountItems[it].name,
                    balance = Constant.accountItems[it].balance,
                    sign = Constant.accountItems[it].sign,
                    currency = Constant.accountItems[it].currency,
                    onItemClick = { onItemClick(it) }
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
    balance: String = "0.00",
    sign: String = "",
    currency: String = Constant.fallbackSymbols.getOrDefault("INR", "₹")
){
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
            currency = currency,
            amount = balance,
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
        shape = RoundedCornerShape(12.dp)
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
    currency: String = Constant.fallbackSymbols.getOrDefault("INR", "₹"),
    focusManager: FocusManager,
    focusRequester: FocusRequester,
    onAmountChange: (String) -> Unit
){
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
                currency,
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

fun Long.toSqlTimestamp(): Timestamp =
    Timestamp(this)

fun Timestamp.toDisplayDate(
    pattern: String = "dd/MM/yyyy",
    zone: ZoneId   = ZoneId.systemDefault()
): String {
    val ldt = this.toInstant()
        .atZone(zone)
        .toLocalDateTime()
    return ldt.format(DateTimeFormatter.ofPattern(pattern))
}

fun Timestamp.toLocalDateTime(): LocalDateTime =
    // java.sql.Timestamp has its own toLocalDateTime(), but this is universal:
    this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()



//@Preview(showBackground = true)
//@Composable
//fun AddTransactionPreview() {
//    PersonalFinanceProTheme {
//        AddTransactionView(id, addTransactionViewModel, navController)
//    }
//}
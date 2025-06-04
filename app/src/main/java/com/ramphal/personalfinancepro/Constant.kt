package com.ramphal.personalfinancepro

import com.ramphal.personalfinancepro.data.AccountItem
import com.ramphal.personalfinancepro.data.CategoryItem

object Constant {

    val categories = listOf<CategoryItem>(
        CategoryItem(R.drawable.ic_grocery_24px,    "Groceries"),
        CategoryItem(R.drawable.ic_shopping_24px,   "Shopping"),
        CategoryItem(R.drawable.ic_bills_24px,      "Bills"),
        CategoryItem(R.drawable.ic_fuel_24px,       "Fuel"),
        CategoryItem(R.drawable.ic_pets_24px,       "Pets"),
        CategoryItem(R.drawable.ic_food_24px,       "Restaurant"),
        CategoryItem(R.drawable.ic_alcohol_24px,    "Alcohol"),
        CategoryItem(R.drawable.ic_travel_24px,   "Travel"),
        CategoryItem(R.drawable.ic_child_care_24px, "Child Care"),
        CategoryItem(R.drawable.ic_insurance_24px,  "Insurance"),
        CategoryItem(R.drawable.ic_subscriptions_24px,   "Subscription"),
        CategoryItem(R.drawable.ic_education_24px,  "Education"),
        CategoryItem(R.drawable.ic_electronics_24px,"Electronics"),
        CategoryItem(R.drawable.ic_healthcare_24px, "Healthcare"),
        CategoryItem(R.drawable.ic_investments_24px,"Investments"),
        CategoryItem(R.drawable.ic_gifts_24px,      "Gifts"),
        CategoryItem(R.drawable.ic_loan_24px,       "Loan"),
        CategoryItem(R.drawable.ic_rent_24px,       "Rent"),
        CategoryItem(R.drawable.ic_savings_24px,    "Savings"),
        CategoryItem(R.drawable.ic_taxes_24px,   "Taxes"),
    )

    val incomeCat = listOf<CategoryItem>(
        CategoryItem(R.drawable.ic_income_24px,"Salary"),
        CategoryItem(R.drawable.ic_business_income_24px,"Business"),
        CategoryItem(R.drawable.ic_rent_24px,"Rental"),
        CategoryItem(R.drawable.ic_interest_24px,"Interest"),
        CategoryItem(R.drawable.ic_investments_24px,"Dividend"),
        CategoryItem(R.drawable.ic_capital_gains_24px,"Capital"),
        CategoryItem(R.drawable.ic_gifts_24px,      "Gifts"),
    )

    val OverallCat = listOf<CategoryItem>(
        CategoryItem(R.drawable.receive_24px,"Income"),
        CategoryItem(R.drawable.send_24px,"Expense"),
        CategoryItem(R.drawable.self_transfer_24px,"Transfer"),
    )

    val accountItems = listOf<AccountItem>(
        AccountItem(R.drawable.ic_bank_account_24px, "Bank", "+", "$", "22300"),
        AccountItem(R.drawable.ic_savings_24px, "Savings", "+", "$", "560"),
        AccountItem(R.drawable.ic_cash_24px, "Cash", "+", "$", "120"),
        AccountItem(R.drawable.ic_loan_24px, "Card", "+", "$", "60000"),
    )

    val fallbackSymbols = mapOf(
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "JPY" to "¥",
        "INR" to "₹",
        "RUB" to "₽",
        "IDR" to "Rp",
        "KRW" to "₩",
        "PHP" to "₱",
        "ZAR" to "R",
        "CAD" to "C$",
        "AUD" to "A$",
        "CHF" to "Fr.",
        "SEK" to "kr",
        "DKK" to "kr",
        "THB" to "฿"
    )


    // Keys for SharedPreferences
    const val PREFS_NAME = "personal_finance_pro_settings"
    const val KEY_CURRENCY_CODE = "currency_code"
    const val KEY_AMOUNT_FORMAT = "amount_format"
    const val KEY_DATE_FORMAT_PATTERN = "date_format_pattern"

}
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
        CategoryItem(R.drawable.ic_savings_24px, "Savings"),
        CategoryItem(R.drawable.ic_loan_24px, "Credit Card"),
        CategoryItem(R.drawable.ic_insurance_24px,  "Insurance"),
        CategoryItem(R.drawable.ic_subscriptions_24px,   "Subscription"),
        CategoryItem(R.drawable.ic_education_24px,  "Education"),
        CategoryItem(R.drawable.ic_electronics_24px,"Electronics"),
        CategoryItem(R.drawable.ic_healthcare_24px, "Healthcare"),
        CategoryItem(R.drawable.ic_investments_24px,"Investments"),
        CategoryItem(R.drawable.ic_gifts_24px,      "Gifts"),
        CategoryItem(R.drawable.ic_loan_24px,       "Loan"),
        CategoryItem(R.drawable.ic_rent_24px,       "Rent"),
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
        AccountItem(R.drawable.ic_bank_account_24px, "Bank", "+", "$", "0.0"),
        AccountItem(R.drawable.ic_savings_24px, "Savings", "+", "$", "0.0"),
        AccountItem(R.drawable.ic_cash_24px, "Cash", "+", "$", "0.0"),
        AccountItem(R.drawable.ic_loan_24px, "Credit Card", "+", "$", "0.0"),
    )

    const val PREFS_NAME = "personal_finance_prefs"
    const val KEY_CURRENCY_CODE = "preferred_currency_code"
    const val KEY_AMOUNT_FORMAT = "amount_format"
    const val KEY_DATE_FORMAT_PATTERN = "date_format_pattern"
    const val KEY_THEME_MODE = "my_theme"

    // Your currency options
    data class CurrencyOption(
        val symbol: String,
        val name: String
    )

    fun getAvailableCurrencyOptions(): List<CurrencyOption> {
        return listOf(
            CurrencyOption("$", "US Dollar"),
            CurrencyOption("€", "Euro"),
            CurrencyOption("£", "British Pound"),
            CurrencyOption("₹", "Indian Rupee"),
            CurrencyOption("¥", "Japanese Yen"),
            CurrencyOption("A$", "Australian Dollar"),
            CurrencyOption("C$", "Canadian Dollar"),
            CurrencyOption("CHF", "Swiss Franc"),
            CurrencyOption("kr", "Swedish Krona"),
            CurrencyOption("₩", "South Korean Won"),
            CurrencyOption("R", "South African Rand"),
            CurrencyOption("S$", "Singapore Dollar"),
            CurrencyOption("HK$", "Hong Kong Dollar"),
            CurrencyOption("Mex$", "Mexican Peso")
        ).sortedBy { it.name } // Sort alphabetically by currency name for better UX
    }

}
package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

enum class TransactionType(val label: String) {
    EXPENSE("Expense"),
    INCOME("Income")
}

enum class ExpenseCategory(
    val displayName: String,
    val color: Color
) {
    FOOD("Food", CatFoodColor),
    TRANSPORT("Transport", CatTransportColor),
    SHOPPING("Shopping", CatShoppingColor),
    BILLS("Bills", CatBillsColor),
    ENTERTAINMENT("Entertainment", CatEntertainmentColor),
    HEALTH("Health", CatHealthColor),
    EDUCATION("Education", CatEducationColor),
    TRAVEL("Travel", CatTravelColor),
    GROCERIES("Groceries", CatGroceriesColor),
    SALARY("Salary", MintIncome),
    FREELANCE("Freelance", EmeraldPrimary),
    INVESTMENT("Investment", IndigoSubscription),
    OTHER("Other", CatOtherColor);

    companion object {
        fun fromString(name: String): ExpenseCategory {
            return entries.find { it.displayName.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true) } ?: OTHER
        }

        val expenseCategories = listOf(
            FOOD, TRANSPORT, SHOPPING, BILLS, ENTERTAINMENT,
            HEALTH, EDUCATION, TRAVEL, GROCERIES, OTHER
        )

        val incomeCategories = listOf(
            SALARY, FREELANCE, INVESTMENT, OTHER
        )
    }
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Credit Card"),
    BANK_TRANSFER("Bank Transfer"),
    OTHER("Other");

    companion object {
        fun fromString(name: String): PaymentMethod {
            return entries.find { it.displayName.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true) } ?: UPI
        }
    }
}

enum class BillingCycle(val displayName: String, val daysInCycle: Int) {
    MONTHLY("Monthly", 30),
    YEARLY("Yearly", 365),
    WEEKLY("Weekly", 7);

    companion object {
        fun fromString(name: String): BillingCycle {
            return entries.find { it.displayName.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true) } ?: MONTHLY
        }
    }
}

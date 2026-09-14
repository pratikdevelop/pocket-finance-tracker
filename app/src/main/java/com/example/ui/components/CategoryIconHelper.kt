package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.ExpenseCategory

object CategoryIconHelper {
    fun getIconForCategory(category: String): ImageVector {
        return when (category.lowercase()) {
            "food", "dining", "restaurant" -> Icons.Default.Restaurant
            "transport", "travel", "fuel", "commute" -> Icons.Default.DirectionsCar
            "shopping", "clothing", "ecommerce" -> Icons.Default.ShoppingBag
            "bills", "utilities", "electricity", "recharge" -> Icons.Default.Receipt
            "entertainment", "movies", "streaming", "games" -> Icons.Default.ConfirmationNumber
            "health", "medical", "pharmacy", "doctor" -> Icons.Default.MedicalServices
            "education", "books", "courses", "tuition" -> Icons.Default.School
            "groceries", "supermarket", "provisions" -> Icons.Default.LocalGroceryStore
            "salary" -> Icons.AutoMirrored.Filled.TrendingUp
            "freelance", "business" -> Icons.Default.Work
            "investment", "stocks", "crypto" -> Icons.Default.ShowChart
            else -> Icons.Default.AccountBalanceWallet
        }
    }

    fun getColorForCategory(category: String): Color {
        return ExpenseCategory.fromString(category).color
    }

    fun getPaymentMethodIcon(method: String): ImageVector {
        return when (method.lowercase()) {
            "cash" -> Icons.Default.Payments
            "upi" -> Icons.Default.QrCode
            "debit card" -> Icons.Default.CreditCard
            "credit card" -> Icons.Default.CreditScore
            "bank transfer" -> Icons.Default.AccountBalance
            else -> Icons.Default.Payment
        }
    }
}

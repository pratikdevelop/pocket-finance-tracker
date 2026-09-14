package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: String = "Bills",
    val billingCycle: String = "Monthly", // Monthly, Yearly, Weekly
    val nextDueDateMillis: Long,
    val paymentMethod: String = "UPI",
    val isActive: Boolean = true,
    val reminderDaysBefore: Int = 2,
    val lastPaidDateMillis: Long? = null
)

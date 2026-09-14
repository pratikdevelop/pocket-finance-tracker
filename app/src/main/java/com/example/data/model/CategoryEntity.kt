package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String = "EXPENSE", // EXPENSE or INCOME
    val colorHex: String = "#10B981",
    val iconName: String = "category",
    val defaultBudget: Double = 0.0,
    val isDefault: Boolean = false
)

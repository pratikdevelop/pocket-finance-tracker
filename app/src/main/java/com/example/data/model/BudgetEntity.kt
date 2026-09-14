package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "ALL" for overall monthly budget or specific category e.g. "Food"
    val monthYear: String, // format: "YYYY-MM" e.g. "2026-08"
    val limitAmount: Double
)

package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseCategory
import com.example.ui.theme.CoralExpense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetDialog(
    isOpen: Boolean,
    initialCategory: String?,
    initialAmount: Double?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (category: String, limit: Double) -> Unit,
    onDelete: ((category: String) -> Unit)? = null
) {
    if (!isOpen) return

    var selectedCategory by remember(initialCategory) {
        mutableStateOf(initialCategory ?: "ALL")
    }
    var amountText by remember(initialAmount) {
        mutableStateOf(initialAmount?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
    }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isOverall = selectedCategory == "ALL"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialAmount != null) "Edit Budget" else "Set Monthly Budget",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = if (selectedCategory == "ALL") "Overall Monthly Budget" else selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Budget Scope / Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Overall Monthly Budget (All Categories)", fontWeight = FontWeight.Bold) },
                            onClick = {
                                selectedCategory = "ALL"
                                expanded = false
                            }
                        )
                        HorizontalDivider()
                        ExpenseCategory.expenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat.displayName
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = input
                            errorMessage = null
                        }
                    },
                    label = { Text("Monthly Limit Amount ($currency)") },
                    placeholder = { Text("e.g. 5000") },
                    prefix = { Text("$currency ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_amount_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = CoralExpense,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid amount"
                        return@Button
                    }
                    onSave(selectedCategory, amount)
                },
                modifier = Modifier.testTag("save_budget_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Budget")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initialAmount != null && onDelete != null && selectedCategory != "ALL") {
                    TextButton(
                        onClick = { onDelete(selectedCategory) },
                        colors = ButtonDefaults.textButtonColors(contentColor = CoralExpense)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

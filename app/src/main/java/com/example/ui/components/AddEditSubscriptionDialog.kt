package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.BillingCycle
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.SubscriptionEntity
import com.example.ui.theme.CoralExpense
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionDialog(
    isOpen: Boolean,
    subscription: SubscriptionEntity?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        name: String,
        amount: Double,
        category: String,
        cycle: String,
        nextDueDateMillis: Long,
        paymentMethod: String,
        reminderDays: Int
    ) -> Unit,
    onDelete: ((SubscriptionEntity) -> Unit)? = null
) {
    if (!isOpen) return

    val context = LocalContext.current
    var name by remember(subscription) { mutableStateOf(subscription?.name ?: "") }
    var amountText by remember(subscription) {
        mutableStateOf(subscription?.amount?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
    }
    var selectedCategory by remember(subscription) { mutableStateOf(subscription?.category ?: "Bills") }
    var selectedCycle by remember(subscription) { mutableStateOf(subscription?.billingCycle ?: "Monthly") }
    var selectedPayment by remember(subscription) { mutableStateOf(subscription?.paymentMethod ?: "UPI") }
    var reminderDays by remember(subscription) { mutableStateOf(subscription?.reminderDaysBefore ?: 2) }

    var nextDueDateMillis by remember(subscription) {
        mutableStateOf(
            subscription?.nextDueDateMillis ?: Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) }.timeInMillis
        )
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var cycleExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (subscription != null) "Edit Subscription / Bill" else "Add Subscription / Bill",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Service / Bill Name") },
                    placeholder = { Text("e.g. Netflix, Electricity, Rent, Spotify") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subscription_name_input")
                )

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = input
                            errorMessage = null
                        }
                    },
                    label = { Text("Amount ($currency)") },
                    placeholder = { Text("e.g. 649") },
                    prefix = { Text("$currency ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subscription_amount_input")
                )

                // Billing Cycle Dropdown
                ExposedDropdownMenuBox(
                    expanded = cycleExpanded,
                    onExpandedChange = { cycleExpanded = !cycleExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCycle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Cycle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cycleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = cycleExpanded,
                        onDismissRequest = { cycleExpanded = false }
                    ) {
                        listOf("Monthly", "Yearly", "Weekly").forEach { cycle ->
                            DropdownMenuItem(
                                text = { Text(cycle) },
                                onClick = {
                                    selectedCycle = cycle
                                    cycleExpanded = false
                                }
                            )
                        }
                    }
                }

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        ExpenseCategory.expenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat.displayName
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Payment Method Dropdown
                ExposedDropdownMenuBox(
                    expanded = paymentExpanded,
                    onExpandedChange = { paymentExpanded = !paymentExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedPayment,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = paymentExpanded,
                        onDismissRequest = { paymentExpanded = false }
                    ) {
                        PaymentMethod.entries.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.displayName) },
                                onClick = {
                                    selectedPayment = method.displayName
                                    paymentExpanded = false
                                }
                            )
                        }
                    }
                }

                // Next Due Date Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Next Due Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FinanceViewModel.formatDate(nextDueDateMillis, "dd MMMM yyyy"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = {
                            val currentCal = Calendar.getInstance().apply { timeInMillis = nextDueDateMillis }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val picked = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    }
                                    nextDueDateMillis = picked.timeInMillis
                                },
                                currentCal.get(Calendar.YEAR),
                                currentCal.get(Calendar.MONTH),
                                currentCal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick Date")
                    }
                }

                // Reminder Days Slider/Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remind me before:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(1, 2, 3, 5).forEach { days ->
                            FilterChip(
                                selected = reminderDays == days,
                                onClick = { reminderDays = days },
                                label = { Text("${days}d") }
                            )
                        }
                    }
                }

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
                    if (name.isBlank()) {
                        errorMessage = "Please enter a name for the subscription"
                        return@Button
                    }
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid amount"
                        return@Button
                    }
                    onSave(
                        subscription?.id ?: 0L,
                        name,
                        amount,
                        selectedCategory,
                        selectedCycle,
                        nextDueDateMillis,
                        selectedPayment,
                        reminderDays
                    )
                },
                modifier = Modifier.testTag("save_subscription_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Subscription")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (subscription != null && onDelete != null) {
                    TextButton(
                        onClick = { onDelete(subscription) },
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

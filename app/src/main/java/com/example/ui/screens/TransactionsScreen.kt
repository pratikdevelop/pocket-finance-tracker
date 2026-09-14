package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    searchQuery: String,
    filterType: TransactionType?,
    filterCategory: String?,
    filterPaymentMethod: String?,
    currency: String,
    onSearchChange: (String) -> Unit,
    onFilterTypeChange: (TransactionType?) -> Unit,
    onFilterCategoryChange: (String?) -> Unit,
    onFilterPaymentMethodChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onTransactionDelete: (TransactionEntity) -> Unit,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showPaymentMenu by remember { mutableStateOf(false) }

    val totalIncome = transactions.filter { it.type.equals("INCOME", ignoreCase = true) }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type.equals("EXPENSE", ignoreCase = true) }.sumOf { it.amount }

    // Group transactions by date string
    val groupedTransactions = remember(transactions) {
        transactions.groupBy { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            val now = Calendar.getInstance()
            when {
                cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) -> "Today"
                cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1 -> "Yesterday"
                else -> FinanceViewModel.formatDate(tx.dateMillis, "dd MMMM yyyy")
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("transactions_screen_lazy_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transactions History",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${transactions.size} records found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onAddTransactionClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Entry")
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by title, note, or category...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_transactions_input")
            )
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Chips
                FilterChip(
                    selected = filterType == null,
                    onClick = { onFilterTypeChange(null) },
                    label = { Text("All Types") }
                )
                FilterChip(
                    selected = filterType == TransactionType.EXPENSE,
                    onClick = {
                        onFilterTypeChange(if (filterType == TransactionType.EXPENSE) null else TransactionType.EXPENSE)
                    },
                    label = { Text("Expenses") },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CoralExpense)
                        )
                    }
                )
                FilterChip(
                    selected = filterType == TransactionType.INCOME,
                    onClick = {
                        onFilterTypeChange(if (filterType == TransactionType.INCOME) null else TransactionType.INCOME)
                    },
                    label = { Text("Income") },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary)
                        )
                    }
                )

                // Category Filter Dropdown
                Box {
                    AssistChip(
                        onClick = { showCategoryMenu = true },
                        label = { Text(filterCategory ?: "Category") },
                        trailingIcon = {
                            Icon(
                                if (filterCategory != null) Icons.Default.Close else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (filterCategory != null) EmeraldContainer else MaterialTheme.colorScheme.surface
                        )
                    )

                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                onFilterCategoryChange(null)
                                showCategoryMenu = false
                            }
                        )
                        ExpenseCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    onFilterCategoryChange(cat.displayName)
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                // Payment Method Filter Dropdown
                Box {
                    AssistChip(
                        onClick = { showPaymentMenu = true },
                        label = { Text(filterPaymentMethod ?: "Payment") },
                        trailingIcon = {
                            Icon(
                                if (filterPaymentMethod != null) Icons.Default.Close else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (filterPaymentMethod != null) IndigoSubscriptionContainer else MaterialTheme.colorScheme.surface
                        )
                    )

                    DropdownMenu(
                        expanded = showPaymentMenu,
                        onDismissRequest = { showPaymentMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Payment Methods") },
                            onClick = {
                                onFilterPaymentMethodChange(null)
                                showPaymentMenu = false
                            }
                        )
                        PaymentMethod.entries.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.displayName) },
                                onClick = {
                                    onFilterPaymentMethodChange(method.displayName)
                                    showPaymentMenu = false
                                }
                            )
                        }
                    }
                }

                if (filterType != null || filterCategory != null || filterPaymentMethod != null || searchQuery.isNotEmpty()) {
                    TextButton(onClick = onClearFilters) {
                        Text("Reset", color = CoralExpense)
                    }
                }
            }
        }

        // Summary Mini-Bar
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Income:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+$currency${FinanceViewModel.formatAmount(totalIncome)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MintIncome)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Expenses:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("-$currency${FinanceViewModel.formatAmount(totalExpense)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = CoralExpense)
                    }
                }
            }
        }

        // Grouped List of Transactions
        if (groupedTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No matching transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try adjusting your search query or filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onClearFilters,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Clear All Filters")
                        }
                    }
                }
            }
        } else {
            groupedTransactions.forEach { (dateGroup, list) ->
                val groupExpenseTotal = list.filter { it.type.equals("EXPENSE", ignoreCase = true) }.sumOf { it.amount }
                val groupIncomeTotal = list.filter { it.type.equals("INCOME", ignoreCase = true) }.sumOf { it.amount }

                item(key = "header_$dateGroup") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateGroup,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (groupExpenseTotal > 0) "-$currency${FinanceViewModel.formatAmount(groupExpenseTotal)}"
                            else "+$currency${FinanceViewModel.formatAmount(groupIncomeTotal)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(list, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        currency = currency,
                        onClick = { onTransactionClick(tx) },
                        onDelete = { onTransactionDelete(tx) }
                    )
                }
            }
        }
    }
}

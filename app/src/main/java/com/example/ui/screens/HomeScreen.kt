package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.CategorySpendItem
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.MonthlyFinanceSummary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    summary: MonthlyFinanceSummary,
    recentTransactions: List<TransactionEntity>,
    categoryBreakdown: List<CategorySpendItem>,
    subscriptions: List<SubscriptionEntity>,
    currency: String,
    onQuickAddExpense: () -> Unit,
    onQuickAddIncome: () -> Unit,
    onAddBill: () -> Unit,
    onNavigateTab: (AppNavTab) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onTransactionDelete: (TransactionEntity) -> Unit,
    onMarkSubscriptionPaid: (SubscriptionEntity) -> Unit,
    onEditBudget: (category: String?, amount: Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // App Header & Month Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pocket Finance",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = summary.monthName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = EmeraldContainer,
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary)
                        )
                        Text(
                            text = "Offline-First",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnEmeraldContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Top Summary Dashboard (Hero Net Savings & In/Out Cards)
        item {
            TopSummaryDashboard(
                summary = summary,
                currency = currency
            )
        }

        // Quick Action Buttons (Add Expense, Add Income, Add Subscription)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    label = "+ Expense",
                    containerColor = CoralExpenseContainer,
                    contentColor = CoralExpense,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_expense_btn"),
                    onClick = onQuickAddExpense
                )

                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "+ Income",
                    containerColor = MintIncomeContainer,
                    contentColor = EmeraldPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_income_btn"),
                    onClick = onQuickAddIncome
                )

                QuickActionButton(
                    icon = Icons.Default.ReceiptLong,
                    label = "+ Bill/Sub",
                    containerColor = IndigoSubscriptionContainer,
                    contentColor = IndigoSubscription,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_bill_btn"),
                    onClick = onAddBill
                )
            }
        }

        // Spending Progress Card
        item {
            SpendingProgressCard(
                summary = summary,
                currency = currency,
                onManageBudgetClick = { onNavigateTab(AppNavTab.BUDGETS) }
            )
        }

        // Upcoming Bills Section
        if (subscriptions.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Bills & Subscriptions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { onNavigateTab(AppNavTab.SUBSCRIPTIONS) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("See All", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        subscriptions.take(3).forEach { sub ->
                            SubscriptionItemCard(
                                subscription = sub,
                                currency = currency,
                                onMarkPaid = { onMarkSubscriptionPaid(sub) },
                                onClick = { onNavigateTab(AppNavTab.SUBSCRIPTIONS) },
                                onDelete = {}
                            )
                        }
                    }
                }
            }
        }

        // Category Spending Bar Chart (Visual representation)
        item {
            CategorySpendingBarChart(
                categoryBreakdown = categoryBreakdown,
                currency = currency,
                monthName = summary.monthName,
                onCategoryClick = { onNavigateTab(AppNavTab.BUDGETS) }
            )
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = { onNavigateTab(AppNavTab.TRANSACTIONS) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("View All", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No transactions recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onQuickAddExpense,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("Add First Expense")
                        }
                    }
                }
            }
        } else {
            items(recentTransactions.take(6), key = { it.id }) { tx ->
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

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

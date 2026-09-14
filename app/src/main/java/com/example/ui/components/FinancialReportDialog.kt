package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CategorySpendItem
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.MonthlyFinanceSummary

@Composable
fun FinancialReportDialog(
    isOpen: Boolean,
    summary: MonthlyFinanceSummary,
    categoryBreakdown: List<CategorySpendItem>,
    currency: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    if (!isOpen) return

    val savingsRate = if (summary.totalIncome > 0) {
        ((summary.netSavings / summary.totalIncome) * 100).coerceAtLeast(0.0)
    } else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Financial Report",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = summary.monthName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Key metrics card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportRow(label = "Total Income", value = "$currency${FinanceViewModel.formatAmount(summary.totalIncome)}", color = MintIncome)
                        ReportRow(label = "Total Expenses", value = "$currency${FinanceViewModel.formatAmount(summary.totalExpense)}", color = CoralExpense)
                        ReportRow(label = "Net Savings", value = "$currency${FinanceViewModel.formatAmount(summary.netSavings)}", color = if (summary.netSavings >= 0) MintIncome else CoralExpense, isBold = true)
                        ReportRow(label = "Savings Rate", value = "${String.format("%.1f", savingsRate)}%", color = EmeraldPrimary, isBold = true)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        ReportRow(label = "Monthly Budget", value = "$currency${FinanceViewModel.formatAmount(summary.overallBudget)}")
                        ReportRow(label = "Budget Consumed", value = "${String.format("%.1f", summary.budgetSpentPercentage * 100)}%")
                        ReportRow(label = "Remaining Budget", value = "$currency${FinanceViewModel.formatAmount(summary.remainingBudget)}")
                    }
                }

                // Category Breakdown
                Text(
                    text = "Top Spending Categories",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (categoryBreakdown.isEmpty()) {
                    Text(
                        text = "No expenses recorded this month.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    categoryBreakdown.sortedByDescending { it.spent }.take(6).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(item.color)
                                )
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "$currency${FinanceViewModel.formatAmount(item.spent)} (${String.format("%.1f", item.percentage * 100)}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShare,
                modifier = Modifier.testTag("share_report_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ReportRow(
    label: String,
    value: String,
    color: Color = Color.Unspecified,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
    }
}

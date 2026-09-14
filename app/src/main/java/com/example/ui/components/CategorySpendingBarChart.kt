package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.CategorySpendItem
import com.example.ui.viewmodel.FinanceViewModel
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun CategorySpendingBarChart(
    categoryBreakdown: List<CategorySpendItem>,
    currency: String,
    monthName: String,
    modifier: Modifier = Modifier,
    onCategoryClick: ((CategorySpendItem) -> Unit)? = null
) {
    // Filter to categories that have actual expenses
    val activeCategories = remember(categoryBreakdown) {
        categoryBreakdown.filter { it.spent > 0 }
            .sortedByDescending { it.spent }
    }

    var selectedCategoryIndex by remember(activeCategories) {
        mutableStateOf(if (activeCategories.isNotEmpty()) 0 else -1)
    }

    val selectedCategory = if (selectedCategoryIndex in activeCategories.indices) {
        activeCategories[selectedCategoryIndex]
    } else null

    val totalMonthSpend = remember(activeCategories) {
        activeCategories.sumOf { it.spent }
    }

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_spending_bar_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Spending Trends",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Monthly Spending Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Category breakdown for $monthName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "$currency${FinanceViewModel.formatAmount(totalMonthSpend)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            if (activeCategories.isEmpty()) {
                // Empty state when no expenses recorded
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .testTag("empty_spending_chart"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "No data",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No spending recorded this month",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add expenses to see your visual trends by category",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                // Canvas Bar Chart
                val maxSpend = remember(activeCategories) {
                    val maxVal = activeCategories.maxOfOrNull { it.spent } ?: 1.0
                    // Give 15% headroom
                    (maxVal * 1.15).coerceAtLeast(10.0)
                }

                val animatedProgress by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "chart_bars_anim"
                )

                val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                val surfaceColor = MaterialTheme.colorScheme.surface

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("category_canvas_chart")
                            .pointerInput(activeCategories) {
                                detectTapGestures { offset ->
                                    val count = activeCategories.size
                                    if (count == 0) return@detectTapGestures

                                    val leftPadding = 48f
                                    val rightPadding = 16f
                                    val chartWidth = size.width - leftPadding - rightPadding
                                    val colWidth = chartWidth / count

                                    val xInChart = offset.x - leftPadding
                                    if (xInChart >= 0 && xInChart < chartWidth) {
                                        val clickedIndex = (xInChart / colWidth).toInt().coerceIn(0, count - 1)
                                        selectedCategoryIndex = clickedIndex
                                        onCategoryClick?.invoke(activeCategories[clickedIndex])
                                    }
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        val leftPadding = 56f
                        val rightPadding = 16f
                        val bottomPadding = 44f
                        val topPadding = 28f

                        val chartWidth = canvasWidth - leftPadding - rightPadding
                        val chartHeight = canvasHeight - topPadding - bottomPadding

                        // Draw Horizontal Grid Lines (4 lines: 0%, 33%, 66%, 100%)
                        val gridSteps = 3
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                        for (i in 0..gridSteps) {
                            val y = topPadding + chartHeight * (1f - i.toFloat() / gridSteps)
                            val stepValue = (maxSpend * (i.toFloat() / gridSteps)).roundToInt()

                            // Grid line
                            drawLine(
                                color = gridColor,
                                start = Offset(leftPadding, y),
                                end = Offset(canvasWidth - rightPadding, y),
                                strokeWidth = 1.2f,
                                pathEffect = if (i > 0) pathEffect else null
                            )

                            // Grid Value text
                            val formattedVal = if (stepValue >= 1000) {
                                "${stepValue / 1000}k"
                            } else {
                                "$stepValue"
                            }
                            val textLayout = textMeasurer.measure(
                                text = formattedVal,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = labelColor,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = Offset(leftPadding - textLayout.size.width - 8f, y - textLayout.size.height / 2f)
                            )
                        }

                        // Draw Bars
                        val count = activeCategories.size
                        val slotWidth = chartWidth / count
                        val barWidth = (slotWidth * 0.58f).coerceIn(16f, 44f)

                        activeCategories.forEachIndexed { index, item ->
                            val centerX = leftPadding + slotWidth * index + slotWidth / 2f
                            val barHeight = ((item.spent / maxSpend) * chartHeight * animatedProgress).toFloat()
                                .coerceAtLeast(6f)
                            val barTop = topPadding + chartHeight - barHeight
                            val barLeft = centerX - barWidth / 2f

                            val isSelected = (index == selectedCategoryIndex)
                            val primaryCatColor = item.color
                            val lighterColor = primaryCatColor.copy(alpha = 0.75f)

                            // Vertical bar gradient brush
                            val brush = Brush.verticalGradient(
                                colors = listOf(lighterColor, primaryCatColor),
                                startY = barTop,
                                endY = topPadding + chartHeight
                            )

                            // Draw rounded top bar
                            drawRoundRect(
                                brush = brush,
                                topLeft = Offset(barLeft, barTop),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(barWidth / 2.5f, barWidth / 2.5f)
                            )

                            // Draw selected highlight stroke & indicator
                            if (isSelected) {
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(barLeft - 2f, barTop - 2f),
                                    size = Size(barWidth + 4f, barHeight + 4f),
                                    cornerRadius = CornerRadius(barWidth / 2.2f, barWidth / 2.2f),
                                    style = Stroke(width = 2.5f)
                                )

                                // Pointer indicator top dot
                                drawCircle(
                                    color = primaryCatColor,
                                    radius = 5f,
                                    center = Offset(centerX, barTop - 9f)
                                )
                            }

                            // Category label below bar
                            val label = if (item.category.length > 5) item.category.take(4) + "…" else item.category
                            val labelLayout = textMeasurer.measure(
                                text = label,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) primaryCatColor else labelColor
                                )
                            )
                            drawText(
                                textLayoutResult = labelLayout,
                                topLeft = Offset(centerX - labelLayout.size.width / 2f, topPadding + chartHeight + 10f)
                            )
                        }
                    }
                }

                // Interactive Category Details Card
                AnimatedVisibility(
                    visible = selectedCategory != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    selectedCategory?.let { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = item.color.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("selected_category_detail_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(item.color)
                                    )
                                    Column {
                                        Text(
                                            text = item.category,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${String.format("%.1f", item.percentage * 100)}% of month's expenses",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$currency${FinanceViewModel.formatAmount(item.spent)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = item.color
                                    )
                                    if (item.budget > 0) {
                                        val budgetRatio = (item.spent / item.budget * 100).roundToInt()
                                        Text(
                                            text = "Budget: $budgetRatio%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (budgetRatio > 100) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Horizontal Category Chips for quick interactive filtering
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeCategories.forEachIndexed { index, item ->
                        val isSelected = index == selectedCategoryIndex
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) item.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, item.color) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedCategoryIndex = index
                                    onCategoryClick?.invoke(item)
                                }
                                .testTag("chip_category_${item.category.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(item.color)
                                )
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$currency${FinanceViewModel.formatAmount(item.spent)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = item.color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

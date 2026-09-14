package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.AppNavTab

sealed class NavItem(
    val tab: AppNavTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    object Home : NavItem(
        AppNavTab.HOME,
        Icons.Filled.Home,
        Icons.Outlined.Home,
        "nav_tab_home"
    )

    object Transactions : NavItem(
        AppNavTab.TRANSACTIONS,
        Icons.Filled.ReceiptLong,
        Icons.Outlined.ReceiptLong,
        "nav_tab_transactions"
    )

    object Budgets : NavItem(
        AppNavTab.BUDGETS,
        Icons.Filled.PieChart,
        Icons.Outlined.PieChart,
        "nav_tab_budgets"
    )

    object Subscriptions : NavItem(
        AppNavTab.SUBSCRIPTIONS,
        Icons.Filled.Repeat,
        Icons.Outlined.Repeat,
        "nav_tab_subscriptions"
    )

    object Settings : NavItem(
        AppNavTab.SETTINGS,
        Icons.Filled.Person,
        Icons.Outlined.Person,
        "nav_tab_settings"
    )

    companion object {
        val items = listOf(Home, Transactions, Budgets, Subscriptions, Settings)
    }
}

@Composable
fun PocketFinanceBottomNav(
    currentTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("main_bottom_nav_bar"),
        windowInsets = NavigationBarDefaults.windowInsets,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavItem.items.forEach { item ->
            val isSelected = currentTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.tab.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                alwaysShowLabel = true,
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

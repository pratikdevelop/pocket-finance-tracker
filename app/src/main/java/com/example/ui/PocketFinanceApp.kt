package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.TransactionType
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketFinanceApp(
    viewModel: FinanceViewModel = viewModel()
) {
    val context = LocalContext.current

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currency by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val lastCategory by viewModel.lastUsedCategory.collectAsStateWithLifecycle()
    val lastPayment by viewModel.lastUsedPaymentMethod.collectAsStateWithLifecycle()

    val summary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val allSubscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle()
    val monthlySubTotal by viewModel.monthlySubscriptionTotal.collectAsStateWithLifecycle()
    val annualSubTotal by viewModel.annualSubscriptionTotal.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val filterCategory by viewModel.filterCategory.collectAsStateWithLifecycle()
    val filterPayment by viewModel.filterPaymentMethod.collectAsStateWithLifecycle()

    val isQuickAddOpen by viewModel.isQuickAddOpen.collectAsStateWithLifecycle()
    val quickAddType by viewModel.quickAddType.collectAsStateWithLifecycle()

    val isAddExpenseOpen by viewModel.isAddExpenseScreenOpen.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()

    val isEditDialogOpen by viewModel.isEditDialogOpen.collectAsStateWithLifecycle()
    val isAddBudgetOpen by viewModel.isAddBudgetDialogOpen.collectAsStateWithLifecycle()
    val isAddSubOpen by viewModel.isAddSubscriptionOpen.collectAsStateWithLifecycle()

    var isReportDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PocketFinanceBottomNav(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddExpenseScreen() },
                shape = CircleShape,
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .testTag("floating_quick_add_button")
                    .size(58.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    AppNavTab.HOME -> {
                        HomeScreen(
                            summary = summary,
                            recentTransactions = allTransactions,
                            categoryBreakdown = categoryBreakdown,
                            subscriptions = allSubscriptions,
                            currency = currency,
                            onQuickAddExpense = { viewModel.openAddExpenseScreen() },
                            onQuickAddIncome = { viewModel.openQuickAdd(TransactionType.INCOME) },
                            onAddBill = { viewModel.openAddOrEditSubscription(null) },
                            onNavigateTab = { viewModel.selectTab(it) },
                            onTransactionClick = { viewModel.openEditTransaction(it) },
                            onTransactionDelete = { viewModel.deleteTransaction(it) },
                            onMarkSubscriptionPaid = { viewModel.markSubscriptionPaid(it) },
                            onEditBudget = { cat, amt -> viewModel.openAddOrEditBudget(cat, amt) }
                        )
                    }

                    AppNavTab.TRANSACTIONS -> {
                        TransactionsScreen(
                            transactions = filteredTransactions,
                            searchQuery = searchQuery,
                            filterType = filterType,
                            filterCategory = filterCategory,
                            filterPaymentMethod = filterPayment,
                            currency = currency,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onFilterTypeChange = { viewModel.setFilterType(it) },
                            onFilterCategoryChange = { viewModel.setFilterCategory(it) },
                            onFilterPaymentMethodChange = { viewModel.setFilterPaymentMethod(it) },
                            onClearFilters = { viewModel.clearFilters() },
                            onTransactionClick = { viewModel.openEditTransaction(it) },
                            onTransactionDelete = { viewModel.deleteTransaction(it) },
                            onAddTransactionClick = { viewModel.openAddExpenseScreen() }
                        )
                    }

                    AppNavTab.BUDGETS -> {
                        BudgetsScreen(
                            summary = summary,
                            categoryBreakdown = categoryBreakdown,
                            currency = currency,
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onSetOverallBudget = { viewModel.openAddOrEditBudget("ALL", summary.overallBudget) },
                            onAddCategoryBudget = { viewModel.openAddOrEditBudget(null, null) },
                            onEditCategoryBudget = { cat, limit -> viewModel.openAddOrEditBudget(cat, limit) }
                        )
                    }

                    AppNavTab.SUBSCRIPTIONS -> {
                        SubscriptionsScreen(
                            subscriptions = allSubscriptions,
                            monthlyTotal = monthlySubTotal,
                            annualTotal = annualSubTotal,
                            currency = currency,
                            onAddSubscription = { viewModel.openAddOrEditSubscription(null) },
                            onEditSubscription = { viewModel.openAddOrEditSubscription(it) },
                            onDeleteSubscription = { viewModel.deleteSubscription(it) },
                            onMarkPaid = { viewModel.markSubscriptionPaid(it) }
                        )
                    }

                    AppNavTab.SETTINGS -> {
                        SettingsReportsScreen(
                            summary = summary,
                            categoryBreakdown = categoryBreakdown,
                            currency = currency,
                            onSelectCurrency = { viewModel.setCurrency(it) },
                            onViewReport = { isReportDialogOpen = true },
                            onShareReport = { viewModel.shareMonthlyReport(context) },
                            onResetSampleData = { viewModel.resetSampleData() },
                            onClearAllData = { viewModel.clearAllData() },
                            onSetOverallBudget = { viewModel.openAddOrEditBudget("ALL", summary.overallBudget) }
                        )
                    }
                }
            }
        }
    }

    // Quick Expense/Income Entry Bottom Sheet
    QuickExpenseEntrySheet(
        isOpen = isQuickAddOpen,
        initialType = quickAddType,
        currency = currency,
        lastCategory = lastCategory,
        lastPaymentMethod = lastPayment,
        onDismiss = { viewModel.closeQuickAdd() },
        onSaveTransaction = { title, amount, type, category, paymentMethod, dateMillis, notes ->
            viewModel.addTransaction(title, amount, type, category, paymentMethod, dateMillis, notes)
        }
    )

    // Edit Transaction Dialog
    EditTransactionDialog(
        isOpen = isEditDialogOpen,
        transaction = viewModel.editingTransaction,
        currency = currency,
        onDismiss = { viewModel.closeEditTransaction() },
        onSave = { viewModel.updateTransaction(it) },
        onDelete = { viewModel.deleteTransaction(it) }
    )

    // Add / Edit Budget Dialog
    AddEditBudgetDialog(
        isOpen = isAddBudgetOpen,
        initialCategory = viewModel.editingBudgetCategory,
        initialAmount = viewModel.editingBudgetAmount,
        currency = currency,
        onDismiss = { viewModel.closeAddOrEditBudget() },
        onSave = { category, limit -> viewModel.saveBudget(category, limit) },
        onDelete = { category -> viewModel.deleteBudget(category) }
    )

    // Add / Edit Subscription Dialog
    AddEditSubscriptionDialog(
        isOpen = isAddSubOpen,
        subscription = viewModel.editingSubscription,
        currency = currency,
        onDismiss = { viewModel.closeAddOrEditSubscription() },
        onSave = { id, name, amount, category, cycle, nextDueDate, paymentMethod, reminderDays ->
            viewModel.saveSubscription(id, name, amount, category, cycle, nextDueDate, paymentMethod, reminderDays)
        },
        onDelete = { viewModel.deleteSubscription(it) }
    )

    // Financial Report Breakdown Dialog
    FinancialReportDialog(
        isOpen = isReportDialogOpen,
        summary = summary,
        categoryBreakdown = categoryBreakdown,
        currency = currency,
        onDismiss = { isReportDialogOpen = false },
        onShare = {
            isReportDialogOpen = false
            viewModel.shareMonthlyReport(context)
        }
    )

    // Full Compose Screen for Adding Expenses with Form Validation
    if (isAddExpenseOpen) {
        AddExpenseScreen(
            currency = currency,
            initialCategory = lastCategory,
            initialPaymentMethod = lastPayment,
            availableCategories = allCategories,
            onNavigateBack = { viewModel.closeAddExpenseScreen() },
            onSaveExpense = { amount, category, dateMillis, description, paymentMethod ->
                viewModel.addExpense(amount, category, dateMillis, description, paymentMethod)
            }
        )
    }
}

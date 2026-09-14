package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BillingCycle
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppNavTab(val title: String) {
    HOME("Home"),
    TRANSACTIONS("Transactions"),
    BUDGETS("Budgets"),
    SUBSCRIPTIONS("Subscriptions"),
    SETTINGS("Profile & Reports")
}

data class MonthlyFinanceSummary(
    val monthKey: String,
    val monthName: String,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netSavings: Double = 0.0,
    val todaySpend: Double = 0.0,
    val overallBudget: Double = 35000.0,
    val budgetSpentPercentage: Float = 0f,
    val remainingBudget: Double = 0.0,
    val dailySpendAllowance: Double = 0.0,
    val daysRemainingInMonth: Int = 1
)

data class CategorySpendItem(
    val category: String,
    val spent: Double,
    val budget: Double,
    val percentage: Float,
    val color: androidx.compose.ui.graphics.Color
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = FinanceRepository(database, application)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(AppNavTab.HOME)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    // Currency & Preferences
    val currencySymbol: StateFlow<String> = repository.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    val lastUsedCategory: StateFlow<String> = repository.lastUsedCategory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Food")

    val lastUsedPaymentMethod: StateFlow<String> = repository.lastUsedPaymentMethod
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "UPI")

    fun setCurrency(symbol: String) {
        repository.setCurrencySymbol(symbol)
    }

    // Selected Month for filtering/reporting
    private val _selectedMonthCalendar = MutableStateFlow(Calendar.getInstance())
    val selectedMonthCalendar: StateFlow<Calendar> = _selectedMonthCalendar.asStateFlow()

    fun nextMonth() {
        val cal = _selectedMonthCalendar.value.clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        _selectedMonthCalendar.value = cal
    }

    fun previousMonth() {
        val cal = _selectedMonthCalendar.value.clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        _selectedMonthCalendar.value = cal
    }

    fun setCurrentMonth() {
        _selectedMonthCalendar.value = Calendar.getInstance()
    }

    // All raw data streams
    val allTransactions = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories = repository.expenseCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubscriptions = repository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Add Expense Screen State
    private val _isAddExpenseScreenOpen = MutableStateFlow(false)
    val isAddExpenseScreenOpen: StateFlow<Boolean> = _isAddExpenseScreenOpen.asStateFlow()

    fun openAddExpenseScreen() {
        _isAddExpenseScreenOpen.value = true
    }

    fun closeAddExpenseScreen() {
        _isAddExpenseScreenOpen.value = false
    }

    // Search and Filter states for Transactions Screen
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow<TransactionType?>(null) // null = ALL
    val filterType: StateFlow<TransactionType?> = _filterType.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null) // null = ALL
    val filterCategory: StateFlow<String?> = _filterCategory.asStateFlow()

    private val _filterPaymentMethod = MutableStateFlow<String?>(null) // null = ALL
    val filterPaymentMethod: StateFlow<String?> = _filterPaymentMethod.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: TransactionType?) {
        _filterType.value = type
    }

    fun setFilterCategory(category: String?) {
        _filterCategory.value = category
    }

    fun setFilterPaymentMethod(method: String?) {
        _filterPaymentMethod.value = method
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _filterType.value = null
        _filterCategory.value = null
        _filterPaymentMethod.value = null
    }

    // Budgets for currently selected month
    val currentMonthBudgets: StateFlow<List<BudgetEntity>> = _selectedMonthCalendar.combine(database.budgetDao().getBudgetsForMonth(getMonthKey(_selectedMonthCalendar.value))) { cal, _ ->
        val key = getMonthKey(cal)
        key
    }.combine(database.budgetDao().getBudgetsForMonth(getMonthKey(Calendar.getInstance()))) { _, _ ->
        val key = getMonthKey(_selectedMonthCalendar.value)
        key
    }.combine(allTransactions) { _, _ ->
        // Triggered on month/transaction changes
        getMonthKey(_selectedMonthCalendar.value)
    }.combine(repository.getBudgetsForMonth(getMonthKey(Calendar.getInstance()))) { _, budgets ->
        budgets
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month summary calculations
    val monthlySummary: StateFlow<MonthlyFinanceSummary> = combine(
        allTransactions,
        _selectedMonthCalendar,
        database.budgetDao().getBudgetsForMonth(getMonthKey(Calendar.getInstance()))
    ) { transactions, cal, budgets ->
        calculateMonthlySummary(transactions, cal, budgets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyFinanceSummary("2026-08", "August 2026"))

    // Category breakdown for selected month
    val categoryBreakdown: StateFlow<List<CategorySpendItem>> = combine(
        allTransactions,
        _selectedMonthCalendar,
        database.budgetDao().getBudgetsForMonth(getMonthKey(Calendar.getInstance()))
    ) { transactions, cal, budgets ->
        calculateCategoryBreakdown(transactions, cal, budgets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered transactions for Transactions Tab
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _searchQuery,
        _filterType,
        _filterCategory,
        _filterPaymentMethod
    ) { transactions, query, type, cat, payment ->
        transactions.filter { tx ->
            val matchesQuery = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.category.contains(query, ignoreCase = true) ||
                    tx.notes.contains(query, ignoreCase = true) ||
                    tx.amount.toString().contains(query)

            val matchesType = type == null || tx.type.equals(type.name, ignoreCase = true)
            val matchesCat = cat == null || tx.category.equals(cat, ignoreCase = true)
            val matchesPayment = payment == null || tx.paymentMethod.equals(payment, ignoreCase = true)

            matchesQuery && matchesType && matchesCat && matchesPayment
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Subscriptions calculations
    val monthlySubscriptionTotal: StateFlow<Double> = allSubscriptions.combine(_selectedMonthCalendar) { subs, _ ->
        subs.filter { it.isActive }.sumOf { sub ->
            when (sub.billingCycle.lowercase()) {
                "weekly" -> sub.amount * 4.33
                "yearly" -> sub.amount / 12.0
                else -> sub.amount
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val annualSubscriptionTotal: StateFlow<Double> = monthlySubscriptionTotal.combine(_selectedMonthCalendar) { monthly, _ ->
        monthly * 12.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Quick Add Sheet State
    private val _isQuickAddOpen = MutableStateFlow(false)
    val isQuickAddOpen: StateFlow<Boolean> = _isQuickAddOpen.asStateFlow()

    private val _quickAddType = MutableStateFlow(TransactionType.EXPENSE)
    val quickAddType: StateFlow<TransactionType> = _quickAddType.asStateFlow()

    fun openQuickAdd(type: TransactionType = TransactionType.EXPENSE) {
        _quickAddType.value = type
        _isQuickAddOpen.value = true
    }

    fun closeQuickAdd() {
        _isQuickAddOpen.value = false
    }

    // Dialog States
    var editingTransaction: TransactionEntity? = null
    private val _isEditDialogOpen = MutableStateFlow(false)
    val isEditDialogOpen: StateFlow<Boolean> = _isEditDialogOpen.asStateFlow()

    fun openEditTransaction(tx: TransactionEntity) {
        editingTransaction = tx
        _isEditDialogOpen.value = true
    }

    fun closeEditTransaction() {
        editingTransaction = null
        _isEditDialogOpen.value = false
    }

    private val _isAddBudgetDialogOpen = MutableStateFlow(false)
    val isAddBudgetDialogOpen: StateFlow<Boolean> = _isAddBudgetDialogOpen.asStateFlow()

    var editingBudgetCategory: String? = null
    var editingBudgetAmount: Double? = null

    fun openAddOrEditBudget(category: String? = null, currentLimit: Double? = null) {
        editingBudgetCategory = category
        editingBudgetAmount = currentLimit
        _isAddBudgetDialogOpen.value = true
    }

    fun closeAddOrEditBudget() {
        editingBudgetCategory = null
        editingBudgetAmount = null
        _isAddBudgetDialogOpen.value = false
    }

    private val _isAddSubscriptionOpen = MutableStateFlow(false)
    val isAddSubscriptionOpen: StateFlow<Boolean> = _isAddSubscriptionOpen.asStateFlow()

    var editingSubscription: SubscriptionEntity? = null

    fun openAddOrEditSubscription(subscription: SubscriptionEntity? = null) {
        editingSubscription = subscription
        _isAddSubscriptionOpen.value = true
    }

    fun closeAddOrEditSubscription() {
        editingSubscription = null
        _isAddSubscriptionOpen.value = false
    }

    // CRUD Actions
    fun addExpense(
        amount: Double,
        category: String,
        dateMillis: Long,
        description: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = description.ifBlank { category }.trim(),
                amount = amount,
                type = "EXPENSE",
                category = category,
                paymentMethod = paymentMethod,
                dateMillis = dateMillis,
                notes = description.trim()
            )
            repository.insertExpense(entity)
            closeAddExpenseScreen()
        }
    }

    fun addCategory(name: String, type: String = "EXPENSE", colorHex: String = "#10B981", iconName: String = "category", defaultBudget: Double = 0.0) {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    type = type,
                    colorHex = colorHex,
                    iconName = iconName,
                    defaultBudget = defaultBudget
                )
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        paymentMethod: String,
        dateMillis: Long = System.currentTimeMillis(),
        notes: String = ""
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = if (title.isBlank()) category else title.trim(),
                amount = amount,
                type = type.name,
                category = category,
                paymentMethod = paymentMethod,
                dateMillis = dateMillis,
                notes = notes.trim()
            )
            repository.insertTransaction(entity)
            closeQuickAdd()
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
            closeEditTransaction()
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
            closeEditTransaction()
        }
    }

    fun saveBudget(category: String, limit: Double) {
        viewModelScope.launch {
            val monthKey = getMonthKey(_selectedMonthCalendar.value)
            repository.setBudget(category, monthKey, limit)
            closeAddOrEditBudget()
        }
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch {
            val monthKey = getMonthKey(_selectedMonthCalendar.value)
            repository.deleteBudget(monthKey, category)
            closeAddOrEditBudget()
        }
    }

    fun saveSubscription(
        id: Long = 0,
        name: String,
        amount: Double,
        category: String,
        cycle: String,
        nextDueDateMillis: Long,
        paymentMethod: String,
        reminderDays: Int
    ) {
        viewModelScope.launch {
            val sub = SubscriptionEntity(
                id = id,
                name = name.trim(),
                amount = amount,
                category = category,
                billingCycle = cycle,
                nextDueDateMillis = nextDueDateMillis,
                paymentMethod = paymentMethod,
                reminderDaysBefore = reminderDays,
                isActive = true
            )
            if (id == 0L) {
                repository.insertSubscription(sub)
            } else {
                repository.updateSubscription(sub)
            }
            closeAddOrEditSubscription()
        }
    }

    fun markSubscriptionPaid(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.markSubscriptionAsPaid(subscription)
        }
    }

    fun toggleSubscriptionActive(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.updateSubscription(subscription.copy(isActive = !subscription.isActive))
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
            closeAddOrEditSubscription()
        }
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.resetWithSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    // Share Monthly Financial Report
    fun shareMonthlyReport(context: Context) {
        val summary = monthlySummary.value
        val breakdown = categoryBreakdown.value
        val curr = currencySymbol.value

        val reportText = buildString {
            appendLine("📊 POCKET FINANCE – MONTHLY FINANCIAL REPORT")
            appendLine("Period: ${summary.monthName}")
            appendLine("----------------------------------------")
            appendLine("💵 Total Income: $curr${formatAmount(summary.totalIncome)}")
            appendLine("💸 Total Expenses: $curr${formatAmount(summary.totalExpense)}")
            appendLine("💰 Net Savings: $curr${formatAmount(summary.netSavings)}")
            appendLine("🎯 Monthly Budget: $curr${formatAmount(summary.overallBudget)}")
            appendLine("📈 Budget Consumed: ${String.format(Locale.getDefault(), "%.1f", summary.budgetSpentPercentage * 100)}%")
            appendLine("🛡️ Remaining Budget: $curr${formatAmount(summary.remainingBudget)}")
            appendLine("----------------------------------------")
            appendLine("📁 TOP SPENDING CATEGORIES:")
            if (breakdown.isEmpty()) {
                appendLine("No expense records for this month.")
            } else {
                breakdown.sortedByDescending { it.spent }.forEach { item ->
                    appendLine("• ${item.category}: $curr${formatAmount(item.spent)} (${String.format(Locale.getDefault(), "%.1f", item.percentage * 100)}%)")
                }
            }
            appendLine("----------------------------------------")
            appendLine("Generated seamlessly via Pocket Finance App")
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reportText)
            putExtra(Intent.EXTRA_TITLE, "Monthly Financial Report - ${summary.monthName}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Financial Report")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    companion object {
        fun getMonthKey(calendar: Calendar): String {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            return String.format(Locale.US, "%04d-%02d", year, month)
        }

        fun formatAmount(amount: Double): String {
            return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
                minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            }.format(amount)
        }

        fun formatDate(millis: Long, format: String = "dd MMM yyyy"): String {
            return SimpleDateFormat(format, Locale.getDefault()).format(Date(millis))
        }

        private fun calculateMonthlySummary(
            transactions: List<TransactionEntity>,
            cal: Calendar,
            budgets: List<BudgetEntity>
        ): MonthlyFinanceSummary {
            val monthStart = cal.clone() as Calendar
            monthStart.set(Calendar.DAY_OF_MONTH, 1)
            monthStart.set(Calendar.HOUR_OF_DAY, 0)
            monthStart.set(Calendar.MINUTE, 0)
            monthStart.set(Calendar.SECOND, 0)
            monthStart.set(Calendar.MILLISECOND, 0)

            val monthEnd = cal.clone() as Calendar
            monthEnd.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            monthEnd.set(Calendar.HOUR_OF_DAY, 23)
            monthEnd.set(Calendar.MINUTE, 59)
            monthEnd.set(Calendar.SECOND, 59)
            monthEnd.set(Calendar.MILLISECOND, 999)

            val startMillis = monthStart.timeInMillis
            val endMillis = monthEnd.timeInMillis

            val monthTransactions = transactions.filter { it.dateMillis in startMillis..endMillis }

            val totalIncome = monthTransactions.filter { it.type.equals("INCOME", ignoreCase = true) }
                .sumOf { it.amount }
            val totalExpense = monthTransactions.filter { it.type.equals("EXPENSE", ignoreCase = true) }
                .sumOf { it.amount }

            // Today's spending
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val todayEnd = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val todaySpend = transactions.filter {
                it.type.equals("EXPENSE", ignoreCase = true) && it.dateMillis in todayStart..todayEnd
            }.sumOf { it.amount }

            val overallBudgetEntity = budgets.find { it.category == "ALL" }
            val overallBudget = overallBudgetEntity?.limitAmount ?: 35000.0

            val budgetSpentRatio = if (overallBudget > 0) (totalExpense / overallBudget).toFloat() else 0f
            val remainingBudget = (overallBudget - totalExpense).coerceAtLeast(0.0)

            val now = Calendar.getInstance()
            val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentDay = if (cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                now.get(Calendar.DAY_OF_MONTH)
            } else {
                1
            }
            val daysRemaining = (totalDays - currentDay + 1).coerceAtLeast(1)
            val dailyAllowance = if (remainingBudget > 0) remainingBudget / daysRemaining else 0.0

            val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

            return MonthlyFinanceSummary(
                monthKey = getMonthKey(cal),
                monthName = monthName,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                netSavings = totalIncome - totalExpense,
                todaySpend = todaySpend,
                overallBudget = overallBudget,
                budgetSpentPercentage = budgetSpentRatio,
                remainingBudget = remainingBudget,
                dailySpendAllowance = dailyAllowance,
                daysRemainingInMonth = daysRemaining
            )
        }

        private fun calculateCategoryBreakdown(
            transactions: List<TransactionEntity>,
            cal: Calendar,
            budgets: List<BudgetEntity>
        ): List<CategorySpendItem> {
            val monthStart = cal.clone() as Calendar
            monthStart.set(Calendar.DAY_OF_MONTH, 1)
            monthStart.set(Calendar.HOUR_OF_DAY, 0)
            monthStart.set(Calendar.MINUTE, 0)
            monthStart.set(Calendar.SECOND, 0)
            monthStart.set(Calendar.MILLISECOND, 0)

            val monthEnd = cal.clone() as Calendar
            monthEnd.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            monthEnd.set(Calendar.HOUR_OF_DAY, 23)
            monthEnd.set(Calendar.MINUTE, 59)
            monthEnd.set(Calendar.SECOND, 59)
            monthEnd.set(Calendar.MILLISECOND, 999)

            val monthExpenses = transactions.filter {
                it.type.equals("EXPENSE", ignoreCase = true) && it.dateMillis in monthStart.timeInMillis..monthEnd.timeInMillis
            }

            val totalMonthExpense = monthExpenses.sumOf { it.amount }

            val categoryMap = monthExpenses.groupBy { it.category }

            return ExpenseCategory.expenseCategories.map { cat ->
                val spent = categoryMap[cat.displayName]?.sumOf { it.amount } ?: 0.0
                val budget = budgets.find { it.category.equals(cat.displayName, ignoreCase = true) }?.limitAmount ?: 0.0
                val percentage = if (totalMonthExpense > 0) (spent / totalMonthExpense).toFloat() else 0f
                CategorySpendItem(
                    category = cat.displayName,
                    spent = spent,
                    budget = budget,
                    percentage = percentage,
                    color = cat.color
                )
            }.filter { it.spent > 0 || it.budget > 0 }
        }
    }
}

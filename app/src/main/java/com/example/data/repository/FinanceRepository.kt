package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class FinanceRepository(
    private val database: AppDatabase,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pocket_finance_prefs", Context.MODE_PRIVATE)

    private val _currencySymbol = MutableStateFlow(prefs.getString("currency_symbol", "₹") ?: "₹")
    val currencySymbol: Flow<String> = _currencySymbol.asStateFlow()

    private val _lastUsedCategory = MutableStateFlow(prefs.getString("last_category", "Food") ?: "Food")
    val lastUsedCategory: Flow<String> = _lastUsedCategory.asStateFlow()

    private val _lastUsedPaymentMethod = MutableStateFlow(prefs.getString("last_payment_method", "UPI") ?: "UPI")
    val lastUsedPaymentMethod: Flow<String> = _lastUsedPaymentMethod.asStateFlow()

    fun setCurrencySymbol(symbol: String) {
        prefs.edit().putString("currency_symbol", symbol).apply()
        _currencySymbol.value = symbol
    }

    fun saveRecentChoices(category: String, paymentMethod: String) {
        prefs.edit()
            .putString("last_category", category)
            .putString("last_payment_method", paymentMethod)
            .apply()
        _lastUsedCategory.value = category
        _lastUsedPaymentMethod.value = paymentMethod
    }

    fun getCurrency(): String = _currencySymbol.value

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = database.categoryDao().getAllCategories()
    val expenseCategories: Flow<List<CategoryEntity>> = database.categoryDao().getCategoriesByType("EXPENSE")
    val incomeCategories: Flow<List<CategoryEntity>> = database.categoryDao().getCategoriesByType("INCOME")

    suspend fun insertCategory(category: CategoryEntity): Long {
        return database.categoryDao().insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        database.categoryDao().updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        database.categoryDao().deleteCategory(category)
    }

    // Transactions & Expenses
    val allTransactions: Flow<List<TransactionEntity>> = database.transactionDao().getAllTransactions()
    val recentTransactions: Flow<List<TransactionEntity>> = database.transactionDao().getRecentTransactions(20)
    val allExpenses: Flow<List<TransactionEntity>> = database.transactionDao().getAllExpenses()

    fun getExpensesBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> {
        return database.transactionDao().getExpensesBetween(startMillis, endMillis)
    }

    fun getExpensesByCategory(category: String): Flow<List<TransactionEntity>> {
        return database.transactionDao().getExpensesByCategory(category)
    }

    suspend fun insertExpense(expense: TransactionEntity): Long {
        return insertTransaction(expense.copy(type = "EXPENSE"))
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        saveRecentChoices(transaction.category, transaction.paymentMethod)
        return database.transactionDao().insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        database.transactionDao().updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        database.transactionDao().deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        database.transactionDao().deleteById(id)
    }

    // Budgets
    fun getBudgetsForMonth(monthKey: String): Flow<List<BudgetEntity>> {
        return database.budgetDao().getBudgetsForMonth(monthKey)
    }

    suspend fun setBudget(category: String, monthKey: String, limit: Double) {
        val existing = database.budgetDao().getBudget(monthKey, category)
        if (existing != null) {
            database.budgetDao().updateBudget(existing.copy(limitAmount = limit))
        } else {
            database.budgetDao().insertBudget(
                BudgetEntity(category = category, monthYear = monthKey, limitAmount = limit)
            )
        }
    }

    suspend fun deleteBudget(monthKey: String, category: String) {
        database.budgetDao().deleteBudgetByCategory(monthKey, category)
    }

    // Subscriptions
    val allSubscriptions: Flow<List<SubscriptionEntity>> = database.subscriptionDao().getAllSubscriptions()
    val activeSubscriptions: Flow<List<SubscriptionEntity>> = database.subscriptionDao().getActiveSubscriptions()

    suspend fun insertSubscription(subscription: SubscriptionEntity): Long {
        return database.subscriptionDao().insertSubscription(subscription)
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        database.subscriptionDao().updateSubscription(subscription)
    }

    suspend fun deleteSubscription(subscription: SubscriptionEntity) {
        database.subscriptionDao().deleteSubscription(subscription)
    }

    suspend fun markSubscriptionAsPaid(subscription: SubscriptionEntity) {
        val now = System.currentTimeMillis()
        // Record as an expense transaction
        val newTx = TransactionEntity(
            title = "${subscription.name} (Subscription)",
            amount = subscription.amount,
            type = "EXPENSE",
            category = subscription.category,
            paymentMethod = subscription.paymentMethod,
            dateMillis = now,
            notes = "Auto-recorded recurring subscription payment",
            isRecurring = true,
            recurringSourceId = subscription.id
        )
        insertTransaction(newTx)

        // Advance next due date based on cycle
        val cal = Calendar.getInstance()
        cal.timeInMillis = if (subscription.nextDueDateMillis > now) subscription.nextDueDateMillis else now
        when (subscription.billingCycle.lowercase()) {
            "weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "yearly" -> cal.add(Calendar.YEAR, 1)
            else -> cal.add(Calendar.MONTH, 1)
        }

        val updatedSub = subscription.copy(
            nextDueDateMillis = cal.timeInMillis,
            lastPaidDateMillis = now
        )
        updateSubscription(updatedSub)
    }

    suspend fun resetWithSampleData() {
        database.transactionDao().deleteAllTransactions()
        database.categoryDao().deleteAllCategories()
        database.budgetDao().deleteAllBudgets()
        database.subscriptionDao().deleteAllSubscriptions()
        AppDatabase.populateInitialData(database)
    }

    suspend fun clearAllData() {
        database.transactionDao().deleteAllTransactions()
        database.categoryDao().deleteAllCategories()
        database.budgetDao().deleteAllBudgets()
        database.subscriptionDao().deleteAllSubscriptions()
    }
}

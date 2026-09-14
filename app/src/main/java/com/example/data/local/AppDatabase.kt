package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        SubscriptionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pocket_finance_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val now = Calendar.getInstance()
            val currentYear = now.get(Calendar.YEAR)
            val currentMonth = now.get(Calendar.MONTH) + 1
            val monthKey = String.format("%04d-%02d", currentYear, currentMonth)

            // 0. Initial Categories
            val categories = listOf(
                CategoryEntity(name = "Food", type = "EXPENSE", colorHex = "#F97316", iconName = "restaurant", defaultBudget = 8000.0, isDefault = true),
                CategoryEntity(name = "Groceries", type = "EXPENSE", colorHex = "#10B981", iconName = "shopping_cart", defaultBudget = 6000.0, isDefault = true),
                CategoryEntity(name = "Transport", type = "EXPENSE", colorHex = "#3B82F6", iconName = "directions_bus", defaultBudget = 4000.0, isDefault = true),
                CategoryEntity(name = "Shopping", type = "EXPENSE", colorHex = "#EC4899", iconName = "shopping_bag", defaultBudget = 5000.0, isDefault = true),
                CategoryEntity(name = "Bills", type = "EXPENSE", colorHex = "#EAB308", iconName = "receipt_long", defaultBudget = 4500.0, isDefault = true),
                CategoryEntity(name = "Entertainment", type = "EXPENSE", colorHex = "#8B5CF6", iconName = "movie", defaultBudget = 3000.0, isDefault = true),
                CategoryEntity(name = "Health", type = "EXPENSE", colorHex = "#EF4444", iconName = "medical_services", defaultBudget = 2500.0, isDefault = true),
                CategoryEntity(name = "Travel", type = "EXPENSE", colorHex = "#06B6D4", iconName = "flight", defaultBudget = 2000.0, isDefault = true),
                CategoryEntity(name = "Education", type = "EXPENSE", colorHex = "#14B8A6", iconName = "school", defaultBudget = 2000.0, isDefault = true),
                CategoryEntity(name = "Other", type = "EXPENSE", colorHex = "#64748B", iconName = "more_horiz", defaultBudget = 1000.0, isDefault = true),
                CategoryEntity(name = "Salary", type = "INCOME", colorHex = "#10B981", iconName = "payments", defaultBudget = 0.0, isDefault = true),
                CategoryEntity(name = "Freelance", type = "INCOME", colorHex = "#059669", iconName = "work", defaultBudget = 0.0, isDefault = true),
                CategoryEntity(name = "Investment", type = "INCOME", colorHex = "#6366F1", iconName = "trending_up", defaultBudget = 0.0, isDefault = true)
            )
            database.categoryDao().insertAll(categories)

            // 1. Initial Monthly Budgets
            val budgets = listOf(
                BudgetEntity(category = "ALL", monthYear = monthKey, limitAmount = 35000.0),
                BudgetEntity(category = "Food", monthYear = monthKey, limitAmount = 8000.0),
                BudgetEntity(category = "Groceries", monthYear = monthKey, limitAmount = 6000.0),
                BudgetEntity(category = "Transport", monthYear = monthKey, limitAmount = 4000.0),
                BudgetEntity(category = "Shopping", monthYear = monthKey, limitAmount = 5000.0),
                BudgetEntity(category = "Bills", monthYear = monthKey, limitAmount = 4500.0),
                BudgetEntity(category = "Entertainment", monthYear = monthKey, limitAmount = 3000.0),
                BudgetEntity(category = "Health", monthYear = monthKey, limitAmount = 2500.0),
                BudgetEntity(category = "Travel", monthYear = monthKey, limitAmount = 2000.0)
            )
            database.budgetDao().insertAll(budgets)

            // 2. Initial Subscriptions & Bills
            val billCal1 = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 2) }
            val billCal2 = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) }
            val billCal3 = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 11) }
            val billCal4 = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 18) }

            val subscriptions = listOf(
                SubscriptionEntity(
                    name = "Netflix Premium",
                    amount = 649.0,
                    category = "Entertainment",
                    billingCycle = "Monthly",
                    nextDueDateMillis = billCal1.timeInMillis,
                    paymentMethod = "Credit Card",
                    reminderDaysBefore = 2
                ),
                SubscriptionEntity(
                    name = "Broadband WiFi",
                    amount = 999.0,
                    category = "Bills",
                    billingCycle = "Monthly",
                    nextDueDateMillis = billCal2.timeInMillis,
                    paymentMethod = "UPI",
                    reminderDaysBefore = 3
                ),
                SubscriptionEntity(
                    name = "Spotify Family",
                    amount = 179.0,
                    category = "Entertainment",
                    billingCycle = "Monthly",
                    nextDueDateMillis = billCal3.timeInMillis,
                    paymentMethod = "Debit Card",
                    reminderDaysBefore = 1
                ),
                SubscriptionEntity(
                    name = "Gym Membership",
                    amount = 1500.0,
                    category = "Health",
                    billingCycle = "Monthly",
                    nextDueDateMillis = billCal4.timeInMillis,
                    paymentMethod = "UPI",
                    reminderDaysBefore = 2
                )
            )
            database.subscriptionDao().insertAll(subscriptions)

            // 3. Initial Transactions (Income & Expenses matching example dashboard: ~45,000 income, ~21,450 expenses)
            val todayCal = Calendar.getInstance()
            val yestCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
            val daysAgo3 = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -3) }
            val daysAgo5 = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -5) }
            val daysAgo8 = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -8) }
            val monthStart = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }

            val transactions = listOf(
                // Income
                TransactionEntity(
                    title = "Monthly Salary",
                    amount = 45000.0,
                    type = "INCOME",
                    category = "Salary",
                    paymentMethod = "Bank Transfer",
                    dateMillis = monthStart.timeInMillis,
                    notes = "August Primary Salary"
                ),
                // Today's spending
                TransactionEntity(
                    title = "Lunch with Team",
                    amount = 450.0,
                    type = "EXPENSE",
                    category = "Food",
                    paymentMethod = "UPI",
                    dateMillis = todayCal.timeInMillis,
                    notes = "Burrito Bowl & Beverage"
                ),
                TransactionEntity(
                    title = "Metro Card Recharge",
                    amount = 200.0,
                    type = "EXPENSE",
                    category = "Transport",
                    paymentMethod = "UPI",
                    dateMillis = todayCal.timeInMillis,
                    notes = "Smart card top-up"
                ),
                // Yesterday's spending
                TransactionEntity(
                    title = "Supermarket Groceries",
                    amount = 2850.0,
                    type = "EXPENSE",
                    category = "Groceries",
                    paymentMethod = "Credit Card",
                    dateMillis = yestCal.timeInMillis,
                    notes = "Fresh vegetables, fruits, and staples"
                ),
                TransactionEntity(
                    title = "Cinema Tickets",
                    amount = 750.0,
                    type = "EXPENSE",
                    category = "Entertainment",
                    paymentMethod = "UPI",
                    dateMillis = yestCal.timeInMillis,
                    notes = "Weekend movie"
                ),
                // Earlier this month
                TransactionEntity(
                    title = "Electricity & Water Bill",
                    amount = 2350.0,
                    type = "EXPENSE",
                    category = "Bills",
                    paymentMethod = "UPI",
                    dateMillis = daysAgo3.timeInMillis,
                    notes = "Utility payments"
                ),
                TransactionEntity(
                    title = "New Casual Sneakers",
                    amount = 3450.0,
                    type = "EXPENSE",
                    category = "Shopping",
                    paymentMethod = "Debit Card",
                    dateMillis = daysAgo5.timeInMillis,
                    notes = "Shopping sale"
                ),
                TransactionEntity(
                    title = "House Rent Contribution",
                    amount = 10000.0,
                    type = "EXPENSE",
                    category = "Bills",
                    paymentMethod = "Bank Transfer",
                    dateMillis = monthStart.timeInMillis + 86400000L,
                    notes = "Monthly flat rent"
                ),
                TransactionEntity(
                    title = "Pharmacy & Vitamins",
                    amount = 850.0,
                    type = "EXPENSE",
                    category = "Health",
                    paymentMethod = "Cash",
                    dateMillis = daysAgo8.timeInMillis,
                    notes = "Health supplements"
                ),
                TransactionEntity(
                    title = "Fuel refill",
                    amount = 550.0,
                    type = "EXPENSE",
                    category = "Transport",
                    paymentMethod = "UPI",
                    dateMillis = daysAgo8.timeInMillis,
                    notes = "Full tank petrol"
                )
            )
            database.transactionDao().insertAll(transactions)
        }
    }
}

package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.TransactionDao
import com.example.data.model.TransactionEntity
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransactionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var transactionDao: TransactionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        transactionDao = database.transactionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndFetchTransaction() = runBlocking {
        val tx = TransactionEntity(
            title = "Organic Grocery",
            amount = 45.50,
            type = "EXPENSE",
            category = "Groceries",
            dateMillis = System.currentTimeMillis(),
            paymentMethod = "Credit Card",
            notes = "Weekly grocery shopping"
        )
        val insertedId = transactionDao.insertTransaction(tx)
        assertTrue(insertedId > 0)

        val fetched = transactionDao.getTransactionById(insertedId)
        assertNotNull(fetched)
        assertEquals("Organic Grocery", fetched?.title)
        assertEquals(45.50, fetched?.amount ?: 0.0, 0.001)
        assertEquals("EXPENSE", fetched?.type)
        assertEquals("Groceries", fetched?.category)
    }

    @Test
    fun deleteAndUndoTransaction() = runBlocking {
        val tx = TransactionEntity(
            title = "Movie Tickets",
            amount = 28.00,
            type = "EXPENSE",
            category = "Entertainment",
            dateMillis = System.currentTimeMillis(),
            paymentMethod = "UPI",
            notes = "Weekend cinema"
        )
        val id = transactionDao.insertTransaction(tx)
        val savedTx = transactionDao.getTransactionById(id)
        assertNotNull(savedTx)

        // Delete
        transactionDao.deleteTransaction(savedTx!!)
        val afterDelete = transactionDao.getTransactionById(id)
        assertNull(afterDelete)

        // Undo (re-insert)
        transactionDao.insertTransaction(savedTx)
        val afterUndo = transactionDao.getTransactionById(id)
        assertNotNull(afterUndo)
        assertEquals("Movie Tickets", afterUndo?.title)
    }

    @Test
    fun fetchExpensesBetweenDates() = runBlocking {
        val now = System.currentTimeMillis()
        val tx1 = TransactionEntity(
            title = "Coffee",
            amount = 4.50,
            type = "EXPENSE",
            category = "Food & Dining",
            dateMillis = now - 5000,
            paymentMethod = "Cash"
        )
        val tx2 = TransactionEntity(
            title = "Salary",
            amount = 3000.00,
            type = "INCOME",
            category = "Salary",
            dateMillis = now,
            paymentMethod = "Bank Transfer"
        )
        transactionDao.insertTransaction(tx1)
        transactionDao.insertTransaction(tx2)

        val allTxs = transactionDao.getAllTransactions().first()
        assertEquals(2, allTxs.size)

        val expenses = transactionDao.getAllExpenses().first()
        assertEquals(1, expenses.size)
        assertEquals("Coffee", expenses[0].title)
    }

    @Test
    fun testAmountFormatting() {
        val formatted = FinanceViewModel.formatAmount(1234.567)
        assertTrue(formatted.contains("1,234.57") || formatted.contains("1.234,57") || formatted.contains("1234.57"))

        val formattedZero = FinanceViewModel.formatAmount(0.0)
        assertEquals("0", formattedZero)
    }
}

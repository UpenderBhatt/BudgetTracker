package com.example.mob_dev_portfolio.data.db.dao

import androidx.room.*
import com.example.mob_dev_portfolio.data.db.entity.Expense
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE budgetId = :budgetId ORDER BY date DESC")
    fun getExpensesForBudget(budgetId: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE budgetID = :budgetId")
    fun getTotalSpentForBudget(budgetId: Long): Flow<Double?>

    @Query("""
        SELECT SUM(amount) FROM expenses
        WHERE budgetId = :budgetId AND categoryId = :categoryId
    """)
    fun getSpentByCategoryForBudget(budgetId: Long, categoryId: Long): Flow<Double?>

    @Query("""
        SELECT categoryId, SUM(amount) as total
        FROM expenses
        WHERE budgetId = :budgetId 
        GROUP BY categoryId
    """)
    fun getSpendingByCategory(budgetId: Long): Flow<List<CategorySpending>>

    @Query("SELECT SUM(amount) FROM expenses WHERE budgetId = :budgetId")
    suspend fun getTotalSpentForBudgetOnce(budgetId: Long): Double?

    @Query("""
        SELECT categoryId, SUM(amount) as total
        FROM expenses
        WHERE budgetId = :budgetId
        GROUP BY categoryId
    """)
    suspend fun getSpendingByCategoryOnce(budgetId: Long): List<CategorySpending>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("""
        SELECT * FROM expenses
        WHERE isRecurring = 1 AND budgetId = :budgetId
    """)
    suspend fun getRecurringExpenses(budgetId: Long): List<Expense>

    @Query("""
        SELECT * FROM expenses
        WHERE budgetId = :budgetId
        ORDER BY date DESC
        LIMIT :limit
    """)
    fun getRecentExpenses(budgetId: Long, limit: Int = 5): Flow<List<Expense>>

}
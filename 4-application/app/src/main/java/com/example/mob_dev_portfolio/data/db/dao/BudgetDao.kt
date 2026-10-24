package com.example.mob_dev_portfolio.data.db.dao

import androidx.room.*
import com.example.mob_dev_portfolio.data.db.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE isActive = 1 LIMIT 1")
    fun getActiveBudget(): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveBudgetOnce(): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Query("UPDATE budgets SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAll()

    @Query("SELECT * FROM budgets WHERE isActive = 0 ORDER BY endDate DESC LIMIT 1")
    suspend fun getPreviousBudget(): Budget?
}
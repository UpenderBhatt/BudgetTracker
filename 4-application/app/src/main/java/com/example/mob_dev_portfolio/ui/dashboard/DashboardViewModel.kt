package com.example.mob_dev_portfolio.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.example.mob_dev_portfolio.BudgetTrackingApp
import com.example.mob_dev_portfolio.data.db.entity.Budget
import com.example.mob_dev_portfolio.data.db.entity.Expense
import com.example.mob_dev_portfolio.data.db.entity.Category
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BudgetTrackingApp
    private val budgetDao = app.database.budgetDao()
    private val expenseDao = app.database.expenseDao()
    private val categoryDao = app.database.categoryDao()

    val activeBudget: LiveData<Budget?> = budgetDao.getActiveBudget().asLiveData()

    val categories: LiveData<List<Category>> = categoryDao.getAllCategories().asLiveData()

    val expenses: LiveData<List<Expense>> = activeBudget.switchMap { budget ->
        if (budget != null) {
            expenseDao.getExpensesForBudget(budget.id).asLiveData()
        } else {
            MutableLiveData(emptyList())
        }
    }

    val totalSpent: LiveData<Double?> = activeBudget.switchMap { budget ->
        if (budget != null) {
            expenseDao.getTotalSpentForBudget(budget.id).asLiveData()
        } else {
            MutableLiveData(null)
        }
    }

    val recentExpenses: LiveData<List<Expense>> = activeBudget.switchMap { budget ->
        if (budget != null) {
            expenseDao.getRecentExpenses(budget.id, 5).asLiveData()
        } else {
            MutableLiveData(emptyList())
        }
    }

    val categorySpending: LiveData<List<com.example.mob_dev_portfolio.data.db.dao.CategorySpending>> =
        activeBudget.switchMap { budget ->
            if (budget != null) {
                expenseDao.getSpendingByCategory(budget.id).asLiveData()
            } else {
                MutableLiveData(emptyList())
            }
        }

    fun getDaysRemianing(budget: Budget): Int {
        val now = Calendar.getInstance().time
        val diff = budget.endDate.time - now.time
        return maxOf(0, TimeUnit.MILLISECONDS.toDays(diff).toInt())
    }

    fun getSpendingPercentatge(spent: Double, budget: Budget): Float {
        if (budget.amount <= 0) return 0f
        return (spent / budget.amount * 100).toFloat().coerceIn(0f, 100f)
    }

    fun generateTip(
        spent: Double,
        budget: Budget,
        categorySpending: List<com.example.mob_dev_portfolio.data.db.dao.CategorySpending>,
        categories: List<Category>
    ): String? {
        val percentage = spent / budget.amount * 100
        val daysLeft = getDaysRemianing(budget)
        val categoryMap = categories.associateBy { it.id }


        if (percentage >= 100) {
            return "You've exceeded your budget! Try to limit spending for the rest of this period."
        }

        if (percentage >= 90) {
            return "You've used 90% of your budget with $daysLeft days remaining. Be careful!"
        }

        val topCategory = categorySpending.maxByOrNull { it.total }
        if (topCategory != null) {
            val catName = topCategory.categoryId?.let { categoryMap[it]?.name } ?: "Uncategorized"
            val catPercentage = (topCategory.total / spent * 100).toInt()

            if (catPercentage > 50) {
                return "$catName accounts for $catPercentage% of your spending. Consider diversifying your expenses."
            }
        }

        if (percentage < 30 && daysLeft < 7) {
            return "Great job! You've only used ${percentage.toInt()}% of your budget"
        }

        if (daysLeft > 0) {
            val remaining = budget.amount - spent
            val dailyBudget = remaining / daysLeft
            return String.format(
                java.util.Locale.UK,
                "You can spend about £%.2f per day for the remaining %d days.",
                dailyBudget,
                daysLeft
            )
        }
        return null
    }
}
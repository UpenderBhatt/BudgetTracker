package com.example.mob_dev_portfolio.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.mob_dev_portfolio.BudgetTrackingApp
import com.example.mob_dev_portfolio.data.db.dao.CategorySpending
import com.example.mob_dev_portfolio.data.db.entity.Budget
import com.example.mob_dev_portfolio.data.db.entity.Category
import com.example.mob_dev_portfolio.data.db.entity.Expense
import kotlinx.coroutines.launch

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BudgetTrackingApp
    private val budgetDao = app.database.budgetDao()
    private val expenseDao = app.database.expenseDao()
    private val categoryDao = app.database.categoryDao()

    val activeBudget: LiveData<Budget?> = budgetDao.getActiveBudget().asLiveData()

    val categories: LiveData<List<Category>> = categoryDao.getAllCategories().asLiveData()

    val categorySpending: LiveData<List<CategorySpending>> = activeBudget.switchMap { budget ->
            if (budget != null) {
                expenseDao.getSpendingByCategory(budget.id).asLiveData()
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

    val expenses: LiveData<List<Expense>> = activeBudget.switchMap { budget ->
            if (budget != null) {
                expenseDao.getExpensesForBudget(budget.id).asLiveData()
            } else {
                MutableLiveData(emptyList())
            }
        }

    private val _comparisonData = MutableLiveData<List<ComparisonItem>>()
    val comparisonData: LiveData<List<ComparisonItem>> = _comparisonData

    private val _hasPreviousPeriod = MutableLiveData(false)
    val hasPreviousPeriod: LiveData<Boolean> = _hasPreviousPeriod

    fun loadComparisonData(
        currentSpending: List<CategorySpending>,
        categories: List<Category>
    ) {
        viewModelScope.launch {
            val previousBudget = budgetDao.getPreviousBudget()

            if (previousBudget == null) {
                _hasPreviousPeriod.value = false
                _comparisonData.value = emptyList()
                return@launch
            }

            _hasPreviousPeriod.value = true

            val previousSpending = expenseDao.getSpendingByCategoryOnce(previousBudget.id)
            val previousMap = previousSpending.associateBy { it.categoryId }
            val categoryMap = categories.associateBy { it.id }

            val allCategoryIds = (currentSpending.mapNotNull { it.categoryId} + previousSpending.mapNotNull { it.categoryId }).distinct()

            val maxAmount = maxOf(
                currentSpending.maxOfOrNull { it.total } ?: 0.0,
                previousSpending.maxOfOrNull { it.total } ?: 0.0
            )

            val items = allCategoryIds.mapNotNull { catId ->
                val category = categoryMap[catId] ?: return@mapNotNull null
                val currentAmount = currentSpending
                    .find { it.categoryId == catId }?.total ?: 0.0
                val previousAmount = previousMap[catId]?.total ?: 0.0

                ComparisonItem(
                    categoryName = category.name,
                    colour = category.colour,
                    currentAmount = currentAmount,
                    previousAmount = previousAmount,
                    maxAmount = maxAmount
                )
            }.sortedByDescending { it.currentAmount }

            _comparisonData.value = items
        }
    }

    fun generateInsight(
        totalSpent: Double,
        budget: Budget,
        categorySpending: List<CategorySpending>,
        categories: List<Category>
    ): String? {

        val categoryMap = categories.associateBy { it.id }

        if (categorySpending.isEmpty()) return null

        val topCategory = categorySpending.maxByOrNull { it.total }
        val topName = topCategory?.categoryId?.let { categoryMap[it]?.name } ?: return null
        val topAmount = topCategory.total
        val topPercentage = (topAmount / totalSpent * 100).toInt()

        val bottomCategory = categorySpending.minByOrNull { it.total }
        val bottomName = bottomCategory?.categoryId?.let { categoryMap[it]?.name }

        if (bottomName != null && categorySpending.size > 1) {
            return "Your biggest expense category is $topName at $topPercentage% of total spending. " +
                    "$bottomName is your lowest at £${String.format(java.util.Locale.UK, "%.0f", bottomCategory.total)}."
        }
        return "$topName makes up $topPercentage% of your spending this period."
    }

}
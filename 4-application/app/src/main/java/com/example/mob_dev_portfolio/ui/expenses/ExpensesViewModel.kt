package com.example.mob_dev_portfolio.ui.expenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.mob_dev_portfolio.BudgetTrackingApp
import com.example.mob_dev_portfolio.data.db.entity.Budget
import com.example.mob_dev_portfolio.data.db.entity.Category
import com.example.mob_dev_portfolio.data.db.entity.Expense
import kotlinx.coroutines.launch

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BudgetTrackingApp
    private val expenseDao = app.database.expenseDao()
    private val categoryDao = app.database.categoryDao()
    private val budgetDao = app.database.budgetDao()

    val activeBudget: LiveData<Budget?> = budgetDao.getActiveBudget().asLiveData()

    val categories: LiveData<List<Category>> = categoryDao.getAllCategories().asLiveData()

    val expenses: LiveData<List<Expense>> = activeBudget.switchMap { budget ->
        if (budget != null) {
            expenseDao.getExpensesForBudget(budget.id).asLiveData()
        } else {
            MutableLiveData(emptyList())
        }
    }

    val totalSpent: LiveData<Double?> = activeBudget.switchMap {budget ->
        if (budget != null) {
            expenseDao.getTotalSpentForBudget(budget.id).asLiveData()
        } else {
            MutableLiveData(null)
        }
    }

    private val _expenseDeleted = MutableLiveData<Expense?>()
    val expenseDeleted: LiveData<Expense?> = _expenseDeleted

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.delete(expense)
            _expenseDeleted.value = expense
        }
    }

    fun undoDelete(expense: Expense) {
        viewModelScope.launch {
            expenseDao.insert(expense)
            _expenseDeleted.value = null
        }
    }

    fun onDeleteHandled() {
        _expenseDeleted.value = null
    }
}
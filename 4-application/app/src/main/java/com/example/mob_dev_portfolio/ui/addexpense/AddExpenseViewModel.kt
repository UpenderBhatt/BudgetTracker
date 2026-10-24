package com.example.mob_dev_portfolio.ui.addexpense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mob_dev_portfolio.BudgetTrackingApp
import com.example.mob_dev_portfolio.data.db.entity.Category
import com.example.mob_dev_portfolio.data.db.entity.Expense
import com.example.mob_dev_portfolio.util.BudgetThresholdChecker
import kotlinx.coroutines.launch
import java.util.Date

class AddExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BudgetTrackingApp
    private val expenseDao = app.database.expenseDao()
    private val categoryDao = app.database.categoryDao()
    private val budgetDao = app.database.budgetDao()
    private val thresholdChecker = BudgetThresholdChecker(application)

    val categories: LiveData<List<Category>> = categoryDao.getAllCategories().asLiveData()

    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory

    private val _selectedDate = MutableLiveData(Date())
    val selectedDate: LiveData<Date> = _selectedDate

    private val _isRecurring = MutableLiveData(false)
    val isRecurring: LiveData<Boolean> = _isRecurring

    private val _recurringFrequency = MutableLiveData("MONTHLY")
    val recurringFrequency: LiveData<String> = _recurringFrequency

    private val _expenseSaved = MutableLiveData<Boolean>()
    val expenseSaved: LiveData<Boolean> = _expenseSaved

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
    }

    fun selectDate(date: Date) {
        _selectedDate.value = date
    }

    fun setRecurring(recurring: Boolean) {
        _isRecurring.value = recurring
    }

    fun setRecurringFrequency(frequency: String) {
        _recurringFrequency.value = frequency
    }

    fun saveExpense(amountText: String, description: String) {
        _errorMessage.value = null

        if(amountText.isBlank()) {
            _errorMessage.value = "Please enter an amount"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _errorMessage.value = "Please enter a valid amount"
            return
        }

        val category = _selectedCategory.value
        if(category == null) {
            _errorMessage.value = "Please select a category"
            return
        }

        viewModelScope.launch {
            val budget = budgetDao.getActiveBudgetOnce()

            if (budget == null) {
                _errorMessage.value = "Please set up a budget first"
                return@launch
            }

            val expense = Expense(
                amount = amount,
                categoryId = category.id,
                description = description.trim(),
                date = _selectedDate.value ?: Date(),
                isRecurring = _isRecurring.value ?: false,
                recurringFrequency = if (_isRecurring.value == true) {
                    _recurringFrequency.value
                } else {
                    null
                },
                budgetId = budget.id
            )

            expenseDao.insert(expense)

            val totalSpent = expenseDao.getTotalSpentForBudgetOnce(budget.id) ?: 0.0
            thresholdChecker.checkThresholds(totalSpent, budget.amount)

            _expenseSaved.value = true
        }
    }

    fun resetForm() {
        _selectedCategory.value = null
        _selectedDate.value = Date()
        _isRecurring.value = false
        _recurringFrequency.value = "Monthly"
        _expenseSaved.value = false
    }
}
package com.example.mob_dev_portfolio.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mob_dev_portfolio.BudgetTrackingApp
import com.example.mob_dev_portfolio.data.db.entity.Budget
import java.util.Date
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BudgetTrackingApp
    private val budgetDao = app.database.budgetDao()
    private val settingsDataStore = app.settingsDataStore

    val activeBudget: LiveData<Budget?> = budgetDao.getActiveBudget().asLiveData()

    private val _selectedPeriodType = MutableLiveData(Budget.PERIOD_WEEKLY)
    val selectedPeriodType: LiveData<String> = _selectedPeriodType

    private val _selectedStartDate = MutableLiveData(Date())
    val selectedStartDate: LiveData<Date> = _selectedStartDate

    private val _budgetSaved = MutableLiveData<Boolean>()
    val budgetSaved: LiveData<Boolean> = _budgetSaved

    private val _amountError = MutableLiveData<String?>()
    val amountError: LiveData<String?> = _amountError

    val alertsEnabled: LiveData<Boolean> = settingsDataStore.alertsEnabled.asLiveData()
    val autoRenewEnabled: LiveData<Boolean> = settingsDataStore.autoRenewBudget.asLiveData()

    fun selectPeriodType(periodType: String) {
        _selectedPeriodType.value = periodType
    }

    fun selectStartDate(date: Date) {
        _selectedStartDate.value = date
    }

    fun updateAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAlertsEnabled(enabled)
        }
    }

    fun updateAutoRenew(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAutoRenewBudget(enabled)
        }
    }

    fun saveBudget(amountText: String) {
        if(amountText.isBlank()) {
            _amountError.value = "Please enter a budget amount"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if(amount == null) {
            _amountError.value = "Please enter a valid amount"
            return
        }

        if(amount <= 0) {
            _amountError.value = "Amount must be greater than zero"
            return
        }

        _amountError.value = null

        val startDate = _selectedStartDate.value ?: Date()
        val periodType = _selectedPeriodType.value ?: Budget.PERIOD_WEEKLY
        val endDate = calculateEndDate(startDate, periodType)

        viewModelScope.launch {
            budgetDao.deactivateAll()
            settingsDataStore.resetAlertFlags()

            val budget = Budget(
                amount = amount,
                periodType = periodType,
                startDate = startDate,
                endDate = endDate,
                isActive = true
            )
            budgetDao.insert(budget)

            _budgetSaved.value = true
            _budgetSaved.value = false
        }
    }

    private fun calculateEndDate(startDate: Date, periodType: String): Date {
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        when (periodType) {
            Budget.PERIOD_WEEKLY -> calendar.add(Calendar.DAY_OF_YEAR, 7)
            Budget.PERIOD_MONTHLY -> calendar.add(Calendar.MONTH, 1)
        }

        return calendar.time
    }

    fun onBudgetSavedHandled() {
        _budgetSaved.value = false
    }
}
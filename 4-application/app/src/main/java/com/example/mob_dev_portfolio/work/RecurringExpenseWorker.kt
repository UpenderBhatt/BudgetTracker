package com.example.mob_dev_portfolio.work

import android.content.Context
import android.icu.util.Calendar
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mob_dev_portfolio.BudgetTrackingApp
import com.example.mob_dev_portfolio.data.db.entity.Budget
import com.example.mob_dev_portfolio.data.db.entity.Expense
import com.example.mob_dev_portfolio.util.BudgetThresholdChecker
import kotlinx.coroutines.flow.first
import java.util.Date

class RecurringExpenseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as BudgetTrackingApp
        val expenseDao = app.database.expenseDao()
        val budgetDao = app.database.budgetDao()
        val settingsDataStore = app.settingsDataStore

        val budget = budgetDao.getActiveBudgetOnce() ?: return Result.success()
        val now = Date()

        if (now.after(budget.endDate)) {
            val autoRenew = settingsDataStore.autoRenewBudget.first()

            if(autoRenew) {
                budgetDao.deactivateAll()
                settingsDataStore.resetAlertFlags()

                val newStartDate = budget.endDate
                val newEndDate = calculateEndDate(newStartDate, budget.periodType)

                val newBudget = Budget(
                    amount = budget.amount,
                    periodType = budget.periodType,
                    startDate = newStartDate,
                    endDate = newEndDate,
                    isActive = true
                )
                budgetDao.insert(newBudget)

                val recurringExpenses = expenseDao.getRecurringExpenses(budget.id)
                val newBudgetFromDb = budgetDao.getActiveBudgetOnce() ?: return Result.success()

                for (expense in recurringExpenses) {
                    val templateExpense = Expense(
                        amount = expense.amount,
                        categoryId = expense.categoryId,
                        description = expense.description,
                        date = now,
                        isRecurring = true,
                        recurringFrequency = expense.recurringFrequency,
                        budgetId = newBudgetFromDb.id
                    )
                    expenseDao.insert(templateExpense)
                }
            }
            return Result.success()
        }

        val recurringExpenses = expenseDao.getRecurringExpenses(budget.id)
        var deductedAny = false

        for (expense in recurringExpenses) {
            if (shouldDeductToday(expense)) {
                val newExpense = Expense(
                    amount = expense.amount,
                    categoryId = expense.categoryId,
                    description = expense.description + " (auto)",
                    date = now,
                    isRecurring = false,
                    recurringFrequency = null,
                    budgetId = budget.id
                )
                expenseDao.insert(newExpense)
                deductedAny = true
            }
        }

        if (deductedAny) {
            val totalSpent = expenseDao.getTotalSpentForBudgetOnce(budget.id) ?: 0.0
            val thresholdChecker = BudgetThresholdChecker(applicationContext)
            thresholdChecker.checkThresholds(totalSpent, budget.amount)
        }

        return Result.success()
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

    private fun shouldDeductToday(expense: Expense): Boolean {
        val now = Calendar.getInstance()
        val expenseDate = Calendar.getInstance()
        expenseDate.time = expense.date

        return when (expense.recurringFrequency) {
            "DAILY" -> true

            "WEEKLY" -> {
                now.get(Calendar.DAY_OF_WEEK) == expenseDate.get(Calendar.DAY_OF_WEEK)
            }

            "MONTHLY" -> {
                val targetDay = expenseDate.get(Calendar.DAY_OF_MONTH)
                val todayDay = now.get(Calendar.DAY_OF_MONTH)
                val lastDayOfMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)

                if (targetDay > lastDayOfMonth) {
                    todayDay == lastDayOfMonth
                } else {
                    todayDay == targetDay
                }
            }
            else -> false
        }
    }
}
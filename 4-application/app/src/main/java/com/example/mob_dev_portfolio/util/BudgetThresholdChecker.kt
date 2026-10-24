package com.example.mob_dev_portfolio.util

import android.content.Context
import com.example.mob_dev_portfolio.BudgetTrackingApp
import com.example.mob_dev_portfolio.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.first
import java.util.Locale

class BudgetThresholdChecker(private val context: Context) {

    private val notificationHelper = NotificationHelper(context)
    private val settingsDataStore = (context.applicationContext as BudgetTrackingApp).settingsDataStore

    suspend fun checkThresholds(spent: Double, budgetAmount: Double) {
        val alertsEnabled = settingsDataStore.alertsEnabled.first()

        if (!alertsEnabled) return

        if (budgetAmount <=0) return

        val percentage = (spent / budgetAmount) * 100
        val remaining = budgetAmount - spent

        if (percentage > 100) {
            val alreadyShown = settingsDataStore.isAlertShown(SettingsDataStore.ALERT_OVER_SHOWN).first()
            if (!alreadyShown) {
                val overAmount = String.format(Locale.UK, "%.2f", spent - budgetAmount)
                notificationHelper.showOverBudget(overAmount)
                settingsDataStore.markAlertShown(SettingsDataStore.ALERT_OVER_SHOWN)
            }
        }
        else if (percentage >= 100) {
            val alreadyShown = settingsDataStore.isAlertShown(SettingsDataStore.ALERT_100_SHOWN).first()
            if (!alreadyShown) {
                val amount = String.format(Locale.UK, "%.2f", budgetAmount)
                notificationHelper.showBudgetReached(amount)
                settingsDataStore.markAlertShown(SettingsDataStore.ALERT_100_SHOWN)
            }
        }

        if (percentage >= 90) {
            val alreadyShown = settingsDataStore.isAlertShown(SettingsDataStore.ALERT_90_SHOWN).first()
            if (!alreadyShown) {
                val remainingStr = String.format(Locale.UK, "%.2f", maxOf(0.0, remaining))
                notificationHelper.showBudgetWarning90(remainingStr)
                settingsDataStore.markAlertShown(SettingsDataStore.ALERT_90_SHOWN)
            }
        }

        if (percentage >= 70) {
            val alreadyShown = settingsDataStore.isAlertShown(SettingsDataStore.ALERT_70_SHOWN).first()
            if (!alreadyShown) {
                val remainingStr = String.format(Locale.UK, "%.2f", remaining)
                notificationHelper.showBudgetWarning70(remainingStr)
                settingsDataStore.markAlertShown(SettingsDataStore.ALERT_70_SHOWN)
            }
        }
    }
}
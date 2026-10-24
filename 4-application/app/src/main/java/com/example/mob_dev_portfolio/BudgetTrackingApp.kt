package com.example.mob_dev_portfolio

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.example.mob_dev_portfolio.data.db.BudgetTrackerDatabase
import com.example.mob_dev_portfolio.data.datastore.SettingsDataStore
import com.example.mob_dev_portfolio.work.RecurringExpenseWorker
import java.util.concurrent.TimeUnit

class BudgetTrackingApp : Application() {

    val database: BudgetTrackerDatabase by lazy {
        BudgetTrackerDatabase.getDatabase(this)
    }

    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleRecurringExpenseWorker()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Budget Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for budget threshold alerts"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun scheduleRecurringExpenseWorker() {
        val workRequest = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(24, TimeUnit.HOURS).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RECURRING_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        const val CHANNEL_ID = "budget_alerts"
        const val RECURRING_WORK_NAME = "recurring_expense_check"
    }
}
package com.example.mob_dev_portfolio.util

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mob_dev_portfolio.BudgetTrackingApp
import com.example.mob_dev_portfolio.MainActivity
import com.example.mob_dev_portfolio.R

class NotificationHelper(private val context: Context) {

    fun showBudgetWarning70(remaining: String) {
        showNotification(
            id = NOTIFICATION_ID_70,
            title = context.getString(R.string.notification_70_title),
            message = context.getString(R.string.notification_70_message, remaining)
        )
    }

    fun showBudgetWarning90(remaining: String) {
        showNotification(
            id = NOTIFICATION_ID_90,
            title = context.getString(R.string.notification_90_title),
            message = context.getString(R.string.notification_90_message, remaining)
        )
    }

    fun showBudgetReached(budgetAmount: String) {
        showNotification(
            id = NOTIFICATION_ID_100,
            title = context.getString(R.string.notification_100_title),
            message = context.getString(R.string.notification_100_message, budgetAmount)
        )
    }

    fun showOverBudget(overAmount: String) {
        showNotification(
            id = NOTIFICATION_ID_OVER,
            title = context.getString(R.string.notification_over_title),
            message = context.getString(R.string.notification_over_message, overAmount)
        )
    }

    private fun showNotification(id: Int, title: String, message: String) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, BudgetTrackingApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.analytics_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }

    companion object {
        const val NOTIFICATION_ID_70 = 1001
        const val NOTIFICATION_ID_90 = 1002
        const val NOTIFICATION_ID_100 = 1003
        const val NOTIFICATION_ID_OVER = 1004
    }
}
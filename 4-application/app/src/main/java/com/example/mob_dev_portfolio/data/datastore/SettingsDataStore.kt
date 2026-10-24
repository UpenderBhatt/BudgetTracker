package com.example.mob_dev_portfolio.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object{
        val ALERTS_ENABLED = booleanPreferencesKey("alerts_enabled")
        val ALERT_70_SHOWN = booleanPreferencesKey("alert_70_shown")
        val ALERT_90_SHOWN = booleanPreferencesKey("alert_90_shown")
        val ALERT_100_SHOWN = booleanPreferencesKey("alert_100_shown")
        val ALERT_OVER_SHOWN = booleanPreferencesKey("alert_over_shown")
        val AUTO_RENEW_BUDGET = booleanPreferencesKey("auto_renew_budget")
    }

    val alertsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ALERTS_ENABLED] ?: true
        }

    val autoRenewBudget: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_RENEW_BUDGET] ?: false
        }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ALERTS_ENABLED] = enabled
        }
    }

    suspend fun setAutoRenewBudget(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_RENEW_BUDGET] = enabled
        }
    }

    suspend fun resetAlertFlags() {
        context.dataStore.edit { preferences ->
            preferences[ALERT_70_SHOWN] = false
            preferences[ALERT_90_SHOWN] = false
            preferences[ALERT_100_SHOWN] = false
            preferences[ALERT_OVER_SHOWN] = false
        }
    }

    suspend fun markAlertShown(key: Preferences.Key<Boolean>) {
        context.dataStore.edit { preferences ->
            preferences[key] = true
        }
    }

    fun isAlertShown(key: Preferences.Key<Boolean>): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: false
        }
    }
}
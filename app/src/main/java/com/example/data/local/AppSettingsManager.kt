package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppThemeMode
import com.example.data.model.SortDirection
import com.example.data.model.SortField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "bachat_settings")

data class AppSettings(
  val themeMode: AppThemeMode = AppThemeMode.LIGHT,
  val secretLockEnabled: Boolean = false,
  val pinHash: String = "",
  val useBiometric: Boolean = false,
  val goalAlertsEnabled: Boolean = true,
  val showGoalsOnDashboard: Boolean = true,
  val lastSortField: SortField = SortField.DATE,
  val lastSortDirection: SortDirection = SortDirection.DESC
)

class AppSettingsManager(private val context: Context) {
  private object Keys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val SECRET_LOCK_ENABLED = booleanPreferencesKey("secret_lock_enabled")
    val PIN_HASH = stringPreferencesKey("pin_hash")
    val USE_BIOMETRIC = booleanPreferencesKey("use_biometric")
    val GOAL_ALERTS = booleanPreferencesKey("goal_alerts")
    val SHOW_GOALS_ON_DASHBOARD = booleanPreferencesKey("show_goals_on_dashboard")
    val SORT_FIELD = stringPreferencesKey("sort_field")
    val SORT_DIRECTION = stringPreferencesKey("sort_direction")
  }

  val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
    val themeModeStr = prefs[Keys.THEME_MODE] ?: AppThemeMode.LIGHT.name
    val themeMode = try { AppThemeMode.valueOf(themeModeStr) } catch (_: Exception) { AppThemeMode.LIGHT }
    
    val sortFieldStr = prefs[Keys.SORT_FIELD] ?: SortField.DATE.name
    val sortField = try { SortField.valueOf(sortFieldStr) } catch (_: Exception) { SortField.DATE }
    val sortDirStr = prefs[Keys.SORT_DIRECTION] ?: SortDirection.DESC.name
    val sortDir = try { SortDirection.valueOf(sortDirStr) } catch (_: Exception) { SortDirection.DESC }

    AppSettings(
      themeMode = themeMode,
      secretLockEnabled = prefs[Keys.SECRET_LOCK_ENABLED] ?: false,
      pinHash = prefs[Keys.PIN_HASH] ?: "",
      useBiometric = prefs[Keys.USE_BIOMETRIC] ?: false,
      goalAlertsEnabled = prefs[Keys.GOAL_ALERTS] ?: true,
      showGoalsOnDashboard = prefs[Keys.SHOW_GOALS_ON_DASHBOARD] ?: true,
      lastSortField = sortField,
      lastSortDirection = sortDir
    )
  }

  suspend fun setThemeMode(mode: AppThemeMode) {
    context.dataStore.edit { prefs ->
      prefs[Keys.THEME_MODE] = mode.name
    }
  }

  suspend fun setSecretLockEnabled(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[Keys.SECRET_LOCK_ENABLED] = enabled
    }
  }

  suspend fun setPinHash(pin: String) {
    context.dataStore.edit { prefs ->
      prefs[Keys.PIN_HASH] = pin
    }
  }

  suspend fun setUseBiometric(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[Keys.USE_BIOMETRIC] = enabled
    }
  }

  suspend fun setGoalAlertsEnabled(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[Keys.GOAL_ALERTS] = enabled
    }
  }
  
  suspend fun setShowGoalsOnDashboard(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[Keys.SHOW_GOALS_ON_DASHBOARD] = enabled
    }
  }

  suspend fun setSort(field: SortField, direction: SortDirection) {
    context.dataStore.edit { prefs ->
      prefs[Keys.SORT_FIELD] = field.name
      prefs[Keys.SORT_DIRECTION] = direction.name
    }
  }
}

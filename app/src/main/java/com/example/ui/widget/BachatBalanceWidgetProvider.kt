package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.BachatDatabase
import com.example.data.model.TransactionType
import com.example.data.model.formatRupee
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

class BachatBalanceWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (appWidgetId in appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId)
    }
  }

  companion object {
    fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
      val scope = CoroutineScope(Dispatchers.IO)
      scope.launch {
        val db = BachatDatabase.getDatabase(context, this)
        val allTx = db.transactionDao().getAllTransactions().firstOrNull() ?: emptyList()

        val validTx = allTx.filter { !it.isSecret }

        val startOfToday = Calendar.getInstance().apply {
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfWeek = Calendar.getInstance().apply {
          set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfMonth = Calendar.getInstance().apply {
          set(Calendar.DAY_OF_MONTH, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val totalIncome = validTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = validTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalBalance = totalIncome - totalExpense

        val todayExpense = validTx.filter {
          it.type == TransactionType.EXPENSE && it.timestamp >= startOfToday
        }.sumOf { it.amount }

        val weekExpense = validTx.filter {
          it.type == TransactionType.EXPENSE && it.timestamp >= startOfWeek
        }.sumOf { it.amount }

        val monthExpense = validTx.filter {
          it.type == TransactionType.EXPENSE && it.timestamp >= startOfMonth
        }.sumOf { it.amount }

        val views = RemoteViews(context.packageName, R.layout.widget_balance_layout).apply {
          setTextViewText(R.id.widget_balance_amount, formatRupee(totalBalance))
          setTextViewText(R.id.widget_stat_today_amount, formatRupee(todayExpense))
          setTextViewText(R.id.widget_stat_week_amount, formatRupee(weekExpense))
          setTextViewText(R.id.widget_stat_month_amount, formatRupee(monthExpense))

          // Click on whole widget to open app
          val launchIntent = Intent(context, MainActivity::class.java)
          val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId + 20000,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          setOnClickPendingIntent(R.id.widget_balance_root, pendingIntent)

          // Click on '+' button to quick add transaction
          val addIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.ACTION_QUICK_ADD"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
          }
          val addPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId + 30000,
            addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          setOnClickPendingIntent(R.id.widget_balance_btn_add, addPendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
      }
    }

    fun notifyDataChanged(context: Context) {
      val appWidgetManager = AppWidgetManager.getInstance(context)
      val thisWidget = ComponentName(context, BachatBalanceWidgetProvider::class.java)
      val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
      for (appWidgetId in allWidgetIds) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
      }
    }
  }
}

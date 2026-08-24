package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.BachatDatabase
import com.example.data.model.AccountTag
import com.example.data.model.CategoryItem
import com.example.data.model.DEFAULT_CATEGORIES
import com.example.data.model.TimePeriod
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.VisibilityFilter
import com.example.data.model.formatRupee
import com.example.ui.FilterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

data class WidgetDonutSegment(
  val category: String,
  val amount: Double,
  val percentage: Double,
  val color: Int
)

class BachatDonutWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (appWidgetId in appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId)
    }
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    for (appWidgetId in appWidgetIds) {
      BachatDonutWidgetConfigureActivity.deleteWidgetConfig(context, appWidgetId)
    }
  }

  companion object {
    private val DEFAULT_PALETTE_COLORS = intArrayOf(
      0xFF4F46E5.toInt(), // Indigo
      0xFFEF4444.toInt(), // Danger/Red
      0xFF10B981.toInt(), // Success/Green
      0xFFF59E0B.toInt(), // Warning/Amber
      0xFF8B5CF6.toInt(), // Purple
      0xFF06B6D4.toInt(), // Cyan
      0xFFEC4899.toInt(), // Pink
      0xFF3B82F6.toInt()  // Blue
    )

    fun filterTransactions(allTx: List<Transaction>, filter: FilterState): List<Transaction> {
      // 1. Filter by Visibility
      var baseTx = when (filter.visibility) {
        VisibilityFilter.STANDARD -> allTx.filter { !it.isSecret }
        VisibilityFilter.SECRET -> allTx.filter { it.isSecret }
      }

      // 2. Filter by Account Tag
      if (filter.accountTag != AccountTag.ALL) {
        baseTx = baseTx.filter { it.accountTag.equals(filter.accountTag.displayName, ignoreCase = true) }
      }

      // 3. Filter by Category
      if (filter.category != "All" && filter.category.isNotBlank()) {
        baseTx = baseTx.filter { it.category.equals(filter.category, ignoreCase = true) }
      }

      // 4. Filter by Transaction Type
      if (filter.transactionType == "EXPENSE") {
        baseTx = baseTx.filter { it.type == TransactionType.EXPENSE }
      } else if (filter.transactionType == "INCOME") {
        baseTx = baseTx.filter { it.type == TransactionType.INCOME }
      }

      // 5. Time Period Bounds
      val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }.timeInMillis

      val endOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
      }.timeInMillis

      val startOfWeek = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }.timeInMillis

      val endOfWeek = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        add(Calendar.DAY_OF_WEEK, 6)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
      }.timeInMillis

      val startOfMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }.timeInMillis

      val endOfMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
      }.timeInMillis

      return when (filter.timePeriod) {
        TimePeriod.TODAY -> baseTx.filter { it.timestamp in startOfToday..endOfToday }
        TimePeriod.WEEK -> baseTx.filter { it.timestamp in startOfWeek..endOfWeek }
        TimePeriod.MONTH -> baseTx.filter { it.timestamp in startOfMonth..endOfMonth }
        TimePeriod.ALL -> baseTx
        TimePeriod.CUSTOM -> {
          val start = if (filter.customStartDate > 0) filter.customStartDate else 0L
          val end = if (filter.customEndDate > 0) filter.customEndDate else Long.MAX_VALUE
          baseTx.filter { it.timestamp in start..end }
        }
      }
    }

    /**
     * Renders a high-resolution anti-aliased Donut Chart Bitmap
     * with category arcs and centered formatted summary amount.
     */
    fun renderDonutBitmap(
      totalAmount: Double,
      segments: List<WidgetDonutSegment>,
      subLabel: String = "SPENT",
      sizePx: Int = 300
    ): Bitmap {
      val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)

      val cx = sizePx / 2f
      val cy = sizePx / 2f
      val strokeWidth = sizePx * 0.125f // ~37.5px stroke width
      val radius = (sizePx - strokeWidth) / 2f - 6f
      val arcRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

      // Background subtle track ring
      val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
        color = 0xFF1E293B.toInt()
      }
      canvas.drawCircle(cx, cy, radius, trackPaint)

      // Draw colored segments
      if (segments.isNotEmpty() && totalAmount > 0) {
        var startAngle = -90f
        for (seg in segments) {
          val rawSweep = (seg.percentage.toFloat() / 100f) * 360f
          if (rawSweep > 0.5f) {
            val sweep = if (segments.size > 1) (rawSweep - 2.5f).coerceAtLeast(1.5f) else rawSweep
            val segmentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
              style = Paint.Style.STROKE
              this.strokeWidth = strokeWidth
              color = seg.color
              strokeCap = Paint.Cap.ROUND
            }
            canvas.drawArc(arcRect, startAngle, sweep, false, segmentPaint)
            startAngle += rawSweep
          }
        }
      }

      // Draw Center Total Amount Text
      val amountStr = formatRupee(totalAmount)
      val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = if (amountStr.length > 9) sizePx * 0.13f else if (amountStr.length > 6) sizePx * 0.155f else sizePx * 0.18f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
      }

      val textBounds = Rect()
      amountPaint.getTextBounds(amountStr, 0, amountStr.length, textBounds)
      val textY = cy - textBounds.exactCenterY() - (sizePx * 0.045f)
      canvas.drawText(amountStr, cx, textY, amountPaint)

      // Draw Center Subtitle Text
      val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF94A3B8.toInt()
        textSize = sizePx * 0.08f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.06f
      }
      canvas.drawText(subLabel.uppercase(), cx, textY + (sizePx * 0.145f), subPaint)

      return bitmap
    }

    fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
      val scope = CoroutineScope(Dispatchers.IO)
      scope.launch {
        val filterState = BachatDonutWidgetConfigureActivity.loadWidgetFilterState(context, appWidgetId)

        val db = BachatDatabase.getDatabase(context, this)
        val allTx = db.transactionDao().getAllTransactions().firstOrNull() ?: emptyList()
        val allCategories = db.categoryDao().getAllCategories().firstOrNull() ?: DEFAULT_CATEGORIES

        val categoryColorMap = mutableMapOf<String, Int>()
        allCategories.forEach {
          categoryColorMap[it.name.lowercase()] = it.colorHex.toInt()
        }

        val filteredTx = filterTransactions(allTx, filterState)
        val expenseTx = filteredTx.filter { it.type == TransactionType.EXPENSE }
        val totalSpent = if (filterState.transactionType == "INCOME") {
          filteredTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        } else {
          expenseTx.sumOf { it.amount }
        }

        val periodTitle = when (filterState.timePeriod) {
          TimePeriod.TODAY -> "Today"
          TimePeriod.WEEK -> "This Week"
          TimePeriod.MONTH -> "This Month"
          TimePeriod.ALL -> "All Time"
          TimePeriod.CUSTOM -> "Custom"
        }

        val tagList = buildList {
          if (filterState.category != "All" && filterState.category.isNotBlank()) add(filterState.category)
          if (filterState.accountTag != AccountTag.ALL) add(filterState.accountTag.displayName)
          if (filterState.transactionType != "ALL") add(filterState.transactionType.lowercase().replaceFirstChar { it.uppercase() })
          if (filterState.visibility != VisibilityFilter.STANDARD) add(filterState.visibility.displayName)
        }

        val headerLabel = if (tagList.isNotEmpty()) {
          "$periodTitle • ${tagList.joinToString(" • ")}"
        } else {
          periodTitle
        }

        // Build Donut Segments
        val groupedCategories = if (filterState.transactionType == "INCOME") {
          filteredTx.filter { it.type == TransactionType.INCOME }.groupBy { it.category }
        } else {
          expenseTx.groupBy { it.category }
        }

        val sortedEntries = groupedCategories.entries.sortedByDescending { it.value.sumOf { tx -> tx.amount } }
        val totalForSegments = if (totalSpent > 0) totalSpent else 1.0

        val segments = sortedEntries.mapIndexed { idx, entry ->
          val catAmount = entry.value.sumOf { it.amount }
          val pct = (catAmount / totalForSegments) * 100.0
          val customColor = categoryColorMap[entry.key.lowercase()]
          val color = customColor ?: DEFAULT_PALETTE_COLORS[idx % DEFAULT_PALETTE_COLORS.size]
          WidgetDonutSegment(
            category = entry.key,
            amount = catAmount,
            percentage = pct,
            color = color
          )
        }

        val donutBitmap = renderDonutBitmap(
          totalAmount = totalSpent,
          segments = segments,
          subLabel = if (filterState.transactionType == "INCOME") "INCOME" else "SPENT",
          sizePx = 320
        )

        val views = RemoteViews(context.packageName, R.layout.widget_donut_layout).apply {
          setTextViewText(R.id.widget_period_label, headerLabel)
          setImageViewBitmap(R.id.widget_donut_image, donutBitmap)

          // Populate category rows
          val rowIds = intArrayOf(R.id.widget_cat_row_1, R.id.widget_cat_row_2, R.id.widget_cat_row_3, R.id.widget_cat_row_4)
          val dotIds = intArrayOf(R.id.widget_cat_dot_1, R.id.widget_cat_dot_2, R.id.widget_cat_dot_3, R.id.widget_cat_dot_4)
          val nameIds = intArrayOf(R.id.widget_cat_name_1, R.id.widget_cat_name_2, R.id.widget_cat_name_3, R.id.widget_cat_name_4)
          val amountIds = intArrayOf(R.id.widget_cat_amount_1, R.id.widget_cat_amount_2, R.id.widget_cat_amount_3, R.id.widget_cat_amount_4)

          if (segments.isEmpty() || totalSpent <= 0) {
            setViewVisibility(R.id.widget_empty_text, View.VISIBLE)
            for (rId in rowIds) {
              setViewVisibility(rId, View.GONE)
            }
          } else {
            setViewVisibility(R.id.widget_empty_text, View.GONE)
            for (i in 0 until 4) {
              if (i < segments.size) {
                val seg = segments[i]
                setViewVisibility(rowIds[i], View.VISIBLE)
                setTextColor(dotIds[i], seg.color)
                setTextViewText(nameIds[i], seg.category)
                val pctStr = "${seg.percentage.toInt()}%"
                val amtStr = formatRupee(seg.amount)
                setTextViewText(amountIds[i], "$pctStr • $amtStr")
              } else {
                setViewVisibility(rowIds[i], View.GONE)
              }
            }
          }

          // Launch App on root click
          val launchIntent = Intent(context, MainActivity::class.java)
          val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          setOnClickPendingIntent(R.id.widget_root, pendingIntent)

          // Quick Add Transaction shortcut on '+' button
          val addIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.ACTION_QUICK_ADD"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
          }
          val addPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId + 10000,
            addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          setOnClickPendingIntent(R.id.widget_btn_add, addPendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
      }
    }

    fun notifyDataChanged(context: Context) {
      val appWidgetManager = AppWidgetManager.getInstance(context)
      val thisWidget = ComponentName(context, BachatDonutWidgetProvider::class.java)
      val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
      for (appWidgetId in allWidgetIds) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
      }
    }
  }
}

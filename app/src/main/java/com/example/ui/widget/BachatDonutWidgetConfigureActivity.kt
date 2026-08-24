package com.example.ui.widget

import android.app.Activity
import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.components.BachatFilterChip
import com.example.ui.components.DonutChart
import com.example.ui.components.DonutSegment
import com.example.ui.theme.BachatAccentIndigo
import com.example.ui.theme.BachatAccentIndigoTint
import com.example.ui.theme.BachatBackground
import com.example.ui.theme.BachatDanger
import com.example.ui.theme.BachatDivider
import com.example.ui.theme.BachatInk
import com.example.ui.theme.BachatOnColor
import com.example.ui.theme.BachatSuccess
import com.example.ui.theme.BachatSurface
import com.example.ui.theme.BachatTextPrimary
import com.example.ui.theme.BachatTextSecondary
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BachatDonutWidgetConfigureActivity : ComponentActivity() {

  private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setResult(RESULT_CANCELED)

    val extras = intent.extras
    if (extras != null) {
      appWidgetId = extras.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
      )
    }

    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }

    val initialFilterState = loadWidgetFilterState(this, appWidgetId)

    setContent {
      MyApplicationTheme {
        WidgetConfigureScreen(
          appWidgetId = appWidgetId,
          initialFilterState = initialFilterState,
          onSaveFilterState = { filterState ->
            saveWidgetFilterState(this, appWidgetId, filterState)
            val appWidgetManager = AppWidgetManager.getInstance(this)
            BachatDonutWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)

            val resultValue = Intent().apply {
              putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)
            finish()
          },
          onCancel = {
            finish()
          }
        )
      }
    }
  }

  companion object {
    private const val PREFS_NAME = "com.example.ui.widget.BachatWidgetPrefs"
    private const val KEY_PERIOD = "filter_period_"
    private const val KEY_ACCOUNT = "filter_account_"
    private const val KEY_TYPE = "filter_type_"
    private const val KEY_CAT = "filter_category_"
    private const val KEY_VISIBILITY = "filter_visibility_"
    private const val KEY_START_DATE = "filter_start_date_"
    private const val KEY_END_DATE = "filter_end_date_"

    fun saveWidgetFilterState(context: Context, appWidgetId: Int, filterState: FilterState) {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
      prefs.putString(KEY_PERIOD + appWidgetId, filterState.timePeriod.name)
      prefs.putString(KEY_ACCOUNT + appWidgetId, filterState.accountTag.name)
      prefs.putString(KEY_TYPE + appWidgetId, filterState.transactionType)
      prefs.putString(KEY_CAT + appWidgetId, filterState.category)
      prefs.putString(KEY_VISIBILITY + appWidgetId, filterState.visibility.name)
      prefs.putLong(KEY_START_DATE + appWidgetId, filterState.customStartDate)
      prefs.putLong(KEY_END_DATE + appWidgetId, filterState.customEndDate)
      prefs.apply()
    }

    fun loadWidgetFilterState(context: Context, appWidgetId: Int): FilterState {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      val periodStr = prefs.getString(KEY_PERIOD + appWidgetId, "MONTH") ?: "MONTH"
      val accountStr = prefs.getString(KEY_ACCOUNT + appWidgetId, "ALL") ?: "ALL"
      val typeStr = prefs.getString(KEY_TYPE + appWidgetId, "EXPENSE") ?: "EXPENSE"
      val catStr = prefs.getString(KEY_CAT + appWidgetId, "All") ?: "All"
      val visStr = prefs.getString(KEY_VISIBILITY + appWidgetId, "STANDARD") ?: "STANDARD"
      val startLong = prefs.getLong(KEY_START_DATE + appWidgetId, 0L)
      val endLong = prefs.getLong(KEY_END_DATE + appWidgetId, System.currentTimeMillis())

      return FilterState(
        timePeriod = try { TimePeriod.valueOf(periodStr) } catch (e: Exception) { TimePeriod.MONTH },
        accountTag = try { AccountTag.valueOf(accountStr) } catch (e: Exception) { AccountTag.ALL },
        transactionType = typeStr,
        category = catStr,
        visibility = try { VisibilityFilter.valueOf(visStr) } catch (e: Exception) { VisibilityFilter.STANDARD },
        customStartDate = startLong,
        customEndDate = endLong
      )
    }

    fun deleteWidgetConfig(context: Context, appWidgetId: Int) {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
      prefs.remove(KEY_PERIOD + appWidgetId)
      prefs.remove(KEY_ACCOUNT + appWidgetId)
      prefs.remove(KEY_TYPE + appWidgetId)
      prefs.remove(KEY_CAT + appWidgetId)
      prefs.remove(KEY_VISIBILITY + appWidgetId)
      prefs.remove(KEY_START_DATE + appWidgetId)
      prefs.remove(KEY_END_DATE + appWidgetId)
      prefs.apply()
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WidgetConfigureScreen(
  appWidgetId: Int,
  initialFilterState: FilterState,
  onSaveFilterState: (FilterState) -> Unit,
  onCancel: () -> Unit
) {
  var filterState by remember { mutableStateOf(initialFilterState) }
  val availableCategories = remember { mutableStateListOf<String>() }
  val allTransactions = remember { mutableStateListOf<Transaction>() }
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

  var previewAmount by remember { mutableDoubleStateOf(0.0) }
  var previewTopCategory by remember { mutableStateOf("") }
  var previewCount by remember { mutableIntStateOf(0) }

  val previewSegments = remember { mutableStateListOf<DonutSegment>() }
  val categoryColors = remember { mutableMapOf<String, Long>() }
  val defaultPaletteHex = remember {
    listOf(
      0xFF4F46E5,
      0xFFEF4444,
      0xFF10B981,
      0xFFF59E0B,
      0xFF8B5CF6,
      0xFF06B6D4,
      0xFFEC4899,
      0xFF3B82F6
    )
  }

  // Load Categories and Transactions from DB for exact real-time preview
  LaunchedEffect(Unit) {
    coroutineScope.launch(Dispatchers.IO) {
      val db = BachatDatabase.getDatabase(context, this)
      val categoriesFromDb = db.categoryDao().getAllCategories().firstOrNull() ?: emptyList()
      val txFromDb = db.transactionDao().getAllTransactions().firstOrNull() ?: emptyList()

      withContext(Dispatchers.Main) {
        availableCategories.clear()
        availableCategories.add("All")
        categoryColors.clear()
        if (categoriesFromDb.isNotEmpty()) {
          categoriesFromDb.forEach {
            if (!availableCategories.contains(it.name)) availableCategories.add(it.name)
            categoryColors[it.name.lowercase()] = it.colorHex
          }
        } else {
          DEFAULT_CATEGORIES.forEach {
            if (!availableCategories.contains(it.name)) availableCategories.add(it.name)
            categoryColors[it.name.lowercase()] = it.colorHex
          }
        }
        allTransactions.clear()
        allTransactions.addAll(txFromDb)
      }
    }
  }

  // Recalculate Live Preview whenever filter changes
  LaunchedEffect(filterState, allTransactions.size, categoryColors.size) {
    val filtered = BachatDonutWidgetProvider.filterTransactions(allTransactions, filterState)
    val expenseTx = filtered.filter { it.type == TransactionType.EXPENSE }
    val total = if (filterState.transactionType == "INCOME") {
      filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    } else {
      expenseTx.sumOf { it.amount }
    }

    previewCount = filtered.size
    previewAmount = total

    val grouped = if (filterState.transactionType == "INCOME") {
      filtered.filter { it.type == TransactionType.INCOME }.groupBy { it.category }
    } else {
      expenseTx.groupBy { it.category }
    }

    val sortedEntries = grouped.entries.sortedByDescending { it.value.sumOf { tx -> tx.amount } }
    val totalForPct = if (total > 0) total else 1.0

    previewSegments.clear()
    sortedEntries.forEachIndexed { idx, entry ->
      val amt = entry.value.sumOf { it.amount }
      val pct = (amt / totalForPct) * 100.0
      val hex = categoryColors[entry.key.lowercase()] ?: defaultPaletteHex[idx % defaultPaletteHex.size]
      val color = Color(hex)
      previewSegments.add(
        DonutSegment(
          category = entry.key,
          amount = amt,
          percentage = pct,
          color = color,
          tintColor = color.copy(alpha = 0.2f)
        )
      )
    }

    val topCat = sortedEntries.firstOrNull()
    previewTopCategory = if (filterState.category != "All" && filterState.category.isNotBlank()) {
      "Filter: ${filterState.category}"
    } else if (topCat != null) {
      "Top: ${topCat.key} (${formatRupee(topCat.value.sumOf { it.amount })})"
    } else {
      "No transactions"
    }
  }

  Scaffold(
    containerColor = BachatBackground
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 20.dp, vertical = 16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(BachatAccentIndigoTint),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Widget Setup",
              tint = BachatAccentIndigo,
              modifier = Modifier.size(22.dp)
            )
          }
          Column {
            Text(
              text = "Widget Filter Setup",
              fontSize = 19.sp,
              fontWeight = FontWeight.Bold,
              color = BachatTextPrimary
            )
            Text(
              text = "Use the exact same app filters for your widget",
              fontSize = 12.sp,
              color = BachatTextSecondary
            )
          }
        }

        // Reset Filter button
        IconButton(
          onClick = { filterState = FilterState() },
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(BachatSurface)
            .testTag("btn_reset_widget_filter")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reset",
            tint = BachatTextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Live Widget Preview Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Spending Breakdown",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
              )
              val subLabel = buildList {
                add(filterState.timePeriod.displayName)
                if (filterState.category != "All") add(filterState.category)
                if (filterState.accountTag != AccountTag.ALL) add(filterState.accountTag.displayName)
                if (filterState.transactionType != "ALL") add(filterState.transactionType.lowercase().replaceFirstChar { it.uppercase() })
                if (filterState.visibility != VisibilityFilter.STANDARD) add(filterState.visibility.displayName)
              }.joinToString(" • ")

              Text(
                text = subLabel,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1
              )
            }

            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(BachatAccentIndigo),
              contentAlignment = Alignment.Center
            ) {
              Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Donut Chart + Category Breakdown
          if (previewSegments.isEmpty() || previewAmount <= 0) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(90.dp)
                  .border(12.dp, Color(0xFF1E293B), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("₹0", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text("SPENT", color = Color(0xFF94A3B8), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "No expenses in this period",
                  color = Color(0xFF94A3B8),
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "Transactions matching your filter will appear here.",
                  color = Color(0xFF64748B),
                  fontSize = 11.sp
                )
              }
            }
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Interactive Donut Chart Canvas
              DonutChart(
                totalAmount = previewAmount,
                segments = previewSegments,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }

      // 1. TIME PERIOD (Same as App Filter)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "TIME PERIOD",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          color = BachatTextSecondary
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          TimePeriod.values().forEach { period ->
            BachatFilterChip(
              label = period.displayName,
              selected = filterState.timePeriod == period,
              onClick = { filterState = filterState.copy(timePeriod = period) }
            )
          }
        }

        // Custom Date Pickers if TimePeriod.CUSTOM
        if (filterState.timePeriod == TimePeriod.CUSTOM) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Start Date
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                .clickable {
                  val cal = Calendar.getInstance().apply {
                    timeInMillis = filterState.customStartDate.coerceAtLeast(1L)
                  }
                  DatePickerDialog(
                    context,
                    { _, y, m, d ->
                      val chosen = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }.timeInMillis
                      filterState = filterState.copy(customStartDate = chosen)
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                  ).show()
                }
                .padding(10.dp)
            ) {
              Column {
                Text("From", fontSize = 11.sp, color = BachatTextSecondary)
                Text(
                  text = if (filterState.customStartDate > 0) dateFmt.format(Date(filterState.customStartDate)) else "Select Date",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = BachatTextPrimary
                )
              }
            }

            // End Date
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                .clickable {
                  val cal = Calendar.getInstance().apply { timeInMillis = filterState.customEndDate }
                  DatePickerDialog(
                    context,
                    { _, y, m, d ->
                      val chosen = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }.timeInMillis
                      filterState = filterState.copy(customEndDate = chosen)
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                  ).show()
                }
                .padding(10.dp)
            ) {
              Column {
                Text("To", fontSize = 11.sp, color = BachatTextSecondary)
                Text(
                  text = dateFmt.format(Date(filterState.customEndDate)),
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = BachatTextPrimary
                )
              }
            }
          }
        }
      }

      HorizontalDivider(color = BachatDivider)

      // 2. TRANSACTION TYPE (Same as App Filter)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "TRANSACTION TYPE",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          color = BachatTextSecondary
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("ALL", "EXPENSE", "INCOME").forEach { type ->
            BachatFilterChip(
              label = type.lowercase().replaceFirstChar { it.uppercase() },
              selected = filterState.transactionType == type,
              onClick = { filterState = filterState.copy(transactionType = type) }
            )
          }
        }
      }

      HorizontalDivider(color = BachatDivider)

      // 3. ACCOUNT TAG (Same as App Filter)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "ACCOUNT",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          color = BachatTextSecondary
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          AccountTag.values().forEach { tag ->
            BachatFilterChip(
              label = tag.displayName,
              selected = filterState.accountTag == tag,
              onClick = { filterState = filterState.copy(accountTag = tag) }
            )
          }
        }
      }

      HorizontalDivider(color = BachatDivider)

      // 4. CATEGORY (Same as App Filter)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "CATEGORY",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          color = BachatTextSecondary
        )
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          availableCategories.forEach { categoryName ->
            val isSelected = filterState.category.equals(categoryName, ignoreCase = true)
            Surface(
              shape = RoundedCornerShape(999.dp),
              color = if (isSelected) BachatInk else BachatSurface,
              modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable { filterState = filterState.copy(category = categoryName) }
                .testTag("filter_cat_$categoryName")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
                Text(
                  text = categoryName,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else BachatTextSecondary
                )
              }
            }
          }
        }
      }

      HorizontalDivider(color = BachatDivider)

      // 5. VISIBILITY (Same as App Filter)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "VISIBILITY",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          color = BachatTextSecondary
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          BachatFilterChip(
            label = "Standard",
            selected = filterState.visibility == VisibilityFilter.STANDARD,
            onClick = { filterState = filterState.copy(visibility = VisibilityFilter.STANDARD) }
          )
          BachatFilterChip(
            label = "Secret",
            selected = filterState.visibility == VisibilityFilter.SECRET,
            icon = Icons.Default.Lock,
            onClick = { filterState = filterState.copy(visibility = VisibilityFilter.SECRET) }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Buttons
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = { onSaveFilterState(filterState) },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("btn_save_widget_config"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BachatInk)
        ) {
          Text(
            text = "Apply Filters & Add Widget",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = BachatOnColor
          )
        }

        OutlinedButton(
          onClick = onCancel,
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("btn_cancel_widget_config"),
          shape = RoundedCornerShape(14.dp)
        ) {
          Text(
            text = "Cancel",
            fontSize = 14.sp,
            color = BachatTextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

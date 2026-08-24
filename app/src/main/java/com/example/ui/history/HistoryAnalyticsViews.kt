package com.example.ui.history

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.components.AppCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import com.example.data.model.AccountTag
import com.example.data.model.ExpenseCategory
import com.example.data.model.IncomeCategory
import com.example.data.model.SortDirection
import com.example.data.model.SortField
import com.example.data.model.TimePeriod
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.VisibilityFilter
import com.example.data.model.formatRupee
import com.example.ui.AnalyticsUiState
import com.example.ui.FilterState
import com.example.ui.components.BachatFilterChip
import com.example.ui.components.BadgePill
import com.example.ui.components.DonutChart
import com.example.ui.components.EmptyState
import com.example.ui.components.RowActionPopup
import com.example.ui.components.SegmentedToggle
import com.example.ui.components.SpendBarItem
import com.example.ui.components.SpendingComparisonCard
import com.example.ui.components.ThinProgressBar
import com.example.ui.components.TransactionRow
import com.example.ui.components.WeeklySpendBarChart
import com.example.ui.components.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.io.File
import com.example.ui.theme.BachatAccentIndigo
import com.example.ui.theme.BachatAccentIndigoTint
import com.example.ui.theme.BachatDanger
import com.example.ui.theme.BachatDangerTint
import com.example.ui.theme.BachatDivider
import com.example.ui.theme.BachatInk
import com.example.ui.theme.BachatOnColor
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import com.example.ui.theme.BachatSuccess
import com.example.ui.theme.BachatSuccessTint
import com.example.ui.theme.BachatSurface
import com.example.ui.theme.BachatTextPrimary
import com.example.ui.theme.BachatTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryAnalyticsScreen(
  selectedTab: Int,
  onTabSelect: (Int) -> Unit,
  analyticsState: AnalyticsUiState,
  filteredTransactions: List<Transaction>,
  filterState: FilterState,
  isSecretLocked: Boolean,
  onUpdateFilter: (FilterState.() -> FilterState) -> Unit,
  onResetFilter: () -> Unit,
  onExportPdf: () -> File,
  onBackClick: () -> Unit,
  onEditTransaction: (Transaction) -> Unit,
  onDeleteTransaction: (Transaction) -> Unit,
  onRequestSecretUnlock: (() -> Unit) -> Unit,
  modifier: Modifier = Modifier
) {
  var showFilterSheet by remember { mutableStateOf(false) }
  var selectedTxForAction by remember { mutableStateOf<Transaction?>(null) }
  val context = LocalContext.current

  fun handleSharePdf() {
    try {
      val pdfFile = onExportPdf()
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        pdfFile
      )
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(shareIntent, "Share Bachat Statement"))
    } catch (e: Exception) {
      Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  Scaffold(
    topBar = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.White)
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.align(Alignment.CenterStart).testTag("history_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = BachatTextPrimary
          )
        }

        Text(
          text = "History & Analytics",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary,
          modifier = Modifier.align(Alignment.Center)
        )

        // Filter button (Black circle with sliders icon)
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(BachatInk)
            .align(Alignment.CenterEnd)
            .clickable { showFilterSheet = true }
            .testTag("filter_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Filter",
            tint = BachatOnColor,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    },
    containerColor = Color.White,
    modifier = modifier.testTag("history_analytics_screen")
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 20.dp)
    ) {
      Spacer(modifier = Modifier.height(4.dp))

      // Tab selector: Analytics / Activity
      val pagerState = rememberPagerState(initialPage = selectedTab, pageCount = { 2 })
      LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
          pagerState.animateScrollToPage(selectedTab)
        }
      }
      LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
          if (page != selectedTab) {
            onTabSelect(page)
          }
        }
      }

      SegmentedToggle(
        options = listOf("Analytics", "Activity"),
        selectedIndex = selectedTab,
        onSelect = { onTabSelect(it) }
      )

      Spacer(modifier = Modifier.height(14.dp))

      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
      ) { page ->
        if (page == 0) {
          // TAB 1: ANALYTICS (ZERO-SCROLL GLANCEABLE DASHBOARD)
          AnalyticsContent(
            analyticsState = analyticsState,
            filteredTransactions = filteredTransactions,
            filterState = filterState
          )
        } else {
          // TAB 2: ACTIVITY
          ActivityContent(
            transactions = filteredTransactions,
            filterState = filterState,
            onResetFilter = onResetFilter,
            onExportPdf = { handleSharePdf() },
            onTransactionClick = { tx -> selectedTxForAction = tx }
          )
        }
      }
    }
  }

  // RowActionPopup for Transactions
  selectedTxForAction?.let { tx ->
    RowActionPopup(
      onDismiss = { selectedTxForAction = null },
      onEdit = { onEditTransaction(tx) },
      onDelete = { onDeleteTransaction(tx) }
    )
  }

  // Filter Bottom Sheet
  if (showFilterSheet) {
    FilterSortSheet(
      filterState = filterState,
      isSecretLocked = isSecretLocked,
      onDismiss = { showFilterSheet = false },
      onApplyFilter = { updated ->
        onUpdateFilter { updated }
        showFilterSheet = false
      },
      onRequestSecretUnlock = onRequestSecretUnlock
    )
  }
}

@Composable
fun AnalyticsContent(
  analyticsState: AnalyticsUiState,
  filteredTransactions: List<Transaction>,
  filterState: FilterState
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("analytics_content"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. Executive Cash Flow & Financial Health Card
    AppCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("period_summary_card")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
      ) {
        // Top Header: Label + Savings Rate Badge + Comparison Pill
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(BachatAccentIndigoTint),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = BachatAccentIndigo,
                modifier = Modifier.size(13.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "SUMMARY • ${analyticsState.periodLabel.uppercase()}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp,
              color = BachatTextSecondary
            )
          }

          if (analyticsState.comparisonPercentage > 0) {
            val isLower = analyticsState.isLower
            BadgePill(
              text = (if (isLower) "↓ " else "↑ ") + "${analyticsState.comparisonPercentage.toInt()}% vs last",
              backgroundColor = if (isLower) BachatSuccessTint else BachatDangerTint,
              textColor = if (isLower) BachatSuccess else BachatDanger
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Income
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Income",
                tint = BachatSuccess,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Income",
                fontSize = 12.sp,
                color = BachatTextSecondary
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = formatRupee(analyticsState.periodIncome),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = BachatTextPrimary
            )
          }

          // Expense
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Expense",
                tint = BachatDanger,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Expense",
                fontSize = 12.sp,
                color = BachatTextSecondary
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = formatRupee(analyticsState.periodExpense),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = BachatTextPrimary
            )
          }

          // Average
          Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Average",
                tint = BachatTextSecondary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Daily Avg",
                fontSize = 12.sp,
                color = BachatTextSecondary
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = formatRupee(analyticsState.dailyAverage),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = BachatTextPrimary
            )
          }
        }
      }
    }

    // 2. Spending Breakdown Visualizer Card
    AppCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("breakdown_card")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
      ) {
        // Header: Category Share Title & Top Category Badge
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Spending Breakdown",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = BachatTextPrimary
          )

          if (analyticsState.topCategory.isNotBlank()) {
            BadgePill(
              text = "Top: ${analyticsState.topCategory}",
              backgroundColor = BachatSurface,
              textColor = BachatTextPrimary,
              icon = getCategoryIcon(analyticsState.topCategory),
              iconTint = BachatTextPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Spending Breakdown Chart + Category Legend
        DonutChart(
          totalAmount = analyticsState.periodExpense,
          segments = analyticsState.donutSegments,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    // 3. Spending Trend Chart Card (if Week or Month)
    if (filterState.timePeriod == TimePeriod.WEEK || filterState.timePeriod == TimePeriod.MONTH) {
      val trendBars = remember(filteredTransactions, filterState) {
        calculateTrendBarItems(filteredTransactions, filterState)
      }

      AppCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("spending_trend_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
        ) {
          Text(
            text = "Spending Trend",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = BachatTextPrimary
          )

          Spacer(modifier = Modifier.height(12.dp))

          val expenses = filteredTransactions.filter { it.type == TransactionType.EXPENSE }
          if (expenses.isEmpty()) {
            EmptyState(
              icon = Icons.Default.TrendingDown,
              message = "No expenses in this period",
              helperText = "Expenses will appear here as you log them"
            )
          } else {
            WeeklySpendBarChart(
              items = trendBars,
              activeColor = BachatDanger,
              baseColor = BachatDangerTint
            )
          }
        }
      }
    }
  }
}

private fun calculateTrendBarItems(transactions: List<Transaction>, filter: FilterState): List<SpendBarItem> {
  val expenses = transactions.filter { it.type == TransactionType.EXPENSE }

  return when (filter.timePeriod) {
    TimePeriod.TODAY -> emptyList()

    TimePeriod.WEEK -> {
      val weekStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
      val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
      (0..6).map { offset ->
        val dayCal = Calendar.getInstance().apply {
          timeInMillis = weekStart.timeInMillis
          add(Calendar.DAY_OF_YEAR, offset)
        }
        val dStart = (dayCal.clone() as Calendar).apply {
          set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val dEnd = (dayCal.clone() as Calendar).apply {
          set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }
        val sumAmt = expenses.filter { it.timestamp in dStart.timeInMillis..dEnd.timeInMillis }.sumOf { it.amount }
        val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)
        val dow = dayCal.get(Calendar.DAY_OF_WEEK) - 1

        SpendBarItem(
          label = dayLabels.getOrElse(dow) { "D" },
          subLabel = "$dayNum",
          amount = sumAmt,
          isHighlighted = dayCal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) &&
                          dayCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
        )
      }
    }

    TimePeriod.MONTH -> {
      val monthCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
      }
      val maxDays = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
      val weeks = mutableListOf<SpendBarItem>()
      var currentWeekStart = 1
      var weekCount = 1
      while (currentWeekStart <= maxDays) {
        val nextWeekStart = (currentWeekStart + 7).coerceAtMost(maxDays + 1)
        
        var sumAmt = 0.0
        var hasHighlight = false
        for (dayNum in currentWeekStart until nextWeekStart) {
          val dayCal = (monthCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dayNum) }
          val dStart = (dayCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
          }
          val dEnd = (dayCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
          }
          sumAmt += expenses.filter { it.timestamp in dStart.timeInMillis..dEnd.timeInMillis }.sumOf { it.amount }
          if (dayCal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) &&
              dayCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
            hasHighlight = true
          }
        }
        
        val labelStart = currentWeekStart
        val labelEnd = nextWeekStart - 1
        val subLabelStr = if (labelStart == labelEnd) "$labelStart" else "$labelStart-$labelEnd"
        
        weeks.add(
          SpendBarItem(
            label = "W$weekCount",
            subLabel = subLabelStr,
            amount = sumAmt,
            isHighlighted = hasHighlight
          )
        )
        currentWeekStart = nextWeekStart
        weekCount++
      }
      weeks
    }

    TimePeriod.ALL, TimePeriod.CUSTOM -> {
      val startTime = if (filter.timePeriod == TimePeriod.CUSTOM) filter.customStartDate else {
        expenses.minOfOrNull { it.timestamp } ?: Calendar.getInstance().timeInMillis
      }
      val endTime = if (filter.timePeriod == TimePeriod.CUSTOM) filter.customEndDate else {
        System.currentTimeMillis()
      }
      val diffDays = (endTime - startTime) / (1000 * 60 * 60 * 24)

      if (diffDays <= 60) {
        val startCal = Calendar.getInstance().apply {
          timeInMillis = startTime
          set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
          timeInMillis = endTime
          set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }
        val fmt = SimpleDateFormat("MMM dd", Locale.getDefault())
        val items = mutableListOf<SpendBarItem>()
        val curr = startCal.clone() as Calendar
        while (curr.timeInMillis <= endCal.timeInMillis) {
          val dStart = (curr.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
          }
          val dEnd = (curr.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
          }
          val sumAmt = expenses.filter { it.timestamp in dStart.timeInMillis..dEnd.timeInMillis }.sumOf { it.amount }
          items.add(SpendBarItem(label = fmt.format(curr.time), subLabel = "", amount = sumAmt))
          curr.add(Calendar.DAY_OF_YEAR, 1)
        }
        items
      } else {
        val startCal = Calendar.getInstance().apply {
          timeInMillis = startTime
          set(Calendar.DAY_OF_MONTH, 1)
          set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
          timeInMillis = endTime
          set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }
        val fmt = SimpleDateFormat("MMM yy", Locale.getDefault())
        val items = mutableListOf<SpendBarItem>()
        val curr = startCal.clone() as Calendar
        while (curr.timeInMillis <= endCal.timeInMillis) {
          val mStart = (curr.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
          }
          val maxD = curr.getActualMaximum(Calendar.DAY_OF_MONTH)
          val mEnd = (curr.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, maxD)
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
          }
          val sumAmt = expenses.filter { it.timestamp in mStart.timeInMillis..mEnd.timeInMillis }.sumOf { it.amount }
          items.add(SpendBarItem(label = fmt.format(curr.time), subLabel = "", amount = sumAmt))
          curr.add(Calendar.MONTH, 1)
        }
        items
      }
    }
  }
}

@Composable
fun ActivityContent(
  transactions: List<Transaction>,
  filterState: FilterState,
  onResetFilter: () -> Unit,
  onExportPdf: () -> Unit,
  onTransactionClick: (Transaction) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().testTag("activity_content"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Header actions: Export PDF pill + Filter Active summary
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${transactions.size} Transactions",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary
        )

        // Export PDF Pill Button
        Button(
          onClick = onExportPdf,
          shape = RoundedCornerShape(999.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BachatSuccessTint,
            contentColor = BachatSuccess
          ),
          modifier = Modifier.height(38.dp).testTag("export_pdf_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.PictureAsPdf,
              contentDescription = null,
              tint = BachatSuccess,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Export PDF",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    if (transactions.isEmpty()) {
      item {
        EmptyState(
          icon = Icons.Default.ReceiptLong,
          message = "No transactions found",
          helperText = "Try changing or resetting your active filters"
        )
      }
    } else {
      items(transactions) { tx ->
        TransactionRow(
          transaction = tx,
          onClick = { onTransactionClick(tx) }
        )
        HorizontalDivider(color = BachatDivider, thickness = 0.6.dp)
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortSheet(
  filterState: FilterState,
  isSecretLocked: Boolean,
  onDismiss: () -> Unit,
  onApplyFilter: (FilterState) -> Unit,
  onRequestSecretUnlock: (() -> Unit) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current

  var localState by remember { mutableStateOf(filterState) }

  val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor = Color.White,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 10.dp, bottom = 4.dp)
          .size(width = 36.dp, height = 4.dp)
          .clip(RoundedCornerShape(999.dp))
          .background(BachatTextSecondary.copy(alpha = 0.3f))
      )
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .imePadding()
        .padding(horizontal = 24.dp, vertical = 8.dp)
        .testTag("filter_sort_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Filter & Sort",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary
        )

        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = BachatTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 1. TIME PERIOD (3-up + 2-up grid of larger icon-over-label tiles)
      Text(
        text = "TIME PERIOD",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))
      val timePeriods = TimePeriod.values()
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          timePeriods.take(3).forEach { period ->
            TimePeriodTile(
              period = period,
              selected = localState.timePeriod == period,
              onClick = { localState = localState.copy(timePeriod = period) },
              modifier = Modifier.weight(1f)
            )
          }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          timePeriods.drop(3).forEach { period ->
            TimePeriodTile(
              period = period,
              selected = localState.timePeriod == period,
              onClick = { localState = localState.copy(timePeriod = period) },
              modifier = Modifier.weight(1f)
            )
          }
          if (timePeriods.size > 3 && timePeriods.size - 3 < 3) {
            val emptySlots = 3 - (timePeriods.size - 3)
            repeat(emptySlots) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }

      // Custom Date Pickers if TimePeriod.CUSTOM
      if (localState.timePeriod == TimePeriod.CUSTOM) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Start Date
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .background(BachatSurface)
              .clickable {
                val cal = Calendar.getInstance().apply { timeInMillis = localState.customStartDate.coerceAtLeast(1L) }
                DatePickerDialog(context, { _, y, m, d ->
                  val chosen = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }.timeInMillis
                  localState = localState.copy(customStartDate = chosen)
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
              }
              .padding(10.dp)
          ) {
            Column {
              Text("From", fontSize = 11.sp, color = BachatTextSecondary)
              Text(
                text = if (localState.customStartDate > 0) dateFmt.format(Date(localState.customStartDate)) else "Select",
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
              .background(BachatSurface)
              .clickable {
                val cal = Calendar.getInstance().apply { timeInMillis = localState.customEndDate }
                DatePickerDialog(context, { _, y, m, d ->
                  val chosen = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }.timeInMillis
                  localState = localState.copy(customEndDate = chosen)
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
              }
              .padding(10.dp)
          ) {
            Column {
              Text("To", fontSize = 11.sp, color = BachatTextSecondary)
              Text(
                text = dateFmt.format(Date(localState.customEndDate)),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BachatTextPrimary
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. SORT BY
      Text(
        text = "SORT BY",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        BachatFilterChip(
          label = "Date",
          selected = localState.sortField == SortField.DATE,
          onClick = { localState = localState.copy(sortField = SortField.DATE) }
        )
        BachatFilterChip(
          label = "Amount",
          selected = localState.sortField == SortField.AMOUNT,
          onClick = { localState = localState.copy(sortField = SortField.AMOUNT) }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Direction Toggle
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(BachatSurface)
            .clickable {
              localState = localState.copy(
                sortDirection = if (localState.sortDirection == SortDirection.DESC) SortDirection.ASC else SortDirection.DESC
              )
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (localState.sortDirection == SortDirection.DESC) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
              contentDescription = null,
              tint = BachatTextPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (localState.sortDirection == SortDirection.DESC) "High -> Low" else "Low -> High",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Bold,
              color = BachatTextPrimary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. VISIBILITY
      Text(
        text = "VISIBILITY",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        BachatFilterChip(
          label = "Standard",
          selected = localState.visibility == VisibilityFilter.STANDARD,
          onClick = { localState = localState.copy(visibility = VisibilityFilter.STANDARD) }
        )

        BachatFilterChip(
          label = "Secret",
          selected = localState.visibility == VisibilityFilter.SECRET,
          icon = if (isSecretLocked) Icons.Default.Lock else null,
          onClick = {
            if (isSecretLocked) {
              onRequestSecretUnlock {
                localState = localState.copy(visibility = VisibilityFilter.SECRET)
              }
            } else {
              localState = localState.copy(visibility = VisibilityFilter.SECRET)
            }
          }
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. ACCOUNT TAG
      Text(
        text = "ACCOUNT",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AccountTag.values().forEach { tag ->
          BachatFilterChip(
            label = tag.displayName,
            selected = localState.accountTag == tag,
            onClick = { localState = localState.copy(accountTag = tag) }
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 5. TYPE & CATEGORY
      Text(
        text = "TYPE",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("ALL", "EXPENSE", "INCOME").forEach { type ->
          BachatFilterChip(
            label = type.lowercase().replaceFirstChar { it.uppercase() },
            selected = localState.transactionType == type,
            onClick = { localState = localState.copy(transactionType = type) }
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Action buttons: Reset & Apply
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = {
            localState = FilterState()
          },
          shape = RoundedCornerShape(999.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BachatSurface,
            contentColor = BachatTextSecondary
          ),
          modifier = Modifier.weight(0.35f).height(48.dp)
        ) {
          Text("Reset", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = { onApplyFilter(localState) },
          shape = RoundedCornerShape(999.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BachatInk,
            contentColor = BachatOnColor
          ),
          modifier = Modifier.weight(0.65f).height(48.dp).testTag("apply_filter_button")
        ) {
          Text("Apply Filters", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

@Composable
fun TimePeriodTile(
  period: TimePeriod,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bgColor = if (selected) BachatInk else BachatSurface
  val contentColor = if (selected) Color.White else BachatTextPrimary

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(bgColor)
      .clickable(onClick = onClick)
      .padding(vertical = 12.dp, horizontal = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(
        imageVector = when (period) {
          TimePeriod.TODAY -> Icons.Default.CalendarMonth
          TimePeriod.WEEK -> Icons.Default.DateRange
          TimePeriod.MONTH -> Icons.Default.CalendarMonth
          TimePeriod.ALL -> Icons.Default.AllInclusive
          TimePeriod.CUSTOM -> Icons.Default.Tune
        },
        contentDescription = period.displayName,
        tint = contentColor,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = period.displayName,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = contentColor
      )
    }
  }
}

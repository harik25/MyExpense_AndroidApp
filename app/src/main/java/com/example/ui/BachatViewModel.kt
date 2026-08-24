package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.export.JsonBackupManager
import com.example.data.export.PdfExportManager
import com.example.data.local.AppSettings
import com.example.data.local.AppSettingsManager
import com.example.data.local.BachatDatabase
import com.example.data.model.AccountTag
import com.example.data.model.AppThemeMode
import com.example.data.model.CategoryItem
import com.example.data.model.Goal
import com.example.data.model.SortDirection
import com.example.data.model.SortField
import com.example.data.model.TimePeriod
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.VisibilityFilter
import com.example.data.repository.BachatRepository
import com.example.ui.components.DonutSegment
import com.example.ui.theme.BachatAccentIndigo
import com.example.ui.theme.BachatAccentIndigoTint
import com.example.ui.theme.BachatDanger
import com.example.ui.theme.BachatDangerTint
import com.example.ui.theme.BachatSuccess
import com.example.ui.theme.BachatSuccessTint
import com.example.ui.theme.BachatWarning
import com.example.ui.theme.BachatWarningTint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

data class FilterState(
  val timePeriod: TimePeriod = TimePeriod.MONTH,
  val accountTag: AccountTag = AccountTag.ALL,
  val transactionType: String = "ALL", // "ALL", "EXPENSE", "INCOME"
  val category: String = "All",
  val selectedCategories: Set<String> = emptySet(), // Multi-select category support
  val visibility: VisibilityFilter = VisibilityFilter.STANDARD,
  val sortField: SortField = SortField.DATE,
  val sortDirection: SortDirection = SortDirection.DESC,
  val customStartDate: Long = 0L,
  val customEndDate: Long = System.currentTimeMillis()
) {
  fun isCategorySelected(catName: String): Boolean {
    if (selectedCategories.isEmpty() || selectedCategories.contains("All")) return true
    return selectedCategories.any { it.equals(catName, ignoreCase = true) }
  }

  fun isAllCategories(): Boolean {
    return selectedCategories.isEmpty() || selectedCategories.contains("All")
  }
}

data class DashboardUiState(
  val totalBalance: Double = 0.0,
  val todayExpense: Double = 0.0,
  val weekExpense: Double = 0.0,
  val monthExpense: Double = 0.0,
  val todayTransactions: List<Transaction> = emptyList(),
  val topGoal: Goal? = null,
  val topGoalSpent: Double = 0.0,
  val donutPeriod: TimePeriod = TimePeriod.MONTH,
  val donutCategory: String = "All",
  val donutTotalExpense: Double = 0.0,
  val donutTopCategory: String = "",
  val donutSegments: List<DonutSegment> = emptyList()
)

data class CategoryAnalyticsDetail(
  val category: String,
  val totalAmount: Double,
  val transactionCount: Int,
  val percentage: Double,
  val color: Color,
  val tintColor: Color
)

data class AnalyticsUiState(
  val periodLabel: String = "Month",
  val periodIncome: Double = 0.0,
  val periodExpense: Double = 0.0,
  val netSavings: Double = 0.0,
  val savingsRate: Double = 0.0,
  val thisMonthExpense: Double = 0.0,
  val lastMonthExpense: Double = 0.0,
  val comparisonPercentage: Double = 0.0,
  val isLower: Boolean = true,
  val topCategory: String = "Food",
  val topCategoryAmount: Double = 0.0,
  val dailyAverage: Double = 0.0,
  val daysElapsed: Int = 20,
  val totalDaysInMonth: Int = 30,
  val projectedMonthEndExpense: Double = 0.0,
  val monthlyAverage: Double = 0.0,
  val transactionCount: Int = 0,
  val donutSegments: List<DonutSegment> = emptyList(),
  val weeklyBars: List<com.example.ui.components.SpendBarItem> = emptyList(),
  val categoryDetails: List<CategoryAnalyticsDetail> = emptyList()
)

sealed class BachatUiEvent {
  data class ShowUndoSnackbar(val message: String, val onUndo: () -> Unit) : BachatUiEvent()
  data class ShowToast(val message: String) : BachatUiEvent()
}

class BachatViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: BachatRepository
  private val settingsManager: AppSettingsManager = AppSettingsManager(application)

  val allGoals: StateFlow<List<Goal>>
  val allTransactions: StateFlow<List<Transaction>>
  val allCategories: StateFlow<List<CategoryItem>>
  val expenseCategories: StateFlow<List<CategoryItem>>
  val incomeCategories: StateFlow<List<CategoryItem>>
  val appSettings: StateFlow<AppSettings>

  private val _filterState = MutableStateFlow(FilterState())
  val filterState: StateFlow<FilterState> = _filterState

  private val _selectedTab = MutableStateFlow(0) // 0: Analytics, 1: Activity
  val selectedTab: StateFlow<Int> = _selectedTab

  private val _uiEvents = MutableSharedFlow<BachatUiEvent>()
  val uiEvents: SharedFlow<BachatUiEvent> = _uiEvents.asSharedFlow()

  // Failed PIN attempts & lockout state
  private val _failedPinAttempts = MutableStateFlow(0)
  val failedPinAttempts: StateFlow<Int> = _failedPinAttempts

  private val _pinLockoutUntil = MutableStateFlow(0L)
  val pinLockoutUntil: StateFlow<Long> = _pinLockoutUntil

  // Soft-deleted item caches for Undo
  private var lastDeletedTransaction: Transaction? = null
  private var lastDeletedGoal: Goal? = null

  init {
    val db = BachatDatabase.getDatabase(application, viewModelScope)
    repository = BachatRepository(
      db.transactionDao(),
      db.goalDao(),
      db.categoryDao()
    )

    allGoals = repository.allGoals.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )
    allTransactions = repository.allTransactions.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )
    allCategories = repository.allCategories.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )
    expenseCategories = allCategories.map { list ->
      list.filter { it.type == TransactionType.EXPENSE }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    incomeCategories = allCategories.map { list ->
      list.filter { it.type == TransactionType.INCOME }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    appSettings = settingsManager.settingsFlow.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      AppSettings()
    )
  }

  fun setTab(index: Int) {
    _selectedTab.value = index
  }

  fun updateFilter(update: FilterState.() -> FilterState) {
    _filterState.value = _filterState.value.update()
  }

  fun resetFilter() {
    _filterState.value = FilterState()
  }

  // Category management
  fun addCategory(name: String, type: TransactionType, iconName: String, colorHex: Long) {
    viewModelScope.launch {
      val cat = CategoryItem(
        name = name,
        type = type,
        iconName = iconName,
        colorHex = colorHex,
        isDefault = false
      )
      repository.insertCategory(cat)
      _uiEvents.emit(BachatUiEvent.ShowToast("Category '$name' created"))
    }
  }

  fun updateCategory(category: CategoryItem) {
    viewModelScope.launch {
      repository.updateCategory(category)
      _uiEvents.emit(BachatUiEvent.ShowToast("Category '${category.name}' updated"))
    }
  }

  fun deleteCategory(category: CategoryItem) {
    viewModelScope.launch {
      repository.deleteCategory(category)
      _uiEvents.emit(BachatUiEvent.ShowToast("Category '${category.name}' removed"))
    }
  }

  fun resetCategoriesToDefault() {
    viewModelScope.launch {
      repository.resetCategoriesToDefault()
      _uiEvents.emit(BachatUiEvent.ShowToast("Categories restored to default"))
    }
  }

  // App Settings actions
  fun setThemeMode(mode: AppThemeMode) {
    viewModelScope.launch {
      settingsManager.setThemeMode(mode)
    }
  }

  fun setSecretLockEnabled(enabled: Boolean) {
    viewModelScope.launch {
      settingsManager.setSecretLockEnabled(enabled)
    }
  }

  fun setPinHash(pin: String) {
    viewModelScope.launch {
      settingsManager.setPinHash(pin)
      settingsManager.setSecretLockEnabled(true)
    }
  }

  fun setUseBiometric(enabled: Boolean) {
    viewModelScope.launch {
      settingsManager.setUseBiometric(enabled)
    }
  }

  fun setGoalAlertsEnabled(enabled: Boolean) {
    viewModelScope.launch {
      settingsManager.setGoalAlertsEnabled(enabled)
    }
  }

  fun setShowGoalsOnDashboard(enabled: Boolean) {
    viewModelScope.launch {
      settingsManager.setShowGoalsOnDashboard(enabled)
    }
  }

  // Verify PIN with lockout logic
  fun verifyPin(enteredPin: String, onSuccess: () -> Unit, onFail: () -> Unit) {
    val now = System.currentTimeMillis()
    if (now < _pinLockoutUntil.value) {
      val secondsLeft = ((_pinLockoutUntil.value - now) / 1000).coerceAtLeast(1)
      viewModelScope.launch {
        _uiEvents.emit(BachatUiEvent.ShowToast("Too many failed attempts. Try again in $secondsLeft seconds"))
      }
      onFail()
      return
    }

    val currentPin = appSettings.value.pinHash
    if (enteredPin == currentPin) {
      _failedPinAttempts.value = 0
      onSuccess()
    } else {
      val attempts = _failedPinAttempts.value + 1
      _failedPinAttempts.value = attempts
      if (attempts >= 3) {
        _pinLockoutUntil.value = System.currentTimeMillis() + 30_000L // 30s lockout
        _failedPinAttempts.value = 0
        viewModelScope.launch {
          _uiEvents.emit(BachatUiEvent.ShowToast("3 failed attempts. Locked for 30 seconds."))
        }
      } else {
        viewModelScope.launch {
          _uiEvents.emit(BachatUiEvent.ShowToast("Incorrect PIN. Attempt $attempts of 3"))
        }
      }
      onFail()
    }
  }

  // Dashboard filter state for the Donut Widget
  private val _dashboardDonutPeriod = MutableStateFlow(TimePeriod.MONTH)
  val dashboardDonutPeriod: StateFlow<TimePeriod> = _dashboardDonutPeriod

  private val _dashboardDonutCategory = MutableStateFlow("All")
  val dashboardDonutCategory: StateFlow<String> = _dashboardDonutCategory

  fun setDashboardDonutPeriod(period: TimePeriod) {
    _dashboardDonutPeriod.value = period
  }

  fun setDashboardDonutCategory(category: String) {
    _dashboardDonutCategory.value = category
  }

  // Dashboard calculation
  val dashboardState: StateFlow<DashboardUiState> = combine(
    allTransactions,
    allGoals,
    allCategories,
    _dashboardDonutPeriod,
    _dashboardDonutCategory
  ) { transactions, goals, categories, donutPeriod, donutCategory ->
    val categoryColorMap = categories.associate { it.name.lowercase() to Color(it.colorHex) }

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

    val validTx = transactions.filter { !it.isSecret }

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

    val todayTxList = validTx.filter { it.timestamp >= startOfToday }

    val topGoal = goals.firstOrNull()
    var topGoalSpent = 0.0
    if (topGoal != null) {
      topGoalSpent = validTx.filter {
        it.type == TransactionType.EXPENSE &&
        it.category.equals(topGoal.category, ignoreCase = true) &&
        it.timestamp >= startOfMonth
      }.sumOf { it.amount }
    }

    // Compute Donut Segment data for the Dashboard Donut Widget
    val donutFilteredTx = validTx.filter { tx ->
      val inTime = when (donutPeriod) {
        TimePeriod.TODAY -> tx.timestamp >= startOfToday
        TimePeriod.WEEK -> tx.timestamp >= startOfWeek
        TimePeriod.MONTH -> tx.timestamp >= startOfMonth
        TimePeriod.ALL -> true
        TimePeriod.CUSTOM -> true
      }
      val inCat = if (donutCategory == "All" || donutCategory.isBlank()) true else tx.category.equals(donutCategory, ignoreCase = true)
      inTime && inCat
    }

    val donutExpenses = donutFilteredTx.filter { it.type == TransactionType.EXPENSE }
    val donutTotalSpent = donutExpenses.sumOf { it.amount }

    val defaultPalette = listOf(
      Pair(BachatAccentIndigo, BachatAccentIndigoTint),
      Pair(BachatDanger, BachatDangerTint),
      Pair(BachatSuccess, BachatSuccessTint),
      Pair(BachatWarning, BachatWarningTint),
      Pair(Color(0xFF8B5CF6), Color(0xFF8B5CF6).copy(alpha = 0.15f)),
      Pair(Color(0xFF06B6D4), Color(0xFF06B6D4).copy(alpha = 0.15f)),
      Pair(Color(0xFFEC4899), Color(0xFFEC4899).copy(alpha = 0.15f))
    )

    val expenseByCategory = donutExpenses.groupBy { it.category }
    val sortedCategoryEntries = expenseByCategory.entries.sortedByDescending { it.value.sumOf { tx -> tx.amount } }
    val totalExpenseForBreakdown = if (donutTotalSpent > 0) donutTotalSpent else 1.0

    val donutSegments = sortedCategoryEntries.mapIndexed { index, entry ->
      val catAmount = entry.value.sumOf { it.amount }
      val customColor = categoryColorMap[entry.key.lowercase()]
      val color = customColor ?: defaultPalette[index % defaultPalette.size].first
      val tint = customColor?.copy(alpha = 0.15f) ?: defaultPalette[index % defaultPalette.size].second
      val pct = (catAmount / totalExpenseForBreakdown) * 100.0
      DonutSegment(
        category = entry.key,
        amount = catAmount,
        percentage = pct,
        color = color,
        tintColor = tint
      )
    }

    val donutTopCategory = sortedCategoryEntries.firstOrNull()?.key ?: ""

    DashboardUiState(
      totalBalance = totalBalance,
      todayExpense = todayExpense,
      weekExpense = weekExpense,
      monthExpense = monthExpense,
      todayTransactions = todayTxList,
      topGoal = topGoal,
      topGoalSpent = topGoalSpent,
      donutPeriod = donutPeriod,
      donutCategory = donutCategory,
      donutTotalExpense = donutTotalSpent,
      donutTopCategory = donutTopCategory,
      donutSegments = donutSegments
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

  // Filtered & Sorted Transactions for History / Activity
  val filteredTransactions: StateFlow<List<Transaction>> = combine(
    allTransactions,
    filterState
  ) { transactions, filter ->
    var filtered = transactions

    // Filter by Visibility
    filtered = when (filter.visibility) {
      VisibilityFilter.STANDARD -> filtered.filter { !it.isSecret }
      VisibilityFilter.SECRET -> filtered.filter { it.isSecret }
    }

    // Filter by Time Period
    val now = Calendar.getInstance()
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

    filtered = when (filter.timePeriod) {
      TimePeriod.TODAY -> filtered.filter { it.timestamp >= startOfToday }
      TimePeriod.WEEK -> filtered.filter { it.timestamp >= startOfWeek }
      TimePeriod.MONTH -> filtered.filter { it.timestamp >= startOfMonth }
      TimePeriod.ALL -> filtered
      TimePeriod.CUSTOM -> filtered.filter {
        it.timestamp in filter.customStartDate..filter.customEndDate
      }
    }

    // Filter by Account Tag
    if (filter.accountTag != AccountTag.ALL) {
      filtered = filtered.filter { it.accountTag.equals(filter.accountTag.displayName, ignoreCase = true) }
    }

    // Filter by Transaction Type
    if (filter.transactionType == "EXPENSE") {
      filtered = filtered.filter { it.type == TransactionType.EXPENSE }
    } else if (filter.transactionType == "INCOME") {
      filtered = filtered.filter { it.type == TransactionType.INCOME }
    }

    // Filter by Category
    if (filter.category != "All" && filter.category.isNotBlank()) {
      filtered = filtered.filter { it.category.equals(filter.category, ignoreCase = true) }
    }

    // Sort by Field & Direction
    when (filter.sortField) {
      SortField.DATE -> {
        filtered = if (filter.sortDirection == SortDirection.DESC) {
          filtered.sortedByDescending { it.timestamp }
        } else {
          filtered.sortedBy { it.timestamp }
        }
      }
      SortField.AMOUNT -> {
        filtered = if (filter.sortDirection == SortDirection.DESC) {
          filtered.sortedByDescending { it.amount }
        } else {
          filtered.sortedBy { it.amount }
        }
      }
    }

    filtered
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Analytics tab calculation - dynamically computed from active FilterState & Categories
  val analyticsState: StateFlow<AnalyticsUiState> = combine(
    allTransactions,
    filterState,
    allCategories
  ) { transactions, filter, categories ->
    val categoryColorMap = categories.associate { it.name.lowercase() to Color(it.colorHex) }

    // Filter by Visibility
    var baseTx = when (filter.visibility) {
      VisibilityFilter.STANDARD -> transactions.filter { !it.isSecret }
      VisibilityFilter.SECRET -> transactions.filter { it.isSecret }
    }

    // Filter by Account Tag
    if (filter.accountTag != AccountTag.ALL) {
      baseTx = baseTx.filter { it.accountTag.equals(filter.accountTag.displayName, ignoreCase = true) }
    }

    // Filter by Specific Category if selected
    if (filter.category != "All" && filter.category.isNotBlank()) {
      baseTx = baseTx.filter { it.category.equals(filter.category, ignoreCase = true) }
    }

    // Filter by Transaction Type if specified
    if (filter.transactionType == "EXPENSE") {
      baseTx = baseTx.filter { it.type == TransactionType.EXPENSE }
    } else if (filter.transactionType == "INCOME") {
      baseTx = baseTx.filter { it.type == TransactionType.INCOME }
    }

    var currentStart = 0L
    var currentEnd = Long.MAX_VALUE
    var prevStart = 0L
    var prevEnd = 0L
    var periodLabel = filter.timePeriod.displayName
    var weeklyBars: List<com.example.ui.components.SpendBarItem> = emptyList()

    when (filter.timePeriod) {
      TimePeriod.TODAY -> {
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

        val startOfYesterday = Calendar.getInstance().apply {
          add(Calendar.DAY_OF_YEAR, -1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfYesterday = Calendar.getInstance().apply {
          add(Calendar.DAY_OF_YEAR, -1)
          set(Calendar.HOUR_OF_DAY, 23)
          set(Calendar.MINUTE, 59)
          set(Calendar.SECOND, 59)
          set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        currentStart = startOfToday
        currentEnd = endOfToday
        prevStart = startOfYesterday
        prevEnd = endOfYesterday
        periodLabel = "Today"

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val todayExpenses = baseTx.filter { it.type == TransactionType.EXPENSE && it.timestamp in currentStart..currentEnd }

        weeklyBars = listOf(
          com.example.ui.components.SpendBarItem(
            label = "Morn",
            subLabel = "6a-12p",
            amount = todayExpenses.filter {
              val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
              cal.get(Calendar.HOUR_OF_DAY) in 6..11
            }.sumOf { it.amount },
            isHighlighted = currentHour in 6..11
          ),
          com.example.ui.components.SpendBarItem(
            label = "Aft",
            subLabel = "12p-5p",
            amount = todayExpenses.filter {
              val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
              cal.get(Calendar.HOUR_OF_DAY) in 12..16
            }.sumOf { it.amount },
            isHighlighted = currentHour in 12..16
          ),
          com.example.ui.components.SpendBarItem(
            label = "Eve",
            subLabel = "5p-9p",
            amount = todayExpenses.filter {
              val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
              cal.get(Calendar.HOUR_OF_DAY) in 17..20
            }.sumOf { it.amount },
            isHighlighted = currentHour in 17..20
          ),
          com.example.ui.components.SpendBarItem(
            label = "Night",
            subLabel = "9p-6a",
            amount = todayExpenses.filter {
              val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
              val h = cal.get(Calendar.HOUR_OF_DAY)
              h >= 21 || h < 6
            }.sumOf { it.amount },
            isHighlighted = currentHour >= 21 || currentHour < 6
          )
        )
      }

      TimePeriod.WEEK -> {
        val weekCal = Calendar.getInstance().apply {
          set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        currentStart = weekCal.timeInMillis
        currentEnd = Calendar.getInstance().apply {
          timeInMillis = currentStart
          add(Calendar.DAY_OF_YEAR, 6)
          set(Calendar.HOUR_OF_DAY, 23)
          set(Calendar.MINUTE, 59)
          set(Calendar.SECOND, 59)
          set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        prevStart = Calendar.getInstance().apply {
          timeInMillis = currentStart
          add(Calendar.DAY_OF_YEAR, -7)
        }.timeInMillis
        prevEnd = currentStart - 1
        periodLabel = "This Week"

        val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val weekExpenses = baseTx.filter { it.type == TransactionType.EXPENSE && it.timestamp in currentStart..currentEnd }

        weeklyBars = (0..6).map { dayOffset ->
          val dayCal = Calendar.getInstance().apply {
            timeInMillis = currentStart
            add(Calendar.DAY_OF_YEAR, dayOffset)
          }
          val dayStart = dayCal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }.timeInMillis
          val dayEnd = dayCal.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
          }.timeInMillis

          val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)
          val dayOfWeekIdx = dayCal.get(Calendar.DAY_OF_WEEK) - 1
          val spend = weekExpenses.filter { it.timestamp in dayStart..dayEnd }.sumOf { it.amount }

          com.example.ui.components.SpendBarItem(
            label = dayLabels.getOrElse(dayOfWeekIdx) { "D" },
            subLabel = "$dayNum",
            amount = spend,
            isHighlighted = dayCal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
          )
        }
      }

      TimePeriod.MONTH -> {
        val monthCal = Calendar.getInstance().apply {
          set(Calendar.DAY_OF_MONTH, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        currentStart = monthCal.timeInMillis
        val totalDaysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        currentEnd = Calendar.getInstance().apply {
          set(Calendar.DAY_OF_MONTH, totalDaysInMonth)
          set(Calendar.HOUR_OF_DAY, 23)
          set(Calendar.MINUTE, 59)
          set(Calendar.SECOND, 59)
          set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        prevStart = Calendar.getInstance().apply {
          add(Calendar.MONTH, -1)
          set(Calendar.DAY_OF_MONTH, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        prevEnd = currentStart - 1
        periodLabel = "This Month"

        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val currentWeekIdx = ((currentDay - 1) / 7).coerceIn(0, 4)
        val monthExpenses = baseTx.filter { it.type == TransactionType.EXPENSE && it.timestamp in currentStart..currentEnd }

        weeklyBars = (0..4).map { weekIdx ->
          val startDay = weekIdx * 7 + 1
          val endDay = ((weekIdx + 1) * 7).coerceAtMost(totalDaysInMonth)

          val calStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, startDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }.timeInMillis

          val calEnd = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, endDay)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
          }.timeInMillis

          val weekSpend = monthExpenses
            .filter { it.timestamp in calStart..calEnd }
            .sumOf { it.amount }

          com.example.ui.components.SpendBarItem(
            label = "W${weekIdx + 1}",
            subLabel = "$startDay-$endDay",
            amount = weekSpend,
            isHighlighted = weekIdx == currentWeekIdx
          )
        }
      }

      TimePeriod.ALL -> {
        currentStart = 0L
        currentEnd = Long.MAX_VALUE
        prevStart = 0L
        prevEnd = 0L
        periodLabel = "All Time"

        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        weeklyBars = (5 downTo 0).map { monthsAgo ->
          val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, -monthsAgo)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }
          val start = cal.timeInMillis
          val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
          val end = Calendar.getInstance().apply {
            timeInMillis = start
            set(Calendar.DAY_OF_MONTH, maxDay)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
          }.timeInMillis

          val mIdx = cal.get(Calendar.MONTH)
          val spend = baseTx.filter { it.type == TransactionType.EXPENSE && it.timestamp in start..end }.sumOf { it.amount }

          com.example.ui.components.SpendBarItem(
            label = monthNames[mIdx],
            subLabel = "${cal.get(Calendar.YEAR)}".takeLast(2),
            amount = spend,
            isHighlighted = monthsAgo == 0
          )
        }
      }

      TimePeriod.CUSTOM -> {
        currentStart = filter.customStartDate
        currentEnd = filter.customEndDate
        val duration = (currentEnd - currentStart).coerceAtLeast(86400000L)
        prevEnd = currentStart - 1
        prevStart = prevEnd - duration
        periodLabel = "Custom Range"

        val segmentDuration = duration / 4
        weeklyBars = (0..3).map { segIdx ->
          val sStart = currentStart + (segIdx * segmentDuration)
          val sEnd = if (segIdx == 3) currentEnd else (sStart + segmentDuration - 1)
          val spend = baseTx.filter { it.type == TransactionType.EXPENSE && it.timestamp in sStart..sEnd }.sumOf { it.amount }

          com.example.ui.components.SpendBarItem(
            label = "P${segIdx + 1}",
            subLabel = "Part ${segIdx + 1}",
            amount = spend,
            isHighlighted = System.currentTimeMillis() in sStart..sEnd
          )
        }
      }
    }

    val currentPeriodTx = baseTx.filter { it.timestamp in currentStart..currentEnd }
    val previousPeriodTx = if (prevStart > 0) baseTx.filter { it.timestamp in prevStart..prevEnd } else emptyList()

    val periodIncome = currentPeriodTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val currentPeriodExpense = currentPeriodTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val previousPeriodExpense = previousPeriodTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    var comparisonPercentage = 0.0
    var isLower = true
    if (previousPeriodExpense > 0) {
      comparisonPercentage = ((previousPeriodExpense - currentPeriodExpense) / previousPeriodExpense) * 100.0
      if (comparisonPercentage < 0) {
        isLower = false
        comparisonPercentage = -comparisonPercentage
      }
    }

    val totalDaysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysElapsed = when (filter.timePeriod) {
      TimePeriod.TODAY -> 1
      TimePeriod.WEEK -> {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        dayOfWeek.coerceIn(1, 7)
      }
      TimePeriod.MONTH -> {
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
      }
      TimePeriod.CUSTOM -> {
        val span = (((currentEnd - currentStart) / (1000 * 60 * 60 * 24)) + 1).coerceAtLeast(1L)
        span.toInt()
      }
      TimePeriod.ALL -> {
        val earliest = currentPeriodTx.minOfOrNull { it.timestamp } ?: (System.currentTimeMillis() - 86400000L)
        val span = (((System.currentTimeMillis() - earliest) / (1000 * 60 * 60 * 24)) + 1).coerceAtLeast(1L)
        span.toInt()
      }
    }

    val amountForAverage = if (filter.transactionType == "INCOME") {
      periodIncome
    } else {
      currentPeriodExpense
    }
    val dailyAvg = if (daysElapsed > 0) amountForAverage / daysElapsed else 0.0
    val monthlyAvg = currentPeriodExpense
    val projectedMonthEnd = dailyAvg * totalDaysInMonth
    val netSavings = periodIncome - currentPeriodExpense
    val savingsRate = if (periodIncome > 0) ((periodIncome - currentPeriodExpense) / periodIncome * 100.0).coerceAtLeast(0.0) else 0.0

    val expenseByCategory = currentPeriodTx
      .filter { it.type == TransactionType.EXPENSE }
      .groupBy { it.category }

    val topEntry = expenseByCategory.entries.maxByOrNull { it.value.sumOf { tx -> tx.amount } }
    val topCatName = topEntry?.key ?: "Food"
    val topCatAmount = topEntry?.value?.sumOf { it.amount } ?: 0.0

    val defaultPalette = listOf(
      Pair(BachatAccentIndigo, BachatAccentIndigoTint),
      Pair(BachatDanger, BachatDangerTint),
      Pair(BachatSuccess, BachatSuccessTint),
      Pair(BachatWarning, BachatWarningTint),
      Pair(Color(0xFF8B5CF6), Color(0xFF8B5CF6).copy(alpha = 0.15f)),
      Pair(Color(0xFF06B6D4), Color(0xFF06B6D4).copy(alpha = 0.15f)),
      Pair(Color(0xFFEC4899), Color(0xFFEC4899).copy(alpha = 0.15f))
    )

    val totalExpenseForBreakdown = if (currentPeriodExpense > 0) currentPeriodExpense else 1.0
    val sortedCategoryEntries = expenseByCategory.entries.sortedByDescending { it.value.sumOf { tx -> tx.amount } }

    val segments = sortedCategoryEntries.mapIndexed { index, entry ->
      val catAmount = entry.value.sumOf { it.amount }
      val customColor = categoryColorMap[entry.key.lowercase()]
      val color = customColor ?: defaultPalette[index % defaultPalette.size].first
      val tint = customColor?.copy(alpha = 0.15f) ?: defaultPalette[index % defaultPalette.size].second
      val pct = (catAmount / totalExpenseForBreakdown) * 100.0
      DonutSegment(
        category = entry.key,
        amount = catAmount,
        percentage = pct,
        color = color,
        tintColor = tint
      )
    }

    val categoryDetails = sortedCategoryEntries.mapIndexed { index, entry ->
      val catAmount = entry.value.sumOf { it.amount }
      val customColor = categoryColorMap[entry.key.lowercase()]
      val color = customColor ?: defaultPalette[index % defaultPalette.size].first
      val tint = customColor?.copy(alpha = 0.15f) ?: defaultPalette[index % defaultPalette.size].second
      val pct = (catAmount / totalExpenseForBreakdown) * 100.0
      CategoryAnalyticsDetail(
        category = entry.key,
        totalAmount = catAmount,
        transactionCount = entry.value.size,
        percentage = pct,
        color = color,
        tintColor = tint
      )
    }

    AnalyticsUiState(
      periodLabel = periodLabel,
      periodIncome = periodIncome,
      periodExpense = currentPeriodExpense,
      netSavings = netSavings,
      savingsRate = savingsRate,
      thisMonthExpense = currentPeriodExpense,
      lastMonthExpense = previousPeriodExpense,
      comparisonPercentage = comparisonPercentage,
      isLower = isLower,
      topCategory = topCatName,
      topCategoryAmount = topCatAmount,
      dailyAverage = dailyAvg,
      daysElapsed = daysElapsed,
      totalDaysInMonth = totalDaysInMonth,
      projectedMonthEndExpense = projectedMonthEnd,
      monthlyAverage = monthlyAvg,
      transactionCount = currentPeriodTx.size,
      donutSegments = segments,
      weeklyBars = weeklyBars,
      categoryDetails = categoryDetails
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())

  // Transactions CRUD + Undo
  fun addTransaction(
    title: String,
    amount: Double,
    type: TransactionType,
    category: String,
    accountTag: String = "UPI",
    timestamp: Long = System.currentTimeMillis(),
    isSecret: Boolean = false,
    note: String = "",
    receiptUri: String? = null
  ) {
    viewModelScope.launch {
      val tx = Transaction(
        title = title,
        amount = amount,
        type = type,
        category = category,
        accountTag = accountTag,
        timestamp = timestamp,
        isAuto = false,
        isSecret = isSecret,
        note = note,
        receiptUri = receiptUri
      )
      repository.insertTransaction(tx)
      com.example.ui.widget.BachatDonutWidgetProvider.notifyDataChanged(getApplication())
    }
  }

  fun updateTransaction(transaction: Transaction) {
    viewModelScope.launch {
      repository.updateTransaction(transaction)
      com.example.ui.widget.BachatDonutWidgetProvider.notifyDataChanged(getApplication())
    }
  }

  fun deleteTransaction(transaction: Transaction) {
    viewModelScope.launch {
      lastDeletedTransaction = transaction
      repository.deleteTransaction(transaction)
      com.example.ui.widget.BachatDonutWidgetProvider.notifyDataChanged(getApplication())
      _uiEvents.emit(
        BachatUiEvent.ShowUndoSnackbar(
          message = "Transaction deleted",
          onUndo = {
            viewModelScope.launch {
              lastDeletedTransaction?.let { repository.insertTransaction(it) }
              lastDeletedTransaction = null
            }
          }
        )
      )
    }
  }

  // Goals CRUD + Undo
  fun addGoal(title: String, cadence: String, category: String, targetAmount: Double) {
    viewModelScope.launch {
      val goal = Goal(
        title = title,
        cadence = cadence,
        category = category,
        targetAmount = targetAmount
      )
      repository.insertGoal(goal)
    }
  }

  fun updateGoal(goal: Goal) {
    viewModelScope.launch {
      repository.updateGoal(goal)
    }
  }

  fun deleteGoal(goal: Goal) {
    viewModelScope.launch {
      lastDeletedGoal = goal
      repository.deleteGoal(goal)
      _uiEvents.emit(
        BachatUiEvent.ShowUndoSnackbar(
          message = "Goal deleted",
          onUndo = {
            viewModelScope.launch {
              lastDeletedGoal?.let { repository.insertGoal(it) }
              lastDeletedGoal = null
            }
          }
        )
      )
    }
  }

  // Export / Import JSON & PDF
  fun exportJson(): String {
    return JsonBackupManager.exportToJson(
      transactions = allTransactions.value,
      goals = allGoals.value
    )
  }

  fun importJson(jsonContent: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    viewModelScope.launch {
      try {
        val (txs, goals) = JsonBackupManager.importFromJson(jsonContent)
        repository.clearAll()
        repository.insertTransactions(txs)
        repository.insertGoals(goals)
        com.example.ui.widget.BachatDonutWidgetProvider.notifyDataChanged(getApplication())
        onSuccess()
      } catch (e: Exception) {
        onError(e.localizedMessage ?: "Invalid JSON format")
      }
    }
  }

  fun exportActivityPdf(context: Context): File {
    return PdfExportManager.generateActivityPdf(
      context = context,
      transactions = filteredTransactions.value,
      periodLabel = filterState.value.timePeriod.displayName
    )
  }
}

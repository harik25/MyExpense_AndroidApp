package com.example.ui.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.Goal
import com.example.data.model.RecurringBill
import com.example.data.model.Transaction
import com.example.ui.BachatUiEvent
import com.example.ui.BachatViewModel
import com.example.ui.bills.NewRecurringBillSheet
import com.example.ui.bills.RecurringBillsScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.goals.AllGoalsScreen
import com.example.ui.goals.NewGoalSheet
import com.example.ui.history.HistoryAnalyticsScreen
import com.example.ui.security.PinMode
import com.example.ui.security.PinScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.BachatInk
import com.example.ui.theme.BachatTextSecondary
import com.example.ui.transaction.NewTransactionSheet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
  object Dashboard : Screen("dashboard")
  object History : Screen("history")
  object AllGoals : Screen("all_goals")
  object RecurringBills : Screen("recurring_bills")
  object Settings : Screen("settings")
  object PinSetup : Screen("pin_setup")
  object PinUnlock : Screen("pin_unlock")
}

@Composable
fun BachatMainContainer(
  viewModel: BachatViewModel = viewModel(),
  quickAddTrigger: Boolean = false,
  onQuickAddHandled: () -> Unit = {}
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

  val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
  val analyticsState by viewModel.analyticsState.collectAsStateWithLifecycle()
  val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
  val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
  val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
  val allRecurringBills by viewModel.allRecurringBills.collectAsStateWithLifecycle()
  val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
  val filterState by viewModel.filterState.collectAsStateWithLifecycle()
  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

  var showNewTransactionSheet by remember { mutableStateOf(false) }
  var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

  LaunchedEffect(quickAddTrigger) {
    if (quickAddTrigger) {
      editingTransaction = null
      showNewTransactionSheet = true
      onQuickAddHandled()
    }
  }

  var showNewGoalSheet by remember { mutableStateOf(false) }
  var editingGoal by remember { mutableStateOf<Goal?>(null) }

  var showNewBillSheet by remember { mutableStateOf(false) }
  var editingBill by remember { mutableStateOf<RecurringBill?>(null) }

  var pendingUnlockCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  val pagerState = rememberPagerState(pageCount = { 2 })

  LaunchedEffect(Unit) {
    viewModel.uiEvents.collectLatest { event ->
      when (event) {
        is BachatUiEvent.ShowToast -> {
          Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
        is BachatUiEvent.ShowUndoSnackbar -> {
          scope.launch {
            val result = snackbarHostState.showSnackbar(
              message = event.message,
              actionLabel = "Undo",
              duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
              event.onUndo()
            }
          }
        }
      }
    }
  }

  val isBottomNavVisible = currentRoute == Screen.Dashboard.route

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = Color.White,
      contentWindowInsets = WindowInsets.statusBars,
      snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
      NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        // 1. DASHBOARD & HISTORY PAGER
        composable(Screen.Dashboard.route) {
          HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
              0 -> {
                DashboardScreen(
                  uiState = dashboardState,
                  showGoals = appSettings.showGoalsOnDashboard,
                  showBills = appSettings.showBillsOnDashboard,
                  onNavigateToGoals = { navController.navigate(Screen.AllGoals.route) },
                  onNavigateToBills = { navController.navigate(Screen.RecurringBills.route) },
                  onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                  onNavigateToHistory = { scope.launch { pagerState.animateScrollToPage(1) } },
                  onOpenNewTransaction = {
                    editingTransaction = null
                    showNewTransactionSheet = true
                  },
                  onEditTransaction = { tx ->
                    editingTransaction = tx
                    showNewTransactionSheet = true
                  },
                  onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) },
                  onEditGoal = { goal ->
                    editingGoal = goal
                    showNewGoalSheet = true
                  },
                  onDeleteGoal = { goal -> viewModel.deleteGoal(goal) },
                  onPayBill = { bill -> viewModel.payRecurringBill(bill) }
                )
              }
              1 -> {
                HistoryAnalyticsScreen(
                  selectedTab = selectedTab,
                  onTabSelect = { viewModel.setTab(it) },
                  analyticsState = analyticsState,
                  filteredTransactions = filteredTransactions,
                  filterState = filterState,
                  categories = allCategories,
                  isSecretLocked = appSettings.secretLockEnabled && appSettings.pinHash.isNotEmpty(),
                  onUpdateFilter = { viewModel.updateFilter(it) },
                  onResetFilter = { viewModel.resetFilter() },
                  onExportPdf = { viewModel.exportActivityPdf(context) },
                  onBackClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                  onEditTransaction = { tx ->
                    editingTransaction = tx
                    showNewTransactionSheet = true
                  },
                  onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) },
                  onRequestSecretUnlock = { onUnlockSuccess ->
                    pendingUnlockCallback = onUnlockSuccess
                    navController.navigate(Screen.PinUnlock.route)
                  }
                )
              }
            }
          }
        }

        // 3. ALL GOALS
        composable(Screen.AllGoals.route) {
          AllGoalsScreen(
            goals = allGoals,
            transactions = allTransactions,
            onBackClick = { navController.popBackStack() },
            onOpenNewGoal = {
              editingGoal = null
              showNewGoalSheet = true
            },
            onEditGoal = { goal ->
              editingGoal = goal
              showNewGoalSheet = true
            },
            onDeleteGoal = { goal -> viewModel.deleteGoal(goal) }
          )
        }

        // 4. SETTINGS
        composable(Screen.Settings.route) {
          SettingsScreen(
            appSettings = appSettings,
            categories = allCategories,
            bills = allRecurringBills,
            onBackClick = { navController.popBackStack() },
            onNavigateToPinSetup = { navController.navigate(Screen.PinSetup.route) },
            onNavigateToAllGoals = { navController.navigate(Screen.AllGoals.route) },
            onNavigateToBills = { navController.navigate(Screen.RecurringBills.route) },
            onSetThemeMode = { viewModel.setThemeMode(it) },
            onSetSecretLockEnabled = { viewModel.setSecretLockEnabled(it) },
            onSetUseBiometric = { viewModel.setUseBiometric(it) },
            onSetGoalAlertsEnabled = { viewModel.setGoalAlertsEnabled(it) },
            onSetShowGoalsOnDashboard = { viewModel.setShowGoalsOnDashboard(it) },
            onSetShowBillsOnDashboard = { viewModel.setShowBillsOnDashboard(it) },
            onAddCategory = { name, type, icon, color -> viewModel.addCategory(name, type, icon, color) },
            onUpdateCategory = { viewModel.updateCategory(it) },
            onDeleteCategory = { viewModel.deleteCategory(it) },
            onResetCategories = { viewModel.resetCategoriesToDefault() },
            onExportJson = { viewModel.exportJson() },
            onImportJson = { json, onSuccess ->
              viewModel.importJson(json, onSuccess = onSuccess, onError = {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
              })
            }
          )
        }

        // 5. RECURRING BILLS
        composable(Screen.RecurringBills.route) {
          RecurringBillsScreen(
            bills = allRecurringBills,
            categories = allCategories,
            onBackClick = { navController.popBackStack() },
            onOpenNewBill = {
              editingBill = null
              showNewBillSheet = true
            },
            onEditBill = { bill ->
              editingBill = bill
              showNewBillSheet = true
            },
            onDeleteBill = { bill -> viewModel.deleteRecurringBill(bill) },
            onPayBill = { bill -> viewModel.payRecurringBill(bill) }
          )
        }

        // 6. PIN SETUP
        composable(Screen.PinSetup.route) {
          PinScreen(
            mode = PinMode.SETUP,
            useBiometric = false,
            onPinSuccess = { newPin ->
              viewModel.setPinHash(newPin)
              Toast.makeText(context, "PIN set successfully!", Toast.LENGTH_SHORT).show()
              navController.popBackStack()
            },
            onBackClick = { navController.popBackStack() }
          )
        }

        // 7. PIN UNLOCK
        composable(Screen.PinUnlock.route) {
          PinScreen(
            mode = PinMode.UNLOCK,
            useBiometric = appSettings.useBiometric,
            onPinSuccess = { entered ->
              if (entered == "BIOMETRIC_SUCCESS") {
                pendingUnlockCallback?.invoke()
                pendingUnlockCallback = null
                navController.popBackStack()
              } else {
                viewModel.verifyPin(
                  enteredPin = entered,
                  onSuccess = {
                    pendingUnlockCallback?.invoke()
                    pendingUnlockCallback = null
                    navController.popBackStack()
                  },
                  onFail = {
                    // Handled inside verifyPin
                  }
                )
              }
            },
            onBackClick = {
              pendingUnlockCallback = null
              navController.popBackStack()
            }
          )
        }
      }
    }

    // Dynamic Floating Bottom Bar + Floating Action Button (FAB)
    if (isBottomNavVisible) {
      val fabInteractionSource = remember { MutableInteractionSource() }
      val isFabPressed by fabInteractionSource.collectIsPressedAsState()
      val fabScale by animateFloatAsState(
        targetValue = if (isFabPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "fab_scale"
      )
      val fabRotation by animateFloatAsState(
        targetValue = if (isFabPressed) 45f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "fab_rotation"
      )

      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .widthIn(max = 440.dp)
          .padding(
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp,
            start = 24.dp,
            end = 24.dp
          )
      ) {
        // Bottom Bar Surface Dock
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(
              elevation = 14.dp,
              shape = RoundedCornerShape(36.dp),
              spotColor = Color(0x330F172A),
              ambientColor = Color(0x1F0F172A)
            ),
          shape = RoundedCornerShape(36.dp),
          color = Color.White,
          border = BorderStroke(1.dp, Color(0xFF0F172A).copy(alpha = 0.08f)),
          tonalElevation = 4.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // 1. Home Item with Dynamic Active Capsule
            val isHome = pagerState.currentPage == 0
            val homeScale by animateFloatAsState(
              targetValue = if (isHome) 1.08f else 1.0f,
              animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
              label = "home_scale"
            )
            val homeIconTint by animateColorAsState(
              targetValue = if (isHome) BachatInk else BachatTextSecondary,
              label = "home_color"
            )
            val homeBgAlpha by animateFloatAsState(
              targetValue = if (isHome) 1f else 0f,
              animationSpec = spring(stiffness = Spring.StiffnessLow),
              label = "home_bg_alpha"
            )

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.06f * homeBgAlpha))
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                ) {
                  scope.launch { pagerState.animateScrollToPage(0) }
                }
                .padding(vertical = 8.dp)
                .testTag("nav_home"),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(homeScale)
              ) {
                Icon(
                  imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                  contentDescription = "Home",
                  tint = homeIconTint,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Home",
                  fontSize = 11.5.sp,
                  fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
                  color = homeIconTint
                )
              }
            }

            // Center Gap for FAB
            Spacer(modifier = Modifier.width(68.dp))

            // 2. History Item with Dynamic Active Capsule
            val isHistory = pagerState.currentPage == 1
            val historyScale by animateFloatAsState(
              targetValue = if (isHistory) 1.08f else 1.0f,
              animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
              label = "history_scale"
            )
            val historyIconTint by animateColorAsState(
              targetValue = if (isHistory) BachatInk else BachatTextSecondary,
              label = "history_color"
            )
            val historyBgAlpha by animateFloatAsState(
              targetValue = if (isHistory) 1f else 0f,
              animationSpec = spring(stiffness = Spring.StiffnessLow),
              label = "history_bg_alpha"
            )

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.06f * historyBgAlpha))
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                ) {
                  scope.launch { pagerState.animateScrollToPage(1) }
                }
                .padding(vertical = 8.dp)
                .testTag("nav_history"),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(historyScale)
              ) {
                Icon(
                  imageVector = if (isHistory) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                  contentDescription = "History",
                  tint = historyIconTint,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "History",
                  fontSize = 11.5.sp,
                  fontWeight = if (isHistory) FontWeight.Bold else FontWeight.Medium,
                  color = historyIconTint
                )
              }
            }
          }
        }

        // Floating Action Button in Center with Dynamic Scale & Obsidian Styling
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .offset(y = (-14).dp)
            .scale(fabScale)
        ) {
          FloatingActionButton(
            onClick = {
              editingTransaction = null
              showNewTransactionSheet = true
            },
            shape = CircleShape,
            containerColor = BachatInk,
            contentColor = Color.White,
            interactionSource = fabInteractionSource,
            elevation = FloatingActionButtonDefaults.elevation(
              defaultElevation = 8.dp,
              pressedElevation = 2.dp
            ),
            modifier = Modifier
              .size(62.dp)
              .border(
                border = BorderStroke(2.dp, Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x00FFFFFF)))),
                shape = CircleShape
              )
              .testTag("fab_new_transaction")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add Transaction",
              tint = Color.White,
              modifier = Modifier
                .size(30.dp)
                .rotate(fabRotation)
            )
          }
        }
      }
    }

    // Modal New / Edit Transaction Sheet
    if (showNewTransactionSheet) {
      NewTransactionSheet(
        editingTransaction = editingTransaction,
        categories = allCategories,
        onDismiss = {
          showNewTransactionSheet = false
          editingTransaction = null
        },
        onSaveTransaction = { title, amount, type, category, tag, timestamp, isSecret, note, receiptUri ->
          if (editingTransaction != null) {
            viewModel.updateTransaction(
              editingTransaction!!.copy(
                title = title,
                amount = amount,
                type = type,
                category = category,
                accountTag = tag,
                timestamp = timestamp,
                isSecret = isSecret,
                note = note,
                receiptUri = receiptUri
              )
            )
          } else {
            viewModel.addTransaction(
              title = title,
              amount = amount,
              type = type,
              category = category,
              accountTag = tag,
              timestamp = timestamp,
              isSecret = isSecret,
              note = note,
              receiptUri = receiptUri
            )
          }
          showNewTransactionSheet = false
          editingTransaction = null
        }
      )
    }

    // Modal New / Edit Goal Sheet
    if (showNewGoalSheet) {
      NewGoalSheet(
        editingGoal = editingGoal,
        categories = allCategories,
        onDismiss = {
          showNewGoalSheet = false
          editingGoal = null
        },
        onSave = { title, cadence, category, targetAmount ->
          if (editingGoal != null) {
            viewModel.updateGoal(
              editingGoal!!.copy(
                title = title,
                cadence = cadence,
                category = category,
                targetAmount = targetAmount
              )
            )
          } else {
            viewModel.addGoal(title, cadence, category, targetAmount)
          }
          showNewGoalSheet = false
          editingGoal = null
        }
      )
    }

    // Modal New / Edit Recurring Bill Sheet
    if (showNewBillSheet) {
      NewRecurringBillSheet(
        editingBill = editingBill,
        categories = allCategories,
        onDismiss = {
          showNewBillSheet = false
          editingBill = null
        },
        onSave = { title, amount, category, dueDay, cadence, tag, note, isAutoPay ->
          if (editingBill != null) {
            viewModel.updateRecurringBill(
              editingBill!!.copy(
                title = title,
                amount = amount,
                category = category,
                dueDayOfMonth = dueDay,
                cadence = cadence,
                accountTag = tag,
                note = note,
                isAutoPay = isAutoPay
              )
            )
          } else {
            viewModel.addRecurringBill(
              title = title,
              amount = amount,
              category = category,
              dueDayOfMonth = dueDay,
              cadence = cadence,
              accountTag = tag,
              note = note,
              isAutoPay = isAutoPay
            )
          }
          showNewBillSheet = false
          editingBill = null
        }
      )
    }
  }
}

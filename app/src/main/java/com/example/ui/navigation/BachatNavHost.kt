package com.example.ui.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.shadow
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
import com.example.data.model.Transaction
import com.example.ui.BachatUiEvent
import com.example.ui.BachatViewModel
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
                  onNavigateToGoals = { navController.navigate(Screen.AllGoals.route) },
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
                  onDeleteGoal = { goal -> viewModel.deleteGoal(goal) }
                )
              }
              1 -> {
                HistoryAnalyticsScreen(
                  selectedTab = selectedTab,
                  onTabSelect = { viewModel.setTab(it) },
                  analyticsState = analyticsState,
                  filteredTransactions = filteredTransactions,
                  filterState = filterState,
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
            onBackClick = { navController.popBackStack() },
            onNavigateToPinSetup = { navController.navigate(Screen.PinSetup.route) },
            onNavigateToAllGoals = { navController.navigate(Screen.AllGoals.route) },
            onSetThemeMode = { viewModel.setThemeMode(it) },
            onSetSecretLockEnabled = { viewModel.setSecretLockEnabled(it) },
            onSetUseBiometric = { viewModel.setUseBiometric(it) },
            onSetGoalAlertsEnabled = { viewModel.setGoalAlertsEnabled(it) },
            onSetShowGoalsOnDashboard = { viewModel.setShowGoalsOnDashboard(it) },
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

    // Overlapping Bottom Bar + Floating Action Button (FAB)
    if (isBottomNavVisible) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp,
            start = 24.dp,
            end = 24.dp
          )
      ) {
        // Bottom Bar Surface
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(12.dp, RoundedCornerShape(32.dp)),
          shape = RoundedCornerShape(32.dp),
          color = Color.White,
          tonalElevation = 6.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Home Item
            val isHome = pagerState.currentPage == 0
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                ) {
                  scope.launch { pagerState.animateScrollToPage(0) }
                }
                .padding(8.dp)
                .testTag("nav_home")
            ) {
              Icon(
                imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                contentDescription = "Home",
                tint = if (isHome) BachatInk else BachatTextSecondary,
                modifier = Modifier.size(24.dp)
              )
              Text(
                text = "Home",
                fontSize = 11.sp,
                fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
                color = if (isHome) BachatInk else BachatTextSecondary
              )
            }

            Spacer(modifier = Modifier.width(64.dp)) // Space for FAB in center

            // History Item
            val isHistory = pagerState.currentPage == 1
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                ) {
                  scope.launch { pagerState.animateScrollToPage(1) }
                }
                .padding(8.dp)
                .testTag("nav_history")
            ) {
              Icon(
                imageVector = if (isHistory) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                contentDescription = "History",
                tint = if (isHistory) BachatInk else BachatTextSecondary,
                modifier = Modifier.size(24.dp)
              )
              Text(
                text = "History",
                fontSize = 11.sp,
                fontWeight = if (isHistory) FontWeight.Bold else FontWeight.Medium,
                color = if (isHistory) BachatInk else BachatTextSecondary
              )
            }
          }
        }

        // Floating Action Button in Center
        FloatingActionButton(
          onClick = {
            editingTransaction = null
            showNewTransactionSheet = true
          },
          shape = CircleShape,
          containerColor = BachatInk,
          contentColor = Color.White,
          elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
          modifier = Modifier
            .size(64.dp)
            .align(Alignment.Center)
            .offset(y = (-14).dp)
            .testTag("fab_new_transaction")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Transaction",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
          )
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
  }
}

package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.ui.components.AppCard
import com.example.ui.components.DashboardGoalCard
import com.example.ui.components.GoalCardContent
import com.example.ui.theme.BachatTheme
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryItem
import com.example.data.model.Goal
import com.example.data.model.TimePeriod
import com.example.data.model.Transaction
import com.example.data.model.formatRupee
import com.example.ui.DashboardUiState
import com.example.ui.components.DonutChart
import com.example.ui.components.EmptyState
import com.example.ui.components.GoalCard
import com.example.ui.components.RowActionPopup
import com.example.ui.components.StatColumn
import com.example.ui.components.TransactionRow
import com.example.ui.theme.BachatAccentIndigo
import com.example.ui.theme.BachatAccentIndigoTint
import com.example.ui.theme.BachatDanger
import com.example.ui.theme.BachatDangerTint
import com.example.ui.theme.BachatDivider
import com.example.ui.theme.BachatInk
import com.example.ui.theme.BachatSuccess
import com.example.ui.theme.BachatSuccessTint
import com.example.ui.theme.BachatSurface
import com.example.ui.theme.BachatTextPrimary
import com.example.ui.theme.BachatTextSecondary

@Composable
fun DashboardScreen(
  uiState: DashboardUiState,
  showGoals: Boolean = true,
  onNavigateToGoals: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onNavigateToHistory: () -> Unit = {},
  onOpenNewTransaction: () -> Unit,
  onEditTransaction: (Transaction) -> Unit,
  onDeleteTransaction: (Transaction) -> Unit,
  onEditGoal: (Goal) -> Unit,
  onDeleteGoal: (Goal) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTxForAction by remember { mutableStateOf<Transaction?>(null) }
  var selectedGoalForAction by remember { mutableStateOf<Goal?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
      .padding(horizontal = 20.dp)
      .testTag("dashboard_screen"),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // Header: Dashboard Title + Settings Gear Icon
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Dashboard",
          fontSize = 26.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary,
          modifier = Modifier.testTag("dashboard_title")
        )

        IconButton(
          onClick = onNavigateToSettings,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(BachatSurface)
            .testTag("settings_button")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = BachatInk,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // Total Balance Card
    item {
      AppCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("total_balance_card")
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Total Balance",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = BachatTextSecondary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = formatRupee(uiState.totalBalance),
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BachatTextPrimary,
            modifier = Modifier.testTag("total_balance_text")
          )

          Spacer(modifier = Modifier.height(24.dp))

          // 3 Stat Columns (Today, Week, Month)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            StatColumn(
              icon = Icons.Default.TrendingDown,
              iconBgColor = BachatDangerTint,
              iconTint = BachatDanger,
              caption = "TODAY",
              value = formatRupee(uiState.todayExpense),
              valueColor = if (uiState.todayExpense > 0) BachatDanger else BachatTextPrimary
            )

            StatColumn(
              icon = Icons.Default.CalendarMonth,
              iconBgColor = BachatSuccessTint,
              iconTint = BachatSuccess,
              caption = "WEEK",
              value = formatRupee(uiState.weekExpense),
              valueColor = if (uiState.weekExpense > 0) BachatDanger else BachatTextPrimary
            )

            StatColumn(
              icon = Icons.Default.BarChart,
              iconBgColor = BachatSurface,
              iconTint = BachatTextSecondary,
              caption = "MONTH",
              value = formatRupee(uiState.monthExpense),
              valueColor = if (uiState.monthExpense > 0) BachatDanger else BachatTextPrimary
            )
          }
        }
      }
    }

    // Goals Section
    if (showGoals) {
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Goals",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BachatTextPrimary
          )

          Spacer(modifier = Modifier.height(10.dp))

          if (uiState.topGoal != null) {
            DashboardGoalCard(
              goal = uiState.topGoal,
              spentAmount = uiState.topGoalSpent,
              onNavigateToGoals = onNavigateToGoals,
              onClick = { selectedGoalForAction = uiState.topGoal }
            )
          } else {
            AppCard(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToGoals() }
                .testTag("dashboard_empty_goal_card")
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(CircleShape)
                      .background(BachatSurface),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.TrackChanges,
                      contentDescription = null,
                      tint = BachatInk,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = "No goal set",
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = BachatTextPrimary
                    )
                    Text(
                      text = "Set a monthly spending limit",
                      fontSize = 12.5.sp,
                      color = BachatTextSecondary
                    )
                  }
                }
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Add Goal",
                  tint = BachatInk,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    }

    // Today Transactions Section
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Today",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.todayTransactions.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onOpenNewTransaction() }
          ) {
            EmptyState(
              icon = Icons.Default.ReceiptLong,
              message = "No spending today — great job! 🎉",
              helperText = "Tap the + button to log an expense or income"
            )
          }
        }
      }
    }

    if (uiState.todayTransactions.isNotEmpty()) {
      items(uiState.todayTransactions) { transaction ->
        TransactionRow(
          transaction = transaction,
          onClick = { selectedTxForAction = transaction }
        )
        HorizontalDivider(color = BachatDivider, thickness = 0.6.dp)
      }
    }

    item {
      Spacer(modifier = Modifier.height(100.dp))
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

  // RowActionPopup for Goals
  selectedGoalForAction?.let { goal ->
    RowActionPopup(
      onDismiss = { selectedGoalForAction = null },
      onEdit = { onEditGoal(goal) },
      onDelete = { onDeleteGoal(goal) }
    )
  }
}

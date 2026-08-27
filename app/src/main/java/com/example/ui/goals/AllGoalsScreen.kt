package com.example.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Goal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.EmptyState
import com.example.ui.components.GoalCard
import com.example.ui.components.RowActionPopup
import com.example.ui.theme.BachatInk
import com.example.ui.theme.BachatOnColor
import com.example.ui.theme.BachatTextPrimary
import java.util.Calendar

@Composable
fun AllGoalsScreen(
  goals: List<Goal>,
  transactions: List<Transaction>,
  onBackClick: () -> Unit,
  onOpenNewGoal: () -> Unit,
  onEditGoal: (Goal) -> Unit,
  onDeleteGoal: (Goal) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedGoalForAction by remember { mutableStateOf<Goal?>(null) }

  val startOfMonth = Calendar.getInstance().apply {
    set(Calendar.DAY_OF_MONTH, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
  }.timeInMillis

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
          modifier = Modifier
            .align(Alignment.CenterStart)
            .testTag("goals_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = BachatTextPrimary
          )
        }

        Text(
          text = "All Goals",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary,
          modifier = Modifier.align(Alignment.Center)
        )
      }
    },
    floatingActionButton = {
      Button(
        onClick = onOpenNewGoal,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = BachatInk,
          contentColor = BachatOnColor
        ),
        modifier = Modifier
          .height(52.dp)
          .padding(horizontal = 24.dp)
          .testTag("new_goal_fab")
      ) {
        androidx.compose.foundation.layout.Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "New Goal",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    },
    floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center,
    containerColor = Color.White,
    modifier = modifier.testTag("all_goals_screen")
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentAlignment = Alignment.TopCenter
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 680.dp)
          .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          Spacer(modifier = Modifier.height(4.dp))
        }

        if (goals.isEmpty()) {
          item {
            EmptyState(
              icon = Icons.Default.TrackChanges,
              message = "No goals yet",
              helperText = "Tap + New Goal below to set your first monthly spending limit"
            )
          }
        } else {
          items(goals) { goal ->
            val spent = transactions.filter {
              !it.isSecret &&
              it.type == TransactionType.EXPENSE &&
              it.category.equals(goal.category, ignoreCase = true) &&
              it.timestamp >= startOfMonth
            }.sumOf { it.amount }

            GoalCard(
              goal = goal,
              spentAmount = spent,
              onClick = { selectedGoalForAction = goal }
            )
          }
        }

        item {
          Spacer(modifier = Modifier.height(100.dp))
        }
      }
    }
  }

  selectedGoalForAction?.let { goal ->
    RowActionPopup(
      onDismiss = { selectedGoalForAction = null },
      onEdit = { onEditGoal(goal) },
      onDelete = { onDeleteGoal(goal) }
    )
  }
}

package com.example.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import com.example.data.model.CategoryItem
import com.example.data.model.DEFAULT_CATEGORIES
import com.example.data.model.Goal
import com.example.data.model.TransactionType
import com.example.ui.components.BachatFilterChip
import com.example.ui.components.SegmentedToggle
import com.example.ui.theme.BachatDanger
import com.example.ui.theme.BachatDangerTint
import com.example.ui.theme.BachatInk
import com.example.ui.theme.BachatOnColor
import com.example.ui.theme.BachatSurface
import com.example.ui.theme.BachatTextPrimary
import com.example.ui.theme.BachatTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGoalSheet(
  editingGoal: Goal? = null,
  categories: List<CategoryItem> = emptyList(),
  onDismiss: () -> Unit,
  onSave: (title: String, cadence: String, category: String, targetAmount: Double) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val keyboardController = LocalSoftwareKeyboardController.current
  val isEditing = editingGoal != null

  val effectiveCategories = if (categories.isNotEmpty()) categories else DEFAULT_CATEGORIES
  val expenseCategories = effectiveCategories.filter { it.type == TransactionType.EXPENSE }.ifEmpty { effectiveCategories }

  var title by remember { mutableStateOf(editingGoal?.title ?: "Food Budget") }
  var category by remember {
    mutableStateOf(editingGoal?.category ?: expenseCategories.firstOrNull()?.name ?: "Food")
  }
  var cadence by remember { mutableStateOf(editingGoal?.cadence ?: "monthly") }
  var amountText by remember {
    mutableStateOf(if (editingGoal != null) editingGoal.targetAmount.toString().removeSuffix(".0") else "5000")
  }

  fun handleDismiss() {
    keyboardController?.hide()
    onDismiss()
  }

  ModalBottomSheet(
    onDismissRequest = { handleDismiss() },
    sheetState = sheetState,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor = Color.White,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 12.dp, bottom = 6.dp)
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
        .testTag("new_goal_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isEditing) "Edit Goal" else "New Goal",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary
        )
        IconButton(onClick = { handleDismiss() }, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = BachatTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "GOAL TITLE",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        placeholder = { Text("e.g. Dining Out Budget") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = BachatSurface,
          unfocusedContainerColor = BachatSurface,
          focusedBorderColor = BachatInk,
          unfocusedBorderColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth().testTag("goal_title_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "TARGET AMOUNT (₹)",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = amountText,
        onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
        placeholder = { Text("e.g. 5000") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = BachatSurface,
          unfocusedContainerColor = BachatSurface,
          focusedBorderColor = BachatInk,
          unfocusedBorderColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth().testTag("goal_amount_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "CADENCE",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      SegmentedToggle(
        options = listOf("Weekly", "Monthly"),
        selectedIndex = if (cadence.equals("weekly", ignoreCase = true)) 0 else 1,
        onSelect = { index ->
          cadence = if (index == 0) "weekly" else "monthly"
        }
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "CATEGORY",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        expenseCategories.forEach { cat ->
          BachatFilterChip(
            label = cat.name,
            selected = category.equals(cat.name, ignoreCase = true),
            onClick = { category = cat.name }
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = { handleDismiss() },
          shape = RoundedCornerShape(999.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BachatDangerTint,
            contentColor = BachatDanger
          ),
          modifier = Modifier.weight(0.35f).height(46.dp)
        ) {
          Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = {
            keyboardController?.hide()
            val target = amountText.toDoubleOrNull() ?: 1000.0
            onSave(title, cadence, category, target)
          },
          shape = RoundedCornerShape(999.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BachatInk,
            contentColor = BachatOnColor
          ),
          modifier = Modifier.weight(0.65f).height(46.dp).testTag("save_goal_button")
        ) {
          Text(if (isEditing) "Update Goal" else "Save Goal", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

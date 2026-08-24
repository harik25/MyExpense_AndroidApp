package com.example.ui.transaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.data.model.CategoryItem
import com.example.data.model.ExpenseCategory
import com.example.data.model.IncomeCategory
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconButton
import com.example.ui.components.SegmentedToggle
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.BachatDanger
import com.example.ui.theme.BachatDangerTint
import com.example.ui.theme.BachatDivider
import com.example.ui.theme.BachatInk
import com.example.ui.theme.BachatOnColor
import com.example.ui.theme.BachatSuccess
import com.example.ui.theme.BachatSuccessTint
import com.example.ui.theme.BachatSurface
import com.example.ui.theme.BachatTextPrimary
import com.example.ui.theme.BachatTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTransactionSheet(
  editingTransaction: Transaction? = null,
  categories: List<CategoryItem> = emptyList(),
  onDismiss: () -> Unit,
  onSaveTransaction: (
    title: String,
    amount: Double,
    type: TransactionType,
    category: String,
    accountTag: String,
    timestamp: Long,
    isSecret: Boolean,
    note: String,
    receiptUri: String?
  ) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current

  fun handleDismiss() {
    keyboardController?.hide()
    onDismiss()
  }

  val isEditing = editingTransaction != null
  var transactionType by remember {
    mutableStateOf(editingTransaction?.type ?: TransactionType.EXPENSE)
  }
  var selectedCategory by remember {
    mutableStateOf(editingTransaction?.category ?: "Food")
  }
  var amountInput by remember {
    mutableStateOf(if (editingTransaction != null) editingTransaction.amount.toString().removeSuffix(".0") else "")
  }
  var selectedAccountTag by remember {
    mutableStateOf(editingTransaction?.accountTag ?: "UPI")
  }
  var note by remember {
    mutableStateOf(editingTransaction?.note ?: "")
  }
  var isSecret by remember {
    mutableStateOf(editingTransaction?.isSecret ?: false)
  }
  var receiptUriString by remember {
    mutableStateOf(editingTransaction?.receiptUri)
  }

  var selectedCalendar by remember {
    mutableStateOf(Calendar.getInstance().apply {
      if (editingTransaction != null) {
        timeInMillis = editingTransaction.timestamp
      }
    })
  }

  // Photo / Receipt Picker Launcher
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    receiptUriString = uri?.toString()
  }

  val isExpense = transactionType == TransactionType.EXPENSE
  val activeColor = if (isExpense) BachatDanger else BachatSuccess
  val activeTint = if (isExpense) BachatDangerTint else BachatSuccessTint

  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
  val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

  ModalBottomSheet(
    onDismissRequest = { handleDismiss() },
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
        .padding(horizontal = 20.dp, vertical = 6.dp)
        .testTag("new_transaction_sheet")
    ) {
      // Header: Title + Close Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = if (isEditing) "Edit Transaction" else "New Transaction",
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary
        )
        IconButton(
          onClick = { handleDismiss() },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = BachatTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 1. Expense / Income Segmented Toggle
      SegmentedToggle(
        options = listOf("Expense", "Income"),
        selectedIndex = if (isExpense) 0 else 1,
        onSelect = { index ->
          if (index == 0) {
            transactionType = TransactionType.EXPENSE
            if (IncomeCategory.values().any { it.displayName == selectedCategory }) {
              selectedCategory = "Food"
            }
          } else {
            transactionType = TransactionType.INCOME
            if (ExpenseCategory.values().any { it.displayName == selectedCategory }) {
              selectedCategory = "Salary"
            }
          }
        },
        selectedTextColor = if (isExpense) BachatDanger else BachatSuccess
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Large Amount Input Box with ₹ Prefix
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(BachatSurface)
          .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "₹",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = activeColor
          )
          Spacer(modifier = Modifier.width(6.dp))

          OutlinedTextField(
            value = amountInput,
            onValueChange = { input ->
              if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                amountInput = input
              }
            },
            placeholder = {
              Text(
                text = "0",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BachatTextSecondary.copy(alpha = 0.5f)
              )
            },
            textStyle = TextStyle(
              fontSize = 30.sp,
              fontWeight = FontWeight.ExtraBold,
              color = activeColor,
              textAlign = TextAlign.Start
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              focusedBorderColor = Color.Transparent,
              unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
              .weight(1f, fill = false)
              .testTag("amount_input_field")
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 3. Category Selector with Eyebrow Label & Color System
      Text(
        text = "SELECT CATEGORY",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))

      val expenseCategoryList = if (categories.isNotEmpty()) categories.filter { it.type == TransactionType.EXPENSE } else emptyList()
      val incomeCategoryList = if (categories.isNotEmpty()) categories.filter { it.type == TransactionType.INCOME } else emptyList()

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (isExpense) {
          if (expenseCategoryList.isNotEmpty()) {
            expenseCategoryList.forEach { cat ->
              CategoryIconButton(
                label = cat.name,
                icon = getCategoryIcon(cat.name, cat.iconName),
                selected = selectedCategory.equals(cat.name, ignoreCase = true),
                onClick = { selectedCategory = cat.name },
                baseColor = BachatDanger,
                tintColor = BachatDangerTint
              )
            }
          } else {
            ExpenseCategory.values().forEach { cat ->
              CategoryIconButton(
                label = cat.shortName,
                icon = getCategoryIcon(cat.displayName),
                selected = selectedCategory == cat.displayName,
                onClick = { selectedCategory = cat.displayName },
                baseColor = BachatDanger,
                tintColor = BachatDangerTint
              )
            }
          }
        } else {
          if (incomeCategoryList.isNotEmpty()) {
            incomeCategoryList.forEach { cat ->
              CategoryIconButton(
                label = cat.name,
                icon = getCategoryIcon(cat.name, cat.iconName),
                selected = selectedCategory.equals(cat.name, ignoreCase = true),
                onClick = { selectedCategory = cat.name },
                baseColor = BachatSuccess,
                tintColor = BachatSuccessTint
              )
            }
          } else {
            IncomeCategory.values().forEach { cat ->
              CategoryIconButton(
                label = cat.displayName,
                icon = getCategoryIcon(cat.displayName),
                selected = selectedCategory == cat.displayName,
                onClick = { selectedCategory = cat.displayName },
                baseColor = BachatSuccess,
                tintColor = BachatSuccessTint
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4. Date & Time Picker (Un-merged)
      Text(
        text = "DATE & TIME",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(BachatSurface)
          .clickable {
            val currentYear = selectedCalendar.get(Calendar.YEAR)
            val currentMonth = selectedCalendar.get(Calendar.MONTH)
            val currentDay = selectedCalendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
              context,
              { _, y, m, d ->
                val hour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
                val min = selectedCalendar.get(Calendar.MINUTE)
                TimePickerDialog(
                  context,
                  { _, h, mn ->
                    val newCal = Calendar.getInstance().apply {
                      set(y, m, d, h, mn, 0)
                    }
                    selectedCalendar = newCal
                  },
                  hour,
                  min,
                  false
                ).show()
              },
              currentYear,
              currentMonth,
              currentDay
            ).show()
          }
          .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = BachatTextPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${dateFormat.format(selectedCalendar.time)}, ${timeFormat.format(selectedCalendar.time)}",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = BachatTextPrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 5. Account Tag Selector (Un-merged)
      Text(
        text = "ACCOUNT TAG",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        listOf("UPI", "Bank", "Cash").forEach { tag ->
          val isSelected = selectedAccountTag == tag
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .background(if (isSelected) BachatInk else BachatSurface)
              .clickable { selectedAccountTag = tag }
              .padding(vertical = 10.dp)
              .testTag("tag_$tag"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = tag,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) Color.White else BachatTextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 6. Note Field (Un-merged)
      Text(
        text = "NOTE (OPTIONAL)",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        placeholder = { Text("What was this for?", fontSize = 12.5.sp, color = BachatTextSecondary) },
        singleLine = true,
        textStyle = TextStyle(fontSize = 12.5.sp, color = BachatTextPrimary),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = BachatSurface,
          unfocusedContainerColor = BachatSurface,
          focusedBorderColor = BachatInk,
          unfocusedBorderColor = Color.Transparent
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
          .testTag("note_input")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Receipt Attachment Row
      if (!receiptUriString.isNullOrBlank()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BachatSurface)
            .padding(horizontal = 12.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              AsyncImage(
                model = receiptUriString,
                contentDescription = "Receipt thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Receipt attached", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = BachatTextPrimary)
            }
            IconButton(onClick = { receiptUriString = null }, modifier = Modifier.size(24.dp)) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Remove receipt", tint = BachatDanger, modifier = Modifier.size(16.dp))
            }
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BachatSurface)
            .clickable { photoPickerLauncher.launch("image/*") }
            .padding(horizontal = 12.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AddPhotoAlternate,
              contentDescription = "Add Receipt",
              tint = BachatTextSecondary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Attach Receipt (Optional)",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = BachatTextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "OPTIONS",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))

      // 6. Secret Switch Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(BachatSurface)
          .clickable { isSecret = !isSecret }
          .padding(horizontal = 14.dp, vertical = 10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text("Mark Secret", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BachatTextPrimary)
            Text("Hide from normal totals and lock with PIN", fontSize = 11.sp, color = BachatTextSecondary)
          }
          Switch(
            checked = isSecret,
            onCheckedChange = { isSecret = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = BachatInk,
              uncheckedThumbColor = Color.White,
              uncheckedTrackColor = BachatDivider
            ),
            modifier = Modifier.size(width = 34.dp, height = 22.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 7. Footer Action Buttons: Cancel & Save / Update
      val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
      val isSaveEnabled = parsedAmount > 0.0

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Cancel Button
        Button(
          onClick = { handleDismiss() },
          shape = RoundedCornerShape(999.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BachatDanger,
            contentColor = Color.White
          ),
          modifier = Modifier
            .weight(0.35f)
            .height(46.dp)
            .testTag("cancel_button")
        ) {
          Text(
            text = "Cancel",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Save / Update Button
        Button(
          onClick = {
            if (isSaveEnabled) {
              keyboardController?.hide()
              onSaveTransaction(
                if (note.isNotBlank()) note else selectedCategory,
                parsedAmount,
                transactionType,
                selectedCategory,
                selectedAccountTag,
                selectedCalendar.timeInMillis,
                isSecret,
                note,
                receiptUriString
              )
            }
          },
          enabled = isSaveEnabled,
          shape = RoundedCornerShape(999.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BachatInk,
            contentColor = BachatOnColor,
            disabledContainerColor = BachatSurface,
            disabledContentColor = BachatTextSecondary
          ),
          modifier = Modifier
            .weight(0.65f)
            .height(46.dp)
            .testTag("save_transaction_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isEditing) "Update" else "Save",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

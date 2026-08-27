package com.example.ui.bills

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryItem
import com.example.data.model.DEFAULT_CATEGORIES
import com.example.data.model.RecurringBill
import com.example.data.model.TransactionType
import com.example.data.model.formatRupee
import com.example.ui.components.AppCard
import com.example.ui.components.BachatFilterChip
import com.example.ui.components.EmptyState
import com.example.ui.components.RecurringBillRowItem
import com.example.ui.components.RowActionPopup
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.BachatAccentIndigo
import com.example.ui.theme.BachatAccentIndigoTint
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
import java.util.Calendar
import java.util.Locale

@Composable
fun RecurringBillsScreen(
  bills: List<RecurringBill>,
  categories: List<CategoryItem>,
  onBackClick: () -> Unit,
  onOpenNewBill: () -> Unit,
  onPayBill: (RecurringBill) -> Unit,
  onEditBill: (RecurringBill) -> Unit,
  onDeleteBill: (RecurringBill) -> Unit,
  modifier: Modifier = Modifier
) {
  val now = Calendar.getInstance()
  val currentMonthKey = String.format(
    Locale.US,
    "%d-%02d",
    now.get(Calendar.YEAR),
    now.get(Calendar.MONTH) + 1
  )

  var selectedBillForAction by remember { mutableStateOf<RecurringBill?>(null) }

  val totalMonthlyCommitment = bills.filter { !it.isPaused }.sumOf { it.amount }
  val totalPaidThisMonth = bills.filter { !it.isPaused && it.isPaidForCurrentMonth(currentMonthKey) }.sumOf { it.amount }
  val totalPendingThisMonth = (totalMonthlyCommitment - totalPaidThisMonth).coerceAtLeast(0.0)

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
          modifier = Modifier.align(Alignment.CenterStart).testTag("bills_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = BachatTextPrimary
          )
        }

        Text(
          text = "Recurring Bills",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary,
          modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
          onClick = onOpenNewBill,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(BachatInk)
            .align(Alignment.CenterEnd)
            .testTag("add_bill_top_button")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Bill",
            tint = BachatOnColor,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    },
    floatingActionButton = {
      Button(
        onClick = onOpenNewBill,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = BachatInk,
          contentColor = Color.White
        ),
        modifier = Modifier
          .height(48.dp)
          .padding(horizontal = 4.dp)
          .testTag("fab_add_bill")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Add Recurring Bill", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    },
    containerColor = Color.White,
    modifier = modifier.testTag("recurring_bills_screen")
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        item {
          Spacer(modifier = Modifier.height(4.dp))
          // Summary Card
          AppCard(modifier = Modifier.fillMaxWidth().testTag("bills_summary_card")) {
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(BachatAccentIndigoTint),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.EventRepeat,
                      contentDescription = null,
                      tint = BachatAccentIndigo,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Monthly Subscriptions & Bills",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BachatTextPrimary
                  )
                }
                Text(
                  text = "${bills.count { it.isPaidForCurrentMonth(currentMonthKey) }}/${bills.size} Paid",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = BachatTextSecondary
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = "TOTAL COMMITTED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BachatTextSecondary)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(text = formatRupee(totalMonthlyCommitment), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = BachatTextPrimary)
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "PAID THIS MONTH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BachatTextSecondary)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(text = formatRupee(totalPaidThisMonth), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = BachatSuccess)
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                  Text(text = "PENDING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BachatTextSecondary)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(text = formatRupee(totalPendingThisMonth), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (totalPendingThisMonth > 0) BachatDanger else BachatTextSecondary)
                }
              }
            }
          }
        }

        if (bills.isEmpty()) {
          item {
            EmptyState(
              icon = Icons.Default.ReceiptLong,
              message = "No recurring bills tracked yet",
              helperText = "Add rent, WiFi, subscriptions or gym fees to never miss a due date"
            )
          }
        } else {
          item {
            Text(
              text = "Active Bills (${bills.size})",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = BachatTextPrimary
            )
          }

          items(bills) { bill ->
            RecurringBillRowItem(
              bill = bill,
              currentMonthKey = currentMonthKey,
              onPayClick = { onPayBill(bill) },
              onEditClick = { selectedBillForAction = bill }
            )
          }
        }

        item {
          Spacer(modifier = Modifier.height(80.dp))
        }
      }
    }
  }

  selectedBillForAction?.let { bill ->
    RowActionPopup(
      onDismiss = { selectedBillForAction = null },
      onEdit = { onEditBill(bill) },
      onDelete = { onDeleteBill(bill) }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRecurringBillSheet(
  editingBill: RecurringBill? = null,
  categories: List<CategoryItem>,
  onDismiss: () -> Unit,
  onSave: (
    title: String,
    amount: Double,
    category: String,
    dueDayOfMonth: Int,
    cadence: String,
    accountTag: String,
    note: String,
    isAutoPay: Boolean
  ) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val effectiveCategories = if (categories.isNotEmpty()) categories else DEFAULT_CATEGORIES
  val availableCategories = effectiveCategories.filter { it.type == TransactionType.EXPENSE }.map { it.name }.ifEmpty { effectiveCategories.map { it.name } }

  var title by remember { mutableStateOf(editingBill?.title ?: "") }
  var amountText by remember { mutableStateOf(editingBill?.let { "%.0f".format(it.amount) } ?: "") }
  var selectedCategory by remember {
    mutableStateOf(editingBill?.category ?: availableCategories.firstOrNull() ?: "Housing")
  }
  var dueDay by remember { mutableStateOf(editingBill?.dueDayOfMonth?.toString() ?: "5") }
  var cadence by remember { mutableStateOf(editingBill?.cadence ?: "Monthly") }
  var accountTag by remember { mutableStateOf(editingBill?.accountTag ?: "UPI") }
  var note by remember { mutableStateOf(editingBill?.note ?: "") }
  var isAutoPay by remember { mutableStateOf(editingBill?.isAutoPay ?: false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color.White,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .testTag("new_recurring_bill_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (editingBill != null) "Edit Recurring Bill" else "Add Recurring Bill",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = BachatTextSecondary)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Bill Title
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Bill / Subscription Name (e.g. Netflix, Rent, WiFi)") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BachatInk,
          unfocusedBorderColor = BachatDivider
        ),
        modifier = Modifier.fillMaxWidth().testTag("bill_title_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Recurrence Frequency / Cadence (Once, Weekly, Monthly, Yearly)
      Text(
        text = "Recurrence Frequency",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      val cadenceOptions = listOf("Once", "Weekly", "Monthly", "Yearly")
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        cadenceOptions.forEach { opt ->
          BachatFilterChip(
            label = opt,
            selected = cadence.equals(opt, ignoreCase = true),
            onClick = { cadence = opt }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Amount & Due Day Row
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = amountText,
          onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) amountText = it },
          label = { Text("Amount (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BachatInk,
            unfocusedBorderColor = BachatDivider
          ),
          modifier = Modifier.weight(1.2f).testTag("bill_amount_input")
        )

        val dueDayLabel = when (cadence) {
          "Weekly" -> "Day (1-7)"
          "Once" -> "Day (1-31)"
          else -> "Day (1-31)"
        }

        OutlinedTextField(
          value = dueDay,
          onValueChange = {
            val num = it.toIntOrNull()
            val maxDay = if (cadence == "Weekly") 7 else 31
            if (it.isEmpty() || (num != null && num in 1..maxDay)) {
              dueDay = it
            }
          },
          label = { Text(dueDayLabel) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BachatInk,
            unfocusedBorderColor = BachatDivider
          ),
          modifier = Modifier.weight(1f).testTag("bill_dueday_input")
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Category Chips
      Text(
        text = "Category",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        availableCategories.forEach { cat ->
          BachatFilterChip(
            label = cat,
            selected = selectedCategory.equals(cat, ignoreCase = true),
            onClick = { selectedCategory = cat }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Payment Account Tag
      Text(
        text = "Payment Method",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("UPI", "Bank", "Cash").forEach { tag ->
          BachatFilterChip(
            label = tag,
            selected = accountTag == tag,
            onClick = { accountTag = tag }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Auto-pay toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(text = "Auto-debit / Auto-pay", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BachatTextPrimary)
          Text(text = "Mark as automatically paid on due date", fontSize = 12.sp, color = BachatTextSecondary)
        }
        Switch(
          checked = isAutoPay,
          onCheckedChange = { isAutoPay = it },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = BachatInk
          )
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Save Button
      Button(
        onClick = {
          val amt = amountText.toDoubleOrNull() ?: 0.0
          val day = dueDay.toIntOrNull() ?: 1
          if (title.isNotBlank() && amt > 0) {
            onSave(title.trim(), amt, selectedCategory, day, cadence, accountTag, note.trim(), isAutoPay)
          }
        },
        enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = BachatInk,
          contentColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("save_bill_button")
      ) {
        Text(
          text = if (editingBill != null) "Update Recurring Bill" else "Add Recurring Bill",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

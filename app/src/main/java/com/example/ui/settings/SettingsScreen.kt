package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.local.AppSettings
import com.example.data.model.AppThemeMode
import com.example.data.model.CategoryItem
import com.example.data.model.TransactionType
import com.example.ui.components.BadgePill
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.SegmentedToggle
import com.example.ui.components.ToggleSwitchRow
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.BachatAccentIndigo
import com.example.ui.theme.BachatAccentIndigoTint
import com.example.ui.theme.BachatDanger
import com.example.ui.theme.BachatDangerTint
import com.example.ui.theme.BachatInk
import com.example.ui.theme.BachatSuccess
import com.example.ui.theme.BachatSuccessTint
import com.example.ui.theme.BachatSurface
import com.example.ui.theme.BachatTextPrimary
import com.example.ui.theme.BachatTextSecondary
import java.io.File
import java.io.FileOutputStream

@Composable
fun SettingsScreen(
  appSettings: AppSettings,
  categories: List<CategoryItem> = emptyList(),
  bills: List<com.example.data.model.RecurringBill> = emptyList(),
  onBackClick: () -> Unit,
  onNavigateToPinSetup: () -> Unit,
  onNavigateToAllGoals: () -> Unit,
  onNavigateToBills: () -> Unit = {},
  onSetThemeMode: (AppThemeMode) -> Unit,
  onSetSecretLockEnabled: (Boolean) -> Unit,
  onSetUseBiometric: (Boolean) -> Unit,
  onSetGoalAlertsEnabled: (Boolean) -> Unit,
  onSetShowGoalsOnDashboard: (Boolean) -> Unit,
  onSetShowBillsOnDashboard: (Boolean) -> Unit = {},
  onAddCategory: (name: String, type: TransactionType, iconName: String, colorHex: Long) -> Unit = { _, _, _, _ -> },
  onUpdateCategory: (CategoryItem) -> Unit = {},
  onDeleteCategory: (CategoryItem) -> Unit = {},
  onResetCategories: () -> Unit = {},
  onExportJson: () -> String,
  onImportJson: (String, () -> Unit) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showImportConfirmDialog by remember { mutableStateOf(false) }
  var pendingImportJsonContent by remember { mutableStateOf<String?>(null) }
  var showCategoryManagementSheet by remember { mutableStateOf(false) }

  // JSON File Picker Launcher
  val jsonPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      try {
        val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
          reader.readText()
        }
        if (content != null) {
          pendingImportJsonContent = content
          showImportConfirmDialog = true
        }
      } catch (e: Exception) {
        Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun handleExportJson() {
    try {
      val json = onExportJson()
      val cacheDir = File(context.cacheDir, "backups").apply { mkdirs() }
      val file = File(cacheDir, "ExpenseApp_Backup_${System.currentTimeMillis()}.json")
      FileOutputStream(file).use { it.write(json.toByteArray()) }

      val fileUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(shareIntent, "Export ExpenseApp Backup"))
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
          modifier = Modifier
            .align(Alignment.CenterStart)
            .testTag("settings_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = BachatTextPrimary
          )
        }

        Text(
          text = "Settings",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary,
          modifier = Modifier.align(Alignment.Center)
        )
      }
    },
    containerColor = Color.White,
    modifier = modifier.testTag("settings_screen")
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
      }

      // SECTION 1: DATA BACKUP
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "DATA BACKUP",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color = BachatTextSecondary
          )
          Spacer(modifier = Modifier.height(10.dp))

          // Export JSON
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(18.dp))
              .background(BachatSuccessTint)
              .clickable { handleExportJson() }
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .testTag("export_json_row"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = BachatSuccess,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Export JSON",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BachatTextPrimary
              )
              Text(
                text = "Backup all transactions & goals",
                fontSize = 12.5.sp,
                color = BachatTextSecondary
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Import JSON
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(18.dp))
              .background(BachatDangerTint)
              .clickable { jsonPickerLauncher.launch("application/json") }
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .testTag("import_json_row"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = BachatDanger,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Import JSON",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BachatTextPrimary
              )
              Text(
                text = "Restore or replace app database from backup",
                fontSize = 12.5.sp,
                color = BachatTextSecondary
              )
            }
          }
        }
      }

      // SECTION 2: CATEGORY CONTROLS
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "CATEGORIES & ICONS",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.5.sp,
              color = BachatTextSecondary
            )
            BadgePill(
              text = "${categories.size} Active",
              backgroundColor = BachatAccentIndigoTint,
              textColor = BachatAccentIndigo
            )
          }
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(20.dp))
              .background(BachatAccentIndigoTint)
              .clickable { showCategoryManagementSheet = true }
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .testTag("manage_categories_settings_card"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = BachatAccentIndigo,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Manage Categories",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BachatTextPrimary
              )
              Text(
                text = "Add, remove, change icon & colors for expenses and income",
                fontSize = 12.5.sp,
                color = BachatTextSecondary
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
              contentDescription = "Open",
              tint = BachatAccentIndigo,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // SECTION 3: SECURITY
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "SECURITY",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color = BachatTextSecondary
          )
          Spacer(modifier = Modifier.height(10.dp))

          ToggleSwitchRow(
            title = "Lock Secret Transactions",
            subtitle = "Require PIN or biometric to view them",
            checked = appSettings.secretLockEnabled,
            onCheckedChange = { checked ->
              if (checked && appSettings.pinHash.isEmpty()) {
                onNavigateToPinSetup()
              } else {
                onSetSecretLockEnabled(checked)
              }
            }
          )

          if (appSettings.pinHash.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))

            ToggleSwitchRow(
              title = "Use Biometric Instead",
              subtitle = "Use fingerprint or face unlock",
              checked = appSettings.useBiometric,
              onCheckedChange = { onSetUseBiometric(it) }
            )
          }
        }
      }

      // SECTION 4: GOALS
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "GOALS",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color = BachatTextSecondary
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(BachatSurface)
              .clickable { onNavigateToAllGoals() }
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .testTag("manage_goals_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = BachatInk,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Manage Goals",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = BachatTextPrimary
              )
            }

            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
              contentDescription = null,
              tint = BachatTextSecondary,
              modifier = Modifier.size(14.dp)
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          ToggleSwitchRow(
            title = "Show Goals on Dashboard",
            subtitle = "Display your monthly spending limits on the home screen",
            checked = appSettings.showGoalsOnDashboard,
            onCheckedChange = { onSetShowGoalsOnDashboard(it) },
            modifier = Modifier.testTag("show_goals_toggle")
          )
        }
      }

      // SECTION 5: BILLS & SUBSCRIPTIONS
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "BILLS & SUBSCRIPTIONS",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.5.sp,
              color = BachatTextSecondary
            )
            if (bills.isNotEmpty()) {
              BadgePill(
                text = "${bills.count { !it.isPaused }} Active",
                backgroundColor = BachatAccentIndigoTint,
                textColor = BachatAccentIndigo
              )
            }
          }
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(BachatSurface)
              .clickable { onNavigateToBills() }
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .testTag("manage_bills_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = BachatInk,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Manage Recurring Bills",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = BachatTextPrimary
                )
                Text(
                  text = "Add, edit, or adjust Once, Weekly & Monthly bills",
                  fontSize = 12.sp,
                  color = BachatTextSecondary
                )
              }
            }

            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
              contentDescription = null,
              tint = BachatTextSecondary,
              modifier = Modifier.size(14.dp)
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          ToggleSwitchRow(
            title = "Show Due Bills on Dashboard",
            subtitle = "Only displays when due in 2 days or overdue",
            checked = appSettings.showBillsOnDashboard,
            onCheckedChange = { onSetShowBillsOnDashboard(it) },
            modifier = Modifier.testTag("show_bills_toggle")
          )
        }
      }

      // SECTION 6: NOTIFICATIONS
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "NOTIFICATIONS",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color = BachatTextSecondary
          )
          Spacer(modifier = Modifier.height(10.dp))

          ToggleSwitchRow(
            title = "Goal limit & overspend alerts",
            subtitle = "Notify me when a category budget or goal reaches limit",
            checked = appSettings.goalAlertsEnabled,
            onCheckedChange = { onSetGoalAlertsEnabled(it) }
          )
        }
      }

      item {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            text = "ExpenseApp v2.0",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = BachatTextSecondary
          )
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}

  // ConfirmDialog for JSON Import
  if (showImportConfirmDialog && pendingImportJsonContent != null) {
    ConfirmDialog(
      title = "Replace all data?",
      message = "This will overwrite your existing transactions and goals. This can't be undone.",
      confirmText = "Replace",
      cancelText = "Cancel",
      onConfirm = {
        val content = pendingImportJsonContent ?: return@ConfirmDialog
        onImportJson(content) {
          Toast.makeText(context, "Data successfully restored!", Toast.LENGTH_SHORT).show()
        }
        pendingImportJsonContent = null
      },
      onDismiss = {
        showImportConfirmDialog = false
        pendingImportJsonContent = null
      }
    )
  }

  // Category Management BottomSheet
  if (showCategoryManagementSheet) {
    CategoryManagementSheet(
      categories = categories,
      onDismiss = { showCategoryManagementSheet = false },
      onAddCategory = onAddCategory,
      onUpdateCategory = onUpdateCategory,
      onDeleteCategory = onDeleteCategory,
      onResetDefaults = onResetCategories
    )
  }
}

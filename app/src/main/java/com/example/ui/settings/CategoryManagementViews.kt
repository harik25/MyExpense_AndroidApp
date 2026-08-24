package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryItem
import com.example.data.model.TransactionType
import com.example.ui.components.BadgePill
import com.example.ui.components.CATEGORY_ICON_MAP
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.SegmentedToggle
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.BachatDanger
import com.example.ui.theme.BachatDangerTint
import com.example.ui.theme.BachatInk
import com.example.ui.theme.BachatOnColor
import com.example.ui.theme.BachatSurface
import com.example.ui.theme.BachatTextPrimary
import com.example.ui.theme.BachatTextSecondary

val PRESET_CATEGORY_COLORS = listOf(
  0xFF4338CA, // Indigo
  0xFF10B981, // Emerald Green
  0xFFEF4444, // Red / Danger
  0xFFF59E0B, // Amber
  0xFF3B82F6, // Blue
  0xFF8B5CF6, // Purple
  0xFFEC4899, // Pink
  0xFF06B6D4, // Cyan
  0xFFF97316, // Orange
  0xFF14B8A6  // Teal
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementSheet(
  categories: List<CategoryItem>,
  onDismiss: () -> Unit,
  onAddCategory: (name: String, type: TransactionType, iconName: String, colorHex: Long) -> Unit,
  onUpdateCategory: (CategoryItem) -> Unit,
  onDeleteCategory: (CategoryItem) -> Unit,
  onResetDefaults: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedTab by remember { mutableStateOf(0) } // 0: Expense, 1: Income
  var showAddEditSheet by remember { mutableStateOf(false) }
  var editingCategory by remember { mutableStateOf<CategoryItem?>(null) }
  var categoryToDelete by remember { mutableStateOf<CategoryItem?>(null) }
  var showResetConfirm by remember { mutableStateOf(false) }

  val filteredCategories = categories.filter {
    if (selectedTab == 0) it.type == TransactionType.EXPENSE else it.type == TransactionType.INCOME
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
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
        .padding(horizontal = 24.dp, vertical = 8.dp)
        .testTag("category_management_sheet")
    ) {
      // Header Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Manage Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BachatTextPrimary
          )
          Text(
            text = "Customize names, icons and color tags",
            fontSize = 12.5.sp,
            color = BachatTextSecondary
          )
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = BachatTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Expense / Income Segmented Toggle
      SegmentedToggle(
        options = listOf(
          "Expenses (${categories.count { it.type == TransactionType.EXPENSE }})",
          "Income (${categories.count { it.type == TransactionType.INCOME }})"
        ),
        selectedIndex = selectedTab,
        onSelect = { selectedTab = it }
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Categories List
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f, fill = false)
          .height(340.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredCategories, key = { it.id }) { cat ->
          CategoryListItem(
            category = cat,
            onEdit = {
              editingCategory = cat
              showAddEditSheet = true
            },
            onDelete = {
              categoryToDelete = cat
            }
          )
        }

        item {
          Spacer(modifier = Modifier.height(10.dp))
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Buttons: "+ Add Category" & "Reset Defaults"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = { showResetConfirm = true },
          shape = RoundedCornerShape(999.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = BachatTextSecondary),
          modifier = Modifier
            .weight(0.38f)
            .height(48.dp)
            .testTag("reset_categories_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Reset", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        }

        Button(
          onClick = {
            editingCategory = null
            showAddEditSheet = true
          },
          shape = RoundedCornerShape(999.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BachatInk,
            contentColor = BachatOnColor
          ),
          modifier = Modifier
            .weight(0.62f)
            .height(48.dp)
            .testTag("add_category_button")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Category", fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }

  // Add / Edit Category Dialog / Sheet
  if (showAddEditSheet) {
    AddEditCategorySheet(
      initialCategory = editingCategory,
      defaultType = if (selectedTab == 0) TransactionType.EXPENSE else TransactionType.INCOME,
      onDismiss = {
        showAddEditSheet = false
        editingCategory = null
      },
      onSave = { name, type, iconName, colorHex ->
        val currentEdit = editingCategory
        if (currentEdit != null) {
          onUpdateCategory(
            currentEdit.copy(
              name = name,
              type = type,
              iconName = iconName,
              colorHex = colorHex
            )
          )
        } else {
          onAddCategory(name, type, iconName, colorHex)
        }
        showAddEditSheet = false
        editingCategory = null
      }
    )
  }

  // Delete Category Confirmation Dialog
  categoryToDelete?.let { cat ->
    ConfirmDialog(
      title = "Delete '${cat.name}'?",
      message = "Are you sure you want to remove this category? Existing transactions with this category will remain safe.",
      confirmText = "Delete",
      cancelText = "Cancel",
      onConfirm = {
        onDeleteCategory(cat)
        categoryToDelete = null
      },
      onDismiss = {
        categoryToDelete = null
      }
    )
  }

  // Reset Confirmation Dialog
  if (showResetConfirm) {
    ConfirmDialog(
      title = "Reset to Default Categories?",
      message = "This will restore the standard categories and icons. Custom categories will be replaced.",
      confirmText = "Reset All",
      cancelText = "Cancel",
      onConfirm = {
        onResetDefaults()
        showResetConfirm = false
      },
      onDismiss = {
        showResetConfirm = false
      }
    )
  }
}

@Composable
fun CategoryListItem(
  category: CategoryItem,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val icon = getCategoryIcon(category.name, category.iconName)
  val catColor = Color(category.colorHex)
  val tintColor = catColor.copy(alpha = 0.15f)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(BachatSurface)
      .padding(horizontal = 14.dp, vertical = 12.dp)
      .testTag("category_item_${category.name}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(tintColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = category.name,
          tint = catColor,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = category.name,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Bold,
            color = BachatTextPrimary
          )
          if (category.isDefault) {
            Spacer(modifier = Modifier.width(6.dp))
            BadgePill(
              text = "Default",
              backgroundColor = Color.White,
              textColor = BachatTextSecondary
            )
          }
        }

        Text(
          text = if (category.type == TransactionType.EXPENSE) "Expense Category" else "Income Category",
          fontSize = 11.5.sp,
          color = BachatTextSecondary
        )
      }
    }

    // Action buttons: Edit Icon/Name & Delete
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(
        onClick = onEdit,
        modifier = Modifier
          .size(36.dp)
          .testTag("edit_category_${category.name}")
      ) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = "Edit Category",
          tint = BachatTextSecondary,
          modifier = Modifier.size(18.dp)
        )
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(36.dp)
          .testTag("delete_category_${category.name}")
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete Category",
          tint = BachatDanger,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategorySheet(
  initialCategory: CategoryItem? = null,
  defaultType: TransactionType = TransactionType.EXPENSE,
  onDismiss: () -> Unit,
  onSave: (name: String, type: TransactionType, iconName: String, colorHex: Long) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val isEditing = initialCategory != null

  var name by remember { mutableStateOf(initialCategory?.name ?: "") }
  var type by remember { mutableStateOf(initialCategory?.type ?: defaultType) }
  var selectedIconName by remember { mutableStateOf(initialCategory?.iconName ?: "Restaurant") }
  var selectedColorHex by remember { mutableStateOf(initialCategory?.colorHex ?: PRESET_CATEGORY_COLORS.first()) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
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
        .testTag("add_edit_category_sheet")
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isEditing) "Edit Category" else "Add New Category",
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

      // Live Category Preview Avatar & Title
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BachatSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(Color(selectedColorHex).copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = CATEGORY_ICON_MAP[selectedIconName] ?: Icons.Default.Restaurant,
              contentDescription = null,
              tint = Color(selectedColorHex),
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = if (name.isBlank()) "Category Name" else name,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = if (name.isBlank()) BachatTextSecondary else BachatTextPrimary
            )
            Text(
              text = "${type.name.lowercase().replaceFirstChar { it.uppercase() }} · Icon: $selectedIconName",
              fontSize = 12.sp,
              color = BachatTextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 1. Category Name Input
      Text(
        text = "CATEGORY NAME",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = name,
        onValueChange = {
          name = it
          errorMessage = null
        },
        placeholder = { Text("e.g. Groceries, Coffee, Gym, Freelance", fontSize = 14.sp) },
        singleLine = true,
        isError = errorMessage != null,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BachatInk,
          unfocusedBorderColor = Color(0xFFE2E8F0),
          focusedContainerColor = Color.White,
          unfocusedContainerColor = BachatSurface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("category_name_input")
      )
      if (errorMessage != null) {
        Text(
          text = errorMessage!!,
          fontSize = 12.sp,
          color = BachatDanger,
          modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Type Selector
      Text(
        text = "TRANSACTION TYPE",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      SegmentedToggle(
        options = listOf("Expense", "Income"),
        selectedIndex = if (type == TransactionType.EXPENSE) 0 else 1,
        onSelect = {
          type = if (it == 0) TransactionType.EXPENSE else TransactionType.INCOME
        }
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 3. Icon Palette Grid
      Text(
        text = "CHOOSE ICON (${CATEGORY_ICON_MAP.size} AVAILABLE)",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))
      
      // Grid of icon buttons
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(160.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(BachatSurface)
          .padding(8.dp)
      ) {
        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 44.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(CATEGORY_ICON_MAP.keys.toList()) { iconKey ->
            val iconVec = CATEGORY_ICON_MAP[iconKey] ?: Icons.Default.Restaurant
            val isSelected = selectedIconName == iconKey
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(selectedColorHex) else Color.White)
                .border(
                  width = if (isSelected) 2.dp else 0.5.dp,
                  color = if (isSelected) Color(selectedColorHex) else Color(0xFFE2E8F0),
                  shape = CircleShape
                )
                .clickable { selectedIconName = iconKey }
                .testTag("icon_option_$iconKey"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = iconVec,
                contentDescription = iconKey,
                tint = if (isSelected) Color.White else BachatTextPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Color Palette Picker
      Text(
        text = "ACCENT COLOR",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = BachatTextSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        PRESET_CATEGORY_COLORS.forEach { colorVal ->
          val isSelected = selectedColorHex == colorVal
          Box(
            modifier = Modifier
              .size(30.dp)
              .clip(CircleShape)
              .background(Color(colorVal))
              .clickable { selectedColorHex = colorVal }
              .then(
                if (isSelected) Modifier.border(2.5.dp, Color.White, CircleShape).border(4.dp, Color(colorVal), CircleShape)
                else Modifier
              ),
            contentAlignment = Alignment.Center
          ) {
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Save Button
      Button(
        onClick = {
          val trimmedName = name.trim()
          if (trimmedName.isBlank()) {
            errorMessage = "Category name cannot be empty"
            return@Button
          }
          onSave(trimmedName, type, selectedIconName, selectedColorHex)
        },
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = BachatInk,
          contentColor = BachatOnColor
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("save_category_button")
      ) {
        Text(
          text = if (isEditing) "Update Category" else "Create Category",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

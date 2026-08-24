package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.shadow
import com.example.ui.theme.BachatTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Goal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.formatRupee
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
import com.example.ui.theme.BachatWarning
import com.example.ui.theme.BachatWarningTint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatColumn(
  icon: ImageVector,
  iconBgColor: Color,
  iconTint: Color,
  caption: String,
  value: String,
  valueColor: Color = BachatTextPrimary,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(iconBgColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = caption,
        tint = iconTint,
        modifier = Modifier.size(18.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = caption.uppercase(),
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 0.5.sp,
      color = BachatTextSecondary
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = value,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = valueColor
    )
  }
}

@Composable
fun BadgePill(
  text: String,
  modifier: Modifier = Modifier,
  backgroundColor: Color = BachatSuccessTint,
  textColor: Color = BachatSuccess,
  icon: ImageVector? = null,
  iconTint: Color = textColor
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(999.dp))
      .background(backgroundColor)
      .padding(horizontal = 10.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
      }
      Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
      )
    }
  }
}

@Composable
fun ThinProgressBar(
  progress: Float,
  modifier: Modifier = Modifier,
  fillColor: Color = BachatSuccess,
  trackColor: Color = BachatDivider,
  height: Dp = 6.dp
) {
  val clampedProgress = progress.coerceIn(0f, 1f)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
      .clip(RoundedCornerShape(999.dp))
      .background(trackColor)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(clampedProgress)
        .height(height)
        .clip(RoundedCornerShape(999.dp))
        .background(fillColor)
    )
  }
}

@Composable
fun ToggleSwitchRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(BachatSurface)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = BachatTextPrimary
      )
      Text(
        text = subtitle,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Normal,
        color = BachatTextSecondary
      )
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = BachatInk,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = BachatDivider
      )
    )
  }
}

val CATEGORY_ICON_MAP: Map<String, ImageVector> = mapOf(
  "Restaurant" to Icons.Default.Restaurant,
  "Fastfood" to Icons.Default.Fastfood,
  "LocalCafe" to Icons.Default.LocalCafe,
  "ShoppingCart" to Icons.Default.ShoppingCart,
  "ShoppingBag" to Icons.Default.ShoppingBag,
  "Person" to Icons.Default.Person,
  "FitnessCenter" to Icons.Default.FitnessCenter,
  "MedicalServices" to Icons.Default.MedicalServices,
  "LocalPharmacy" to Icons.Default.LocalPharmacy,
  "DirectionsBus" to Icons.Default.DirectionsBus,
  "DirectionsCar" to Icons.Default.DirectionsCar,
  "TwoWheeler" to Icons.Default.TwoWheeler,
  "Flight" to Icons.Default.Flight,
  "Home" to Icons.Default.Home,
  "FlashOn" to Icons.Default.FlashOn,
  "Wifi" to Icons.Default.Wifi,
  "Tv" to Icons.Default.Tv,
  "Payments" to Icons.Default.Payments,
  "AccountBalanceWallet" to Icons.Default.AccountBalanceWallet,
  "AccountBalance" to Icons.Default.AccountBalance,
  "CreditCard" to Icons.Default.CreditCard,
  "TrendingUp" to Icons.Default.TrendingUp,
  "ShowChart" to Icons.Default.ShowChart,
  "Savings" to Icons.Default.Savings,
  "School" to Icons.Default.School,
  "Movie" to Icons.Default.Movie,
  "SportsEsports" to Icons.Default.SportsEsports,
  "Pets" to Icons.Default.Pets,
  "CardGiftcard" to Icons.Default.CardGiftcard,
  "Celebration" to Icons.Default.Celebration,
  "Work" to Icons.Default.Work,
  "AutoAwesome" to Icons.Default.AutoAwesome,
  "ReceiptLong" to Icons.Default.ReceiptLong,
  "AttachMoney" to Icons.Default.AttachMoney,
  "LocalGasStation" to Icons.Default.LocalGasStation,
  "Construction" to Icons.Default.Construction,
  "PhoneIphone" to Icons.Default.PhoneIphone
)

fun getCategoryIcon(category: String, iconName: String? = null): ImageVector {
  if (iconName != null && CATEGORY_ICON_MAP.containsKey(iconName)) {
    return CATEGORY_ICON_MAP[iconName]!!
  }
  return when (category.lowercase().trim()) {
    "food", "dining", "restaurant" -> Icons.Default.Restaurant
    "personal/health", "personal", "health", "care" -> Icons.Default.Person
    "transport", "travel", "cab", "fuel" -> Icons.Default.DirectionsBus
    "housing", "rent", "home" -> Icons.Default.Home
    "financial", "bills", "utility" -> Icons.Default.Payments
    "salary", "wage" -> Icons.Default.AccountBalanceWallet
    "interest" -> Icons.Default.TrendingUp
    "investment", "stocks", "mutual funds" -> Icons.Default.ShowChart
    "loan", "emi", "credit" -> Icons.Default.CreditCard
    "unexpected", "misc", "other" -> Icons.Default.AutoAwesome
    "shopping", "groceries" -> Icons.Default.ShoppingCart
    "entertainment", "movies", "ott" -> Icons.Default.Movie
    "education", "fees", "books" -> Icons.Default.School
    "gym", "fitness" -> Icons.Default.FitnessCenter
    "coffee", "cafe" -> Icons.Default.LocalCafe
    "fast food", "snacks" -> Icons.Default.Fastfood
    "gifts", "gift" -> Icons.Default.CardGiftcard
    "freelance", "side hustle", "bonus" -> Icons.Default.AttachMoney
    else -> Icons.Default.AccountBalance
  }
}

@Composable
fun TransactionRow(
  transaction: Transaction,
  onClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val isExpense = transaction.type == TransactionType.EXPENSE
  val iconBgColor = if (isExpense) BachatDangerTint else BachatSuccessTint
  val iconColor = if (isExpense) BachatDanger else BachatSuccess
  val amountColor = if (isExpense) BachatDanger else BachatSuccess
  val categoryIcon = getCategoryIcon(transaction.category)

  val timeFormatter = SimpleDateFormat("HH:mm · dd MMM", Locale.getDefault())
  val dateFormatted = timeFormatter.format(Date(transaction.timestamp))

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(vertical = 4.dp, horizontal = 0.dp)
      .testTag("transaction_row_${transaction.id}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Avatar
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(iconBgColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = categoryIcon,
        contentDescription = transaction.category,
        tint = iconColor,
        modifier = Modifier.size(16.dp)
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Middle Info
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = transaction.category,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = BachatTextPrimary
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = dateFormatted,
          fontSize = 11.sp,
          color = BachatTextSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BachatSurface)
            .padding(horizontal = 4.dp, vertical = 0.dp)
        ) {
          Text(
            text = transaction.accountTag,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = BachatTextSecondary
          )
        }

        if (transaction.isAuto) {
          Spacer(modifier = Modifier.width(4.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(BachatSuccessTint)
              .padding(horizontal = 4.dp, vertical = 0.dp)
          ) {
            Text(
              text = "⚡ AUTO",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = BachatSuccess
            )
          }
        }

        if (transaction.note.isNotBlank()) {
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "· ${transaction.note}",
            fontSize = 11.sp,
            color = BachatTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        if (!transaction.receiptUri.isNullOrBlank()) {
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = "Receipt attached",
            tint = BachatAccentIndigo,
            modifier = Modifier.size(12.dp)
          )
        }
      }
    }

    // Trailing Amount
    Text(
      text = (if (isExpense) "-" else "+") + formatRupee(transaction.amount),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = amountColor
    )
  }
}

@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .shadow(4.dp, RoundedCornerShape(20.dp), clip = false)
      .clip(RoundedCornerShape(20.dp))
      .background(BachatTheme.colors.background)
      .border(1.dp, BachatTheme.colors.divider, RoundedCornerShape(20.dp))
      .padding(16.dp),
    content = content,
  )
}

@Composable
fun GoalCardContent(
  goal: Goal,
  spentAmount: Double
) {
  val target = goal.targetAmount
  val ratio = if (target > 0) (spentAmount / target).toFloat() else 0f
  val percentage = (ratio * 100).toInt()

  val statusColor = when {
    ratio > 1f -> BachatDanger
    ratio >= 0.8f -> BachatWarning
    else -> BachatSuccess
  }

  Column(
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = goal.title,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "(monthly) • ${goal.category}",
          fontSize = 13.sp,
          color = BachatTextSecondary,
          fontWeight = FontWeight.Normal
        )
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(BachatSurface)
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = "${percentage}%",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
      verticalAlignment = Alignment.Bottom
    ) {
      Text(
        text = formatRupee(spentAmount),
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        color = BachatTextPrimary
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "/ ${formatRupee(target)}",
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = BachatTextSecondary,
        modifier = Modifier.padding(bottom = 2.dp)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    ThinProgressBar(
      progress = ratio,
      fillColor = statusColor,
      height = 5.dp
    )

    Spacer(modifier = Modifier.height(14.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (ratio <= 1f) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
          contentDescription = null,
          tint = statusColor,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (ratio <= 1f) "On Track" else "Over Budget",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = statusColor
        )
      }

      val diff = target - spentAmount
      val safeText = if (diff >= 0) "${formatRupee(diff)} safe" else "${formatRupee(-diff)} over"
      Text(
        text = safeText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = statusColor
      )
    }
  }
}

@Composable
fun DashboardGoalCard(
  goal: Goal,
  spentAmount: Double,
  onNavigateToGoals: () -> Unit,
  onClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val target = goal.targetAmount
  val ratio = if (target > 0) (spentAmount / target).toFloat() else 0f
  val percentage = (ratio * 100).toInt()

  val statusColor = when {
    ratio > 1f -> BachatDanger
    ratio >= 0.8f -> BachatWarning
    else -> BachatSuccess
  }

  AppCard(
    modifier = modifier
      .clickable { onClick() }
      .testTag("dashboard_goal_card_${goal.id}")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(statusColor)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = goal.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = BachatTextPrimary
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "(monthly)",
            fontSize = 12.5.sp,
            color = BachatTextSecondary
          )
        }

        Text(
          text = "${formatRupee(spentAmount)} / ${formatRupee(target)}",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      ThinProgressBar(
        progress = ratio,
        fillColor = statusColor,
        height = 4.dp
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "${percentage}%",
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        color = statusColor,
        modifier = Modifier.align(Alignment.End)
      )

      Spacer(modifier = Modifier.height(10.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToGoals() }
          .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "View all goals →",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = BachatTextPrimary
        )
      }
    }
  }
}

@Composable
fun GoalCard(
  goal: Goal,
  spentAmount: Double,
  onClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  AppCard(
    modifier = modifier
      .clickable { onClick() }
      .testTag("goal_card_${goal.id}")
  ) {
    GoalCardContent(goal = goal, spentAmount = spentAmount)
  }
}

@Composable
fun RowActionPopup(
  onDismiss: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = Color.White,
      shadowElevation = 8.dp,
      modifier = Modifier
        .width(220.dp)
        .testTag("row_action_popup")
    ) {
      Column(
        modifier = Modifier.padding(vertical = 6.dp)
      ) {
        // Edit
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              onDismiss()
              onEdit()
            }
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("action_popup_edit"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = BachatInk,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(14.dp))
          Text(
            text = "Edit",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = BachatInk
          )
        }

        HorizontalDivider(color = BachatDivider, thickness = 0.8.dp)

        // Delete
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              onDismiss()
              onDelete()
            }
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("action_popup_delete"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = BachatDanger,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(14.dp))
          Text(
            text = "Delete",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = BachatDanger
          )
        }
      }
    }
  }
}

@Composable
fun ConfirmDialog(
  title: String,
  message: String,
  confirmText: String = "Replace",
  cancelText: String = "Cancel",
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = BachatTextPrimary
      )
    },
    text = {
      Text(
        text = message,
        fontSize = 14.sp,
        color = BachatTextSecondary
      )
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("confirm_dialog_cancel")
      ) {
        Text(
          text = cancelText,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = BachatTextSecondary
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm()
          onDismiss()
        },
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = BachatDanger,
          contentColor = Color.White
        ),
        modifier = Modifier.testTag("confirm_dialog_confirm")
      ) {
        Text(
          text = confirmText,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White
  )
}

@Composable
fun EmptyState(
  icon: ImageVector,
  message: String,
  helperText: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 40.dp, horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(BachatSurface),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = BachatTextSecondary,
        modifier = Modifier.size(32.dp)
      )
    }
    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text = message,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = BachatTextPrimary,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = helperText,
      fontSize = 13.sp,
      color = BachatTextSecondary,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
fun SegmentedToggle(
  options: List<String>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
  selectedTextColor: Color = BachatInk
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(999.dp))
      .background(BachatSurface)
      .padding(4.dp)
  ) {
    Row(modifier = Modifier.fillMaxWidth()) {
      options.forEachIndexed { index, title ->
        val isSelected = index == selectedIndex
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(999.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onSelect(index) }
            .padding(vertical = 9.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) selectedTextColor else BachatTextSecondary
          )
        }
      }
    }
  }
}

@Composable
fun BachatFilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(999.dp))
      .background(if (selected) BachatInk else BachatSurface)
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (selected) Color.White else BachatTextSecondary,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = if (selected) Color.White else BachatTextSecondary
      )
    }
  }
}

@Composable
fun CategoryIconButton(
  label: String,
  icon: ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  baseColor: Color,
  tintColor: Color,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .clickable { onClick() }
      .padding(horizontal = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(62.dp)
        .clip(CircleShape)
        .background(if (selected) baseColor else tintColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (selected) Color.White else baseColor,
        modifier = Modifier.size(26.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      fontSize = 11.5.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      color = if (selected) BachatTextPrimary else BachatTextSecondary,
      maxLines = 1
    )
  }
}


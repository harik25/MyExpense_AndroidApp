package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.Locale

enum class TransactionType {
  EXPENSE,
  INCOME
}

@Entity(tableName = "custom_categories")
data class CategoryItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val type: TransactionType = TransactionType.EXPENSE,
  val iconName: String = "Restaurant",
  val colorHex: Long = 0xFF4338CA,
  val isDefault: Boolean = false
)

val DEFAULT_CATEGORIES: List<CategoryItem> = listOf(
  // Expense Defaults
  CategoryItem(id = 1, name = "Food", type = TransactionType.EXPENSE, iconName = "Restaurant", colorHex = 0xFFEF4444, isDefault = true),
  CategoryItem(id = 2, name = "Personal/Health", type = TransactionType.EXPENSE, iconName = "Person", colorHex = 0xFF8B5CF6, isDefault = true),
  CategoryItem(id = 3, name = "Transport", type = TransactionType.EXPENSE, iconName = "DirectionsBus", colorHex = 0xFF3B82F6, isDefault = true),
  CategoryItem(id = 4, name = "Housing", type = TransactionType.EXPENSE, iconName = "Home", colorHex = 0xFFF59E0B, isDefault = true),
  CategoryItem(id = 5, name = "Financial", type = TransactionType.EXPENSE, iconName = "Payments", colorHex = 0xFF10B981, isDefault = true),
  CategoryItem(id = 6, name = "Unexpected", type = TransactionType.EXPENSE, iconName = "AutoAwesome", colorHex = 0xFF6366F1, isDefault = true),

  // Income Defaults
  CategoryItem(id = 7, name = "Salary", type = TransactionType.INCOME, iconName = "AccountBalanceWallet", colorHex = 0xFF10B981, isDefault = true),
  CategoryItem(id = 8, name = "Interest", type = TransactionType.INCOME, iconName = "TrendingUp", colorHex = 0xFF3B82F6, isDefault = true),
  CategoryItem(id = 9, name = "Investment", type = TransactionType.INCOME, iconName = "ShowChart", colorHex = 0xFF8B5CF6, isDefault = true),
  CategoryItem(id = 10, name = "Loan", type = TransactionType.INCOME, iconName = "CreditCard", colorHex = 0xFFF59E0B, isDefault = true),
  CategoryItem(id = 11, name = "Unexpected", type = TransactionType.INCOME, iconName = "AutoAwesome", colorHex = 0xFF6366F1, isDefault = true)
)

enum class ExpenseCategory(val displayName: String, val shortName: String) {
  FOOD("Food", "Food"),
  PERSONAL_HEALTH("Personal/Health", "Personal/H..."),
  TRANSPORT("Transport", "Transport"),
  HOUSING("Housing", "Housing"),
  FINANCIAL("Financial", "Financial"),
  UNEXPECTED("Unexpected", "Unexpected")
}

enum class IncomeCategory(val displayName: String) {
  SALARY("Salary"),
  INTEREST("Interest"),
  INVESTMENT("Investment"),
  LOAN("Loan"),
  UNEXPECTED("Unexpected")
}

enum class AccountTag(val displayName: String) {
  ALL("All"),
  UPI("UPI"),
  BANK("Bank"),
  CASH("Cash")
}

enum class TimePeriod(val displayName: String) {
  TODAY("Today"),
  WEEK("Week"),
  MONTH("Month"),
  ALL("All"),
  CUSTOM("Custom")
}

enum class GoalCadence(val displayName: String) {
  MONTHLY("monthly"),
  WEEKLY("weekly"),
  YEARLY("yearly")
}

enum class GoalStatus {
  ON_TRACK,
  NEAR_LIMIT,
  OVER_BUDGET
}

enum class VisibilityFilter(val displayName: String) {
  STANDARD("Standard"),
  SECRET("Secret")
}

enum class SortField(val displayName: String) {
  DATE("Date"),
  AMOUNT("Amount")
}

enum class SortDirection(val displayName: String) {
  DESC("↓ Newest/Highest"),
  ASC("↑ Oldest/Lowest")
}

enum class AppThemeMode {
  SYSTEM,
  LIGHT,
  DARK
}

@Entity(tableName = "transactions")
data class Transaction(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val amount: Double,
  val type: TransactionType,
  val category: String, // from ExpenseCategory or IncomeCategory displayName
  val accountTag: String = "UPI",
  val timestamp: Long = System.currentTimeMillis(),
  val isAuto: Boolean = false,
  val isSecret: Boolean = false,
  val note: String = "",
  val receiptUri: String? = null
)

@Entity(tableName = "goals")
data class Goal(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val cadence: String = "monthly",
  val category: String = "Food",
  val targetAmount: Double
)

@Entity(tableName = "recurring_bills")
data class RecurringBill(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val amount: Double,
  val category: String = "Housing",
  val dueDayOfMonth: Int = 1, // 1 to 31
  val cadence: String = "Monthly", // "Monthly", "Weekly", "Yearly"
  val accountTag: String = "UPI",
  val note: String = "",
  val isAutoPay: Boolean = false,
  val lastPaidMonthKey: String = "", // e.g. "2026-08" to indicate it has been paid for this month
  val isPaused: Boolean = false
) {
  fun isPaidForCurrentMonth(currentMonthKey: String): Boolean {
    return lastPaidMonthKey == currentMonthKey
  }
}

data class CategoryBudgetInsight(
  val category: String,
  val budgetLimit: Double,
  val spentAmount: Double,
  val remainingAmount: Double,
  val percentageUsed: Double,
  val status: GoalStatus,
  val goalId: Long? = null
)

data class OverspendAlert(
  val category: String,
  val budgetLimit: Double,
  val spentAmount: Double,
  val overspendAmount: Double,
  val percentageUsed: Double,
  val isExceeded: Boolean // true: >=100% (danger), false: >=80% (warning)
)

data class CategoryShift(
  val category: String,
  val currentMonthAmount: Double,
  val lastMonthAmount: Double,
  val diffAmount: Double,
  val percentageChange: Double,
  val isIncrease: Boolean
)

data class MonthOverMonthInsight(
  val thisMonthTotal: Double = 0.0,
  val lastMonthTotal: Double = 0.0,
  val diffAmount: Double = 0.0,
  val percentageChange: Double = 0.0,
  val isLower: Boolean = true,
  val thisMonthDailyAvg: Double = 0.0,
  val lastMonthDailyAvg: Double = 0.0,
  val projectedMonthEndTotal: Double = 0.0,
  val peakSpendDayLabel: String = "",
  val peakSpendDayAmount: Double = 0.0,
  val categoryShifts: List<CategoryShift> = emptyList(),
  val keyTakeaways: List<String> = emptyList()
)

fun formatRupee(amount: Double, showDecimals: Boolean = false): String {
  val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
  formatter.maximumFractionDigits = if (showDecimals && amount % 1.0 != 0.0) 2 else 0
  val formatted = formatter.format(amount)
  return formatted.replace("₹ ", "₹").replace("INR ", "₹").replace("Rs. ", "₹")
}

fun formatRupee(amount: Long): String {
  return formatRupee(amount.toDouble(), false)
}

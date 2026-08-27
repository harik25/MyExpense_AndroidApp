package com.example.data.repository

import com.example.data.local.CategoryDao
import com.example.data.local.GoalDao
import com.example.data.local.RecurringBillDao
import com.example.data.local.TransactionDao
import com.example.data.model.CategoryItem
import com.example.data.model.DEFAULT_CATEGORIES
import com.example.data.model.Goal
import com.example.data.model.RecurringBill
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class BachatRepository(
  private val transactionDao: TransactionDao,
  private val goalDao: GoalDao,
  private val categoryDao: CategoryDao,
  private val recurringBillDao: RecurringBillDao
) {
  val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
  val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()
  val allCategories: Flow<List<CategoryItem>> = categoryDao.getAllCategories()
  val allRecurringBills: Flow<List<RecurringBill>> = recurringBillDao.getAllBills()
  val activeRecurringBills: Flow<List<RecurringBill>> = recurringBillDao.getActiveBills()

  suspend fun insertTransaction(transaction: Transaction): Long {
    return transactionDao.insertTransaction(transaction)
  }

  suspend fun insertTransactions(transactions: List<Transaction>) {
    transactionDao.insertTransactions(transactions)
  }

  suspend fun updateTransaction(transaction: Transaction) {
    transactionDao.updateTransaction(transaction)
  }

  suspend fun deleteTransaction(transaction: Transaction) {
    transactionDao.deleteTransaction(transaction)
  }

  suspend fun deleteTransactionById(id: Long) {
    transactionDao.deleteTransactionById(id)
  }

  suspend fun insertGoal(goal: Goal): Long {
    return goalDao.insertGoal(goal)
  }

  suspend fun insertGoals(goals: List<Goal>) {
    goalDao.insertGoals(goals)
  }

  suspend fun updateGoal(goal: Goal) {
    goalDao.updateGoal(goal)
  }

  suspend fun deleteGoal(goal: Goal) {
    goalDao.deleteGoal(goal)
  }

  suspend fun deleteGoalById(id: Long) {
    goalDao.deleteGoalById(id)
  }

  // Category management methods
  suspend fun insertCategory(category: CategoryItem): Long {
    return categoryDao.insertCategory(category)
  }

  suspend fun insertCategories(categories: List<CategoryItem>) {
    categoryDao.insertCategories(categories)
  }

  suspend fun updateCategory(category: CategoryItem, oldName: String? = null) {
    categoryDao.updateCategory(category)
    if (!oldName.isNullOrBlank() && !oldName.equals(category.name, ignoreCase = true)) {
      transactionDao.updateCategoryName(oldName, category.name)
      goalDao.updateCategoryName(oldName, category.name)
      recurringBillDao.updateCategoryName(oldName, category.name)
    }
  }

  suspend fun deleteCategory(category: CategoryItem) {
    categoryDao.deleteCategory(category)
  }

  suspend fun deleteCategoryById(id: Long) {
    categoryDao.deleteCategoryById(id)
  }

  suspend fun resetCategoriesToDefault() {
    categoryDao.clearAll()
    categoryDao.insertCategories(DEFAULT_CATEGORIES)
  }

  // Recurring bills management
  suspend fun insertRecurringBill(bill: RecurringBill): Long {
    return recurringBillDao.insertBill(bill)
  }

  suspend fun insertRecurringBills(bills: List<RecurringBill>) {
    recurringBillDao.insertBills(bills)
  }

  suspend fun updateRecurringBill(bill: RecurringBill) {
    recurringBillDao.updateBill(bill)
  }

  suspend fun deleteRecurringBill(bill: RecurringBill) {
    recurringBillDao.deleteBill(bill)
  }

  suspend fun deleteRecurringBillById(id: Long) {
    recurringBillDao.deleteBillById(id)
  }

  suspend fun markRecurringBillPaid(id: Long, monthKey: String) {
    recurringBillDao.markBillPaid(id, monthKey)
  }

  suspend fun clearAll() {
    transactionDao.clearAll()
    goalDao.clearAll()
    categoryDao.clearAll()
    recurringBillDao.clearAll()
  }
}

package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CategoryItem
import com.example.data.model.Goal
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
  fun getAllTransactions(): Flow<List<Transaction>>

  @Query("SELECT * FROM transactions WHERE isSecret = 0 ORDER BY timestamp DESC")
  fun getNonSecretTransactions(): Flow<List<Transaction>>

  @Query("SELECT * FROM transactions WHERE id = :id")
  suspend fun getTransactionById(id: Long): Transaction?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: Transaction): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransactions(transactions: List<Transaction>)

  @Update
  suspend fun updateTransaction(transaction: Transaction)

  @Delete
  suspend fun deleteTransaction(transaction: Transaction)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteTransactionById(id: Long)

  @Query("UPDATE transactions SET category = :newCategory WHERE category = :oldCategory")
  suspend fun updateCategoryName(oldCategory: String, newCategory: String)

  @Query("DELETE FROM transactions")
  suspend fun clearAll()
}

@Dao
interface GoalDao {
  @Query("SELECT * FROM goals")
  fun getAllGoals(): Flow<List<Goal>>

  @Query("SELECT * FROM goals WHERE id = :id")
  suspend fun getGoalById(id: Long): Goal?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGoal(goal: Goal): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGoals(goals: List<Goal>)

  @Update
  suspend fun updateGoal(goal: Goal)

  @Delete
  suspend fun deleteGoal(goal: Goal)

  @Query("DELETE FROM goals WHERE id = :id")
  suspend fun deleteGoalById(id: Long)

  @Query("UPDATE goals SET category = :newCategory WHERE category = :oldCategory")
  suspend fun updateCategoryName(oldCategory: String, newCategory: String)

  @Query("DELETE FROM goals")
  suspend fun clearAll()
}

@Dao
interface CategoryDao {
  @Query("SELECT * FROM custom_categories ORDER BY id ASC")
  fun getAllCategories(): Flow<List<CategoryItem>>

  @Query("SELECT * FROM custom_categories WHERE type = :type ORDER BY id ASC")
  fun getCategoriesByType(type: com.example.data.model.TransactionType): Flow<List<CategoryItem>>

  @Query("SELECT * FROM custom_categories WHERE id = :id")
  suspend fun getCategoryById(id: Long): CategoryItem?

  @Query("SELECT * FROM custom_categories WHERE name = :name LIMIT 1")
  suspend fun getCategoryByName(name: String): CategoryItem?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: CategoryItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<CategoryItem>)

  @Update
  suspend fun updateCategory(category: CategoryItem)

  @Delete
  suspend fun deleteCategory(category: CategoryItem)

  @Query("DELETE FROM custom_categories WHERE id = :id")
  suspend fun deleteCategoryById(id: Long)

  @Query("DELETE FROM custom_categories")
  suspend fun clearAll()
}

@Dao
interface RecurringBillDao {
  @Query("SELECT * FROM recurring_bills ORDER BY dueDayOfMonth ASC, id ASC")
  fun getAllBills(): Flow<List<com.example.data.model.RecurringBill>>

  @Query("SELECT * FROM recurring_bills WHERE isPaused = 0 ORDER BY dueDayOfMonth ASC")
  fun getActiveBills(): Flow<List<com.example.data.model.RecurringBill>>

  @Query("SELECT * FROM recurring_bills WHERE id = :id")
  suspend fun getBillById(id: Long): com.example.data.model.RecurringBill?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBill(bill: com.example.data.model.RecurringBill): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBills(bills: List<com.example.data.model.RecurringBill>)

  @Update
  suspend fun updateBill(bill: com.example.data.model.RecurringBill)

  @Delete
  suspend fun deleteBill(bill: com.example.data.model.RecurringBill)

  @Query("DELETE FROM recurring_bills WHERE id = :id")
  suspend fun deleteBillById(id: Long)

  @Query("UPDATE recurring_bills SET lastPaidMonthKey = :monthKey WHERE id = :id")
  suspend fun markBillPaid(id: Long, monthKey: String)

  @Query("UPDATE recurring_bills SET category = :newCategory WHERE category = :oldCategory")
  suspend fun updateCategoryName(oldCategory: String, newCategory: String)

  @Query("DELETE FROM recurring_bills")
  suspend fun clearAll()
}


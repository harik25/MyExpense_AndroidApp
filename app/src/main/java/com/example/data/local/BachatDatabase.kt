package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CategoryItem
import com.example.data.model.DEFAULT_CATEGORIES
import com.example.data.model.Goal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
  entities = [Transaction::class, Goal::class, CategoryItem::class],
  version = 5,
  exportSchema = false
)
abstract class BachatDatabase : RoomDatabase() {
  abstract fun transactionDao(): TransactionDao
  abstract fun goalDao(): GoalDao
  abstract fun categoryDao(): CategoryDao

  companion object {
    @Volatile
    private var INSTANCE: BachatDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): BachatDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          BachatDatabase::class.java,
          "bachat_database"
        )
        .addCallback(BachatDatabaseCallback(scope))
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        
        // Ensure default categories exist if database was already created before
        scope.launch(Dispatchers.IO) {
          try {
            val existing = instance.categoryDao().getCategoryByName("Food")
            if (existing == null) {
              instance.categoryDao().insertCategories(DEFAULT_CATEGORIES)
            }
          } catch (_: Exception) {}
        }

        instance
      }
    }

    private class BachatDatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(
              database.transactionDao(),
              database.goalDao(),
              database.categoryDao()
            )
          }
        }
      }
    }

    suspend fun populateInitialData(
      transactionDao: TransactionDao,
      goalDao: GoalDao,
      categoryDao: CategoryDao
    ) {
      categoryDao.insertCategories(DEFAULT_CATEGORIES)
      val now = Calendar.getInstance()
      val year = now.get(Calendar.YEAR)
      val month = now.get(Calendar.MONTH)

      // 14th of this month, 21:10
      val cal14 = Calendar.getInstance().apply {
        set(year, month, 14, 21, 10, 0)
        set(Calendar.MILLISECOND, 0)
      }

      // 1st of this month, 21:10
      val cal01 = Calendar.getInstance().apply {
        set(year, month, 1, 21, 10, 0)
        set(Calendar.MILLISECOND, 0)
      }

      // Last month dates for comparison (Total = 1178)
      val calLastMonth = Calendar.getInstance().apply {
        add(Calendar.MONTH, -1)
        set(Calendar.DAY_OF_MONTH, 15)
        set(Calendar.HOUR_OF_DAY, 14)
      }
      val calLastMonth2 = Calendar.getInstance().apply {
        add(Calendar.MONTH, -1)
        set(Calendar.DAY_OF_MONTH, 5)
        set(Calendar.HOUR_OF_DAY, 10)
      }

      val initialTransactions = listOf(
        // Income to make total balance ₹3,156 (3505 - 349 = 3156)
        Transaction(
          title = "Salary",
          amount = 3505.0,
          type = TransactionType.INCOME,
          category = "Salary",
          accountTag = "Bank",
          timestamp = cal01.timeInMillis,
          isAuto = false,
          isSecret = false,
          note = "Monthly salary"
        ),
        // Food expense ₹200 on 14th
        Transaction(
          title = "Food",
          amount = 200.0,
          type = TransactionType.EXPENSE,
          category = "Food",
          accountTag = "UPI",
          timestamp = cal14.timeInMillis,
          isAuto = false,
          isSecret = false,
          note = "Dinner"
        ),
        // Personal/Health expense ₹149 on 1st with Netflix note
        Transaction(
          title = "Personal/Health",
          amount = 149.0,
          type = TransactionType.EXPENSE,
          category = "Personal/Health",
          accountTag = "UPI",
          timestamp = cal01.timeInMillis,
          isAuto = true,
          isSecret = false,
          note = "Netflix"
        ),
        // Last month transactions for comparison (Total = ₹1,178)
        Transaction(
          title = "Food",
          amount = 678.0,
          type = TransactionType.EXPENSE,
          category = "Food",
          accountTag = "UPI",
          timestamp = calLastMonth.timeInMillis,
          isAuto = false,
          isSecret = false,
          note = "Groceries"
        ),
        Transaction(
          title = "Transport",
          amount = 500.0,
          type = TransactionType.EXPENSE,
          category = "Transport",
          accountTag = "UPI",
          timestamp = calLastMonth2.timeInMillis,
          isAuto = false,
          isSecret = false,
          note = "Fuel"
        )
      )

      val initialGoals = listOf(
        Goal(
          title = "Food Budget",
          cadence = "monthly",
          category = "Food",
          targetAmount = 5000.0
        )
      )

      transactionDao.insertTransactions(initialTransactions)
      goalDao.insertGoals(initialGoals)
    }
  }
}

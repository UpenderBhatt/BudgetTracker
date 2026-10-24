package com.example.mob_dev_portfolio.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mob_dev_portfolio.data.db.dao.BudgetDao
import com.example.mob_dev_portfolio.data.db.dao.CategoryDao
import com.example.mob_dev_portfolio.data.db.dao.ExpenseDao
import com.example.mob_dev_portfolio.data.db.entity.Budget
import com.example.mob_dev_portfolio.data.db.entity.Category
import com.example.mob_dev_portfolio.data.db.entity.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Category::class, Budget::class, Expense::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class BudgetTrackerDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: BudgetTrackerDatabase? = null

        fun getDatabase(context: Context): BudgetTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetTrackerDatabase::class.java,
                    "budget_tracker_database"
                )
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PrepopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.categoryDao()
                    dao.insert(Category(name = "Food", colour = "#F0D45B", isDefault = true))
                    dao.insert(Category(name = "Transport", colour = "#5B8AF0", isDefault = true))
                    dao.insert(Category(name = "Entertainment", colour = "#C77DFF", isDefault = true))
                    dao.insert(Category(name = "Rent", colour = "#F0855B", isDefault = true))
                }
            }
        }
    }
}

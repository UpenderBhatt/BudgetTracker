package com.example.mob_dev_portfolio.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Double,

    val periodType: String,

    val startDate: Date,

    val endDate: Date,

    val isActive: Boolean = true
){
    companion object{
        const val PERIOD_WEEKLY = "WEEKLY"
        const val PERIOD_MONTHLY = "MONTHLY"
    }
}


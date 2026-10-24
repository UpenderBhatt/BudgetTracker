package com.example.mob_dev_portfolio.data.db

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    //Converts long into date object
    @TypeConverter
    fun fromTimeStamp(value: Long?): Date?{
        return value?.let { Date(it) }
    }

    //Converts date into long
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
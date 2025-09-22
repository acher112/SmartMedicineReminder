package com.example.smartmedicinereminder.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartmedicinereminder.data.dao.ReminderDao
import com.example.smartmedicinereminder.data.model.Dose
import com.example.smartmedicinereminder.data.model.MedicineReminder

@Database(
    entities = [MedicineReminder::class, Dose::class],
    version = 1,
    exportSchema = true
)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var INSTANCE: ReminderDatabase? = null

        fun getInstance(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

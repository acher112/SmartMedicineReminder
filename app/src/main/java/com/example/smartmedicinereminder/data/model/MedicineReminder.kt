package com.example.smartmedicinereminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_reminders")
data class MedicineReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val startDate: String,
    val endDate: String,
    val category: String,              // NEW: "medicine" or "syrup"
    val frontImageUri: String?,        // for medicine
    val backImageUri: String?,         // for medicine
    val teaspoonCount: Int?,           // for syrup
    val syrupImageUri: String?         // for syrup
)

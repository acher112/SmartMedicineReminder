package com.example.smartmedicinereminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doses")
data class Dose(
    @PrimaryKey(autoGenerate = true) val doseId: Int = 0,
    val reminderId: Int,          // FK -> MedicineReminder.id
    val time: String,             // "08:00 AM"
    val quantity: Int,            // e.g., 1 tablet OR 10 ml
    val type: String = "tablet"   // "tablet", "syrup", "capsule" etc.
)

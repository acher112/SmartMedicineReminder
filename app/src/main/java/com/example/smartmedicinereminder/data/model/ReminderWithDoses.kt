package com.example.smartmedicinereminder.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ReminderWithDoses(
    @Embedded val reminder: MedicineReminder,
    @Relation(
        parentColumn = "id",          // Matches MedicineReminder primary key
        entityColumn = "reminderId"   // Matches Dose FK
    )
    val doses: List<Dose>
)

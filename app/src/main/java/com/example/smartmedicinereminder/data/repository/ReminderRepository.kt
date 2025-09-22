package com.example.smartmedicinereminder.data.repository

import com.example.smartmedicinereminder.data.dao.ReminderDao
import com.example.smartmedicinereminder.data.model.Dose
import com.example.smartmedicinereminder.data.model.MedicineReminder
import com.example.smartmedicinereminder.data.model.ReminderWithDoses
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {

    // ✅ Get all reminders with doses (Live updating Flow)
    fun getAllRemindersWithDoses(): Flow<List<ReminderWithDoses>> =
        dao.getAllRemindersWithDoses()

    // ✅ Get one reminder with doses as Flow (for edit)
    fun getReminderWithDosesById(id: Int): Flow<ReminderWithDoses?> =
        dao.getReminderWithDosesByIdFlow(id)

    // ✅ Insert reminder and its doses
    suspend fun insertReminder(reminder: MedicineReminder, doses: List<Dose>): Int {
        val reminderId = dao.insertReminder(reminder).toInt()
        if (doses.isNotEmpty()) {
            val updatedDoses = doses.map { dose ->
                dose.copy(reminderId = reminderId)
            }
            dao.insertDoses(updatedDoses)
        }
        return reminderId   // ✅ return the inserted ID
    }

    // ✅ Delete full reminder with all doses
    suspend fun deleteReminder(reminder: MedicineReminder) {
        // First delete all doses for this reminder
        dao.deleteDosesByReminderId(reminder.id)
        // Then delete reminder itself
        dao.deleteReminder(reminder)
    }

    // ✅ Delete a single dose by ID
    suspend fun deleteDoseById(doseId: Int) {
        dao.deleteDoseById(doseId)
    }

    // ✅ Update reminder and its doses
    suspend fun updateReminder(reminder: MedicineReminder, doses: List<Dose>) {
        // Update reminder details
        dao.updateReminder(reminder)

        // Remove old doses first
        dao.deleteDosesByReminderId(reminder.id)

        // Insert new doses linked to this reminder
        if (doses.isNotEmpty()) {
            val updatedDoses = doses.map { dose ->
                dose.copy(reminderId = reminder.id)
            }
            dao.insertDoses(updatedDoses)
        }
    }

    // ✅ Get one reminder with doses (non-Flow, suspend for AlarmManager use)
    suspend fun getReminderWithDosesNow(id: Int): ReminderWithDoses? =
        dao.getReminderWithDosesById(id)
}

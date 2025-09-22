package com.example.smartmedicinereminder.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.smartmedicinereminder.data.model.Dose
import com.example.smartmedicinereminder.data.model.MedicineReminder
import com.example.smartmedicinereminder.data.model.ReminderWithDoses
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    // Insert a reminder and return its ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MedicineReminder): Long

    // Insert multiple doses
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoses(doses: List<Dose>)

    // Update reminder (for editing front/back images, dates, etc.)
    @Update
    suspend fun updateReminder(reminder: MedicineReminder)

    // Delete reminder
    @Delete
    suspend fun deleteReminder(reminder: MedicineReminder)

    // Delete a specific dose by ID
    @Query("DELETE FROM doses WHERE doseId = :doseId")
    suspend fun deleteDoseById(doseId: Int)

    // Delete all doses that belong to a reminder
    @Query("DELETE FROM doses WHERE reminderId = :reminderId")
    suspend fun deleteDosesByReminderId(reminderId: Int)

    // Fetch reminders along with their doses (all)
    @Transaction
    @Query("SELECT * FROM medicine_reminders ORDER BY id DESC")
    fun getAllRemindersWithDoses(): Flow<List<ReminderWithDoses>>

    // Fetch a single reminder with its doses as Flow (for edit)
    @Transaction
    @Query("SELECT * FROM medicine_reminders WHERE id = :id LIMIT 1")
    fun getReminderWithDosesByIdFlow(id: Int): Flow<ReminderWithDoses?>

    // Fetch all reminders only (without doses)
    @Query("SELECT * FROM medicine_reminders ORDER BY id DESC")
    fun getAllReminders(): LiveData<List<MedicineReminder>>

    // Get a single reminder by ID (MedicineReminder only)
    @Query("SELECT * FROM medicine_reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Int): MedicineReminder?

    @Query("SELECT * FROM medicine_reminders WHERE id = :id")
    suspend fun getReminderWithDosesById(id: Int): ReminderWithDoses?

}

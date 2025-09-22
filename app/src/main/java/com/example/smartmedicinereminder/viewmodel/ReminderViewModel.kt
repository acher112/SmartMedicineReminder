package com.example.smartmedicinereminder.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartmedicinereminder.alarm.AlarmReceiver
import com.example.smartmedicinereminder.data.model.Dose
import com.example.smartmedicinereminder.data.model.MedicineReminder
import com.example.smartmedicinereminder.data.model.ReminderWithDoses
import com.example.smartmedicinereminder.data.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    val remindersWithDoses: StateFlow<List<ReminderWithDoses>> =
        repository.getAllRemindersWithDoses()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Insert + schedule new reminder */
    fun insertReminder(
        context: Context,
        name: String,
        startDate: String,
        endDate: String,
        category: String,
        frontImageUri: String?,
        backImageUri: String?,
        syrupImageUri: String?,
        teaspoonCount: Int?,
        doses: List<Pair<String, Int>>
    ) {
        viewModelScope.launch {
            val reminder = MedicineReminder(
                id = 0,
                name = name,
                startDate = startDate,
                endDate = endDate,
                category = category,
                frontImageUri = frontImageUri ?: "",
                backImageUri = backImageUri ?: "",
                syrupImageUri = syrupImageUri ?: "",
                teaspoonCount = teaspoonCount ?: 0
            )

            val doseList = doses.map { (time, qty) ->
                Dose(0, 0, time, qty)
            }

            val reminderId = repository.insertReminder(reminder, doseList)

            // schedule alarms with saved reminder
            scheduleAlarmsForReminder(context, reminderId, reminder, doses)
        }
    }

    /** Schedule alarms for each dose */
    private fun scheduleAlarmsForReminder(
        context: Context,
        reminderId: Int,
        reminder: MedicineReminder,
        doses: List<Pair<String, Int>>
    ) {
        doses.forEachIndexed { index, (timeStr, qty) ->
            val triggerAtMillis = computeNextTriggerMillis(timeStr)
            val requestCode = reminderId * 100 + index
            scheduleAlarm(context, reminderId, reminder, qty, triggerAtMillis, requestCode)
        }
    }

    /** Re-schedule alarms (e.g., after reboot) */
    fun rescheduleAlarmsForReminder(
        context: Context,
        reminderWithDoses: ReminderWithDoses
    ) {
        val reminderId = reminderWithDoses.reminder.id
        val reminder = reminderWithDoses.reminder

        reminderWithDoses.doses.forEachIndexed { index, dose ->
            val triggerAtMillis = computeNextTriggerMillis(dose.time)
            val requestCode = reminderId * 100 + index
            scheduleAlarm(context, reminderId, reminder, dose.quantity, triggerAtMillis, requestCode)
        }

        Log.d("ReminderViewModel", "🔄 Rescheduled alarms for ${reminder.name} (ID=$reminderId)")
    }

    /** Core scheduling logic */
    private fun scheduleAlarm(
        context: Context,
        reminderId: Int,
        reminder: MedicineReminder,
        qty: Int,
        triggerAtMillis: Long,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // choose which image to attach
        val imageToSend = when {
            reminder.category.equals("Syrup", ignoreCase = true) && !reminder.syrupImageUri.isNullOrBlank() ->
                reminder.syrupImageUri ?: ""
            !reminder.frontImageUri.isNullOrBlank() -> reminder.frontImageUri ?: ""
            !reminder.backImageUri.isNullOrBlank() -> reminder.backImageUri ?: ""
            else -> ""
        }


        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminderId)
            putExtra("medicineName", reminder.name)
            putExtra("medicineType", reminder.category)
            putExtra("dosage", qty.toString())
            putExtra("imagePath", imageToSend)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pending
                        )
                    } else {
                        Log.w(
                            "ReminderViewModel",
                            "⚠️ Exact alarms not allowed. Ask user to grant permission in system settings."
                        )
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pending
                    )
                }
                else -> {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pending
                    )
                }
            }

            Log.d(
                "ReminderViewModel",
                "✅ Alarm scheduled → ${reminder.name} at $triggerAtMillis (reqCode=$requestCode) image=$imageToSend"
            )
        } catch (e: SecurityException) {
            Log.e("ReminderViewModel", "❌ Failed to schedule alarm: ${e.message}")
        }
    }

    /** Compute next trigger time from "HH:mm" string */
    private fun computeNextTriggerMillis(timeStr: String): Long {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.timeInMillis
    }

    /** DB operations */
    fun deleteDose(doseId: Int) {
        viewModelScope.launch { repository.deleteDoseById(doseId) }
    }

    fun deleteReminder(reminder: MedicineReminder) {
        viewModelScope.launch { repository.deleteReminder(reminder) }
    }

    fun updateReminder(reminder: MedicineReminder, doses: List<Dose>) {
        viewModelScope.launch { repository.updateReminder(reminder, doses) }
    }

    fun getReminderWithDosesById(id: Int): Flow<ReminderWithDoses?> =
        repository.getReminderWithDosesById(id)
}

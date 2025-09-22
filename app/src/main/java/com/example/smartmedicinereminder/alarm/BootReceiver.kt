package com.example.smartmedicinereminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smartmedicinereminder.data.db.ReminderDatabase
import com.example.smartmedicinereminder.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "📱 Device booted → rescheduling all reminders")

            // Run in background thread
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = ReminderDatabase.getInstance(context).reminderDao()
                    val repository = ReminderRepository(dao)

                    val reminders = repository.getAllRemindersWithDoses().first()
                    var count = 0

                    reminders.forEach { rwd ->
                        val reminderId = rwd.reminder.id
                        val reminderName = rwd.reminder.name

                        rwd.doses.forEachIndexed { index, dose ->
                            val triggerAtMillis = computeNextTriggerMillis(dose.time)

                            val isSyrup = !rwd.reminder.syrupImageUri.isNullOrEmpty()

                            // Reschedule alarm using AlarmManager
                            AlarmScheduler.scheduleExactAlarm(
                                context = context,
                                reminderId = reminderId,
                                doseIndex = index,
                                triggerAtMillis = triggerAtMillis,
                                reminderName = reminderName,
                                qty = dose.quantity,
                                frontImageUri = if (!isSyrup) rwd.reminder.frontImageUri else null,
                                backImageUri = if (!isSyrup) rwd.reminder.backImageUri else null,
                                syrupImageUri = if (isSyrup) rwd.reminder.syrupImageUri else null
                            )
                            count++
                        }
                    }
                    Log.d(TAG, "✅ Rescheduled $count alarm(s) after boot")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ BootReceiver failed: ${e.message}", e)
                }
            }
        }
    }

    private fun computeNextTriggerMillis(timeStr: String?): Long {
        val parts = timeStr?.split(":") ?: emptyList()
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time has already passed today → schedule for tomorrow
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.timeInMillis
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}

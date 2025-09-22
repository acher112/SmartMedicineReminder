package com.example.smartmedicinereminder.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun scheduleExactAlarm(
        context: Context,
        reminderId: Int,
        doseIndex: Int,
        triggerAtMillis: Long,
        reminderName: String,
        qty: Int,
        frontImageUri: String? = null,
        backImageUri: String? = null,
        syrupImageUri: String? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminderId)
            putExtra("REMINDER_NAME", reminderName)
            putExtra("DOSE_INDEX", doseIndex)
            putExtra("DOSE_QTY", qty)
            putExtra("FRONT_IMAGE_URI", frontImageUri)
            putExtra("BACK_IMAGE_URI", backImageUri)
            putExtra("SYRUP_IMAGE_URI", syrupImageUri)
        }

        val reqCode = reminderId * 100 + doseIndex
        val pending = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pending
                    )
                } else {
                    Log.w(TAG, "⚠️ Exact alarms not permitted → ask user to allow in settings")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pending
                )
            }

            Log.d(
                TAG,
                "✅ Scheduled alarm req=$reqCode → $reminderName (qty=$qty) at $triggerAtMillis"
            )
        } catch (se: SecurityException) {
            Log.e(TAG, "❌ SecurityException scheduling alarm: ${se.message}", se)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception scheduling alarm: ${e.message}", e)
        }
    }

    fun cancelAlarm(context: Context, reminderId: Int, doseIndex: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val reqCode = reminderId * 100 + doseIndex

        val pending = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pending != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pending)
            pending.cancel()
            Log.d(TAG, "⏹️ Cancelled alarm req=$reqCode")
        } else {
            Log.d(TAG, "⚠️ No alarm found to cancel req=$reqCode")
        }
    }
}

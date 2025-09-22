package com.example.smartmedicinereminder.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartmedicinereminder.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        private const val CHANNEL_ID = "alarm_fullscreen_channel"
        private const val NOTIF_ID_BASE = 10000 // base id; we'll add reminderId to avoid collisions
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicineName") ?: "Medicine"
        val dosage = intent.getStringExtra("dosage") ?: "1"
        val imagePath = intent.getStringExtra("imagePath") ?: ""
        val reminderId = intent.getIntExtra("REMINDER_ID", medicineName.hashCode())

        Log.d(TAG, "⏰ Alarm fired → $medicineName ($dosage) image=$imagePath id=$reminderId")

        // 1) Try to launch AlarmActivity directly (may be blocked on some devices)
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("medicineName", medicineName)
            putExtra("dosage", dosage)
            putExtra("imagePath", imagePath)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        try {
            context.startActivity(alarmIntent)
            Log.d(TAG, "✅ Started AlarmActivity via startActivity()")
        } catch (t: Throwable) {
            Log.w(TAG, "⚠️ Failed to start activity directly: ${t.message}")
        }

        // 2) Post a full-screen notification as a robust fallback.
        // This increases the chance the system will bring the alarm UI in front.
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm (Full screen)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full-screen alarms for medicine reminders"
                enableLights(true)
                lightColor = Color.RED
                importance = NotificationManager.IMPORTANCE_HIGH
                setShowBadge(false)
            }
            nm.createNotificationChannel(channel)
        }

        // PendingIntent that opens AlarmActivity (used as fullScreenIntent)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            reminderId, // unique per reminder
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // A smaller content intent so tapping the notification does something sensible
        val contentPending = PendingIntent.getActivity(
            context,
            reminderId + 1,
            Intent(context, AlarmActivity::class.java).apply {
                putExtra("medicineName", medicineName)
                putExtra("dosage", dosage)
                putExtra("imagePath", imagePath)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // make sure this exists
            .setContentTitle("Take your medicine")
            .setContentText("Time to take $dosage of $medicineName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true) // <= the key: full screen
            .setContentIntent(contentPending)
            .build()

        try {
            nm.notify(NOTIF_ID_BASE + (reminderId and 0xFFFF), notif)
            Log.d(TAG, "✅ Posted full-screen notification for alarm (notifId=${NOTIF_ID_BASE + (reminderId and 0xFFFF)})")
        } catch (t: Throwable) {
            Log.e(TAG, "❌ Failed to post alarm notification: ${t.message}", t)
        }
    }
}

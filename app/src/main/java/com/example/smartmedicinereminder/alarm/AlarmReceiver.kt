package com.example.smartmedicinereminder.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartmedicinereminder.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        private const val CHANNEL_ID = "hidden_alarm_channel"
        private const val NOTIF_ID = 99999 // single hidden notification id
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("REMINDER_ID", 0)
        val reminderName = intent.getStringExtra("REMINDER_NAME") ?: "Medicine"
        val doseQty = intent.getIntExtra("DOSE_QTY", 1)
        val frontImage = intent.getStringExtra("FRONT_IMAGE_URI")

        Log.d(TAG, "⏰ Alarm fired → $reminderName ($doseQty)")

        // Intent for full-screen activity
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("REMINDER_ID", reminderId)
            putExtra("REMINDER_NAME", reminderName)
            putExtra("DOSE_QTY", doseQty)
            putExtra("FRONT_IMAGE_URI", frontImage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // Required pending intent for full-screen launch
        val fullScreenPending = PendingIntent.getActivity(
            context,
            reminderId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hidden Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null) // no sound here (we play manually in AlarmActivity)
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
                description = "Channel used only for launching full-screen alarms"
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            nm.createNotificationChannel(channel)
        }

        // Build invisible / silent notification (only to trigger fullscreen)
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentTitle(null)
            .setContentText(null)
            .setFullScreenIntent(fullScreenPending, true)
            .setOngoing(false)
            .setAutoCancel(true)
            .setSound(null)
            .build()

        nm.notify(NOTIF_ID, notif) // post → triggers AlarmActivity

        try {
            // Directly start activity too (extra guarantee)
            context.startActivity(alarmIntent)
            Log.d(TAG, "✅ Started AlarmActivity directly")
        } catch (t: Throwable) {
            Log.w(TAG, "⚠️ Failed direct start: ${t.message}")
        }
    }
}

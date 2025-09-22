package com.example.smartmedicinereminder.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.smartmedicinereminder.R

class ReminderService : Service() {

    companion object {
        const val CHANNEL_ID = "ReminderChannel"
        const val NOTIF_ID = 2001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val medicineName = intent?.getStringExtra("MEDICINE_NAME") ?: "Medicine Reminder"
        val doseQty = intent?.getIntExtra("DOSE_QTY", 1) ?: 1

        // ✅ PendingIntent to open app when notification tapped
        val notifIntent = Intent(this, Class.forName("com.example.smartmedicinereminder.MainActivity"))
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notifIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ✅ Foreground notification (silent)
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Medicine Reminder")
            .setContentText("Take $doseQty tablet(s) of $medicineName")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW) // no sound
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent) // open app on tap
            .setOngoing(true) // makes it persistent until service stops
            .build()

        startForeground(NOTIF_ID, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medicine Reminder Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows medicine reminder info (no sound)"
                setSound(null, null) // ✅ explicitly disables sound
                enableVibration(false) // ✅ disable vibration
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

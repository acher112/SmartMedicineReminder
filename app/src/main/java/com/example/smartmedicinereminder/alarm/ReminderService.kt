package com.example.smartmedicinereminder.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.smartmedicinereminder.R

class ReminderService : Service() {

    companion object {
        const val CHANNEL_ID = "ReminderChannel"
        const val NOTIF_ID = 2001
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val medicineName = intent?.getStringExtra("MEDICINE_NAME") ?: "Medicine"
        val doseQty = intent?.getIntExtra("DOSE_QTY", 1) ?: 1

        // ✅ Launch full-screen AlarmActivity
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("DOSE_QTY", doseQty)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(alarmIntent)

        // ✅ PendingIntent for notification (opens app if tapped)
        val notifIntent = Intent(this, AlarmActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notifIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ✅ Foreground notification (silent)
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Medicine Reminder")
            .setContentText("Take $doseQty tablet(s) of $medicineName")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notification)

        // ✅ Start ringtone (looping)
        mediaPlayer = MediaPlayer.create(this, R.raw.reminder_sound)?.apply {
            isLooping = true
            start()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
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
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

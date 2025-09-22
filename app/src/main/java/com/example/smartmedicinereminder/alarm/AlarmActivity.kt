package com.example.smartmedicinereminder.alarm

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartmedicinereminder.R
import java.io.File

class AlarmActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Wake screen + show on lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_alarm)

        // ✅ Extract reminder data
        val reminderName = intent.getStringExtra("REMINDER_NAME") ?: getString(R.string.app_name)
        val doseQty = intent.getIntExtra("DOSE_QTY", 1)
        val imagePath = intent.getStringExtra("FRONT_IMAGE_URI").orEmpty()

        // ✅ Bind views
        val tvTitle: TextView = findViewById(R.id.tvTitle)
        val tvDose: TextView = findViewById(R.id.tvDose)
        val ivMedicine: ImageView = findViewById(R.id.ivMedicine)
        val btnConfirm: Button = findViewById(R.id.btnConfirm)

        // ✅ Set medicine info
        tvTitle.text = "Time for $reminderName"
        tvDose.text = "Take $doseQty dose(s)"

        // ✅ Show medicine image if available
        val file = File(imagePath)
        if (file.exists()) {
            ivMedicine.setImageURI(Uri.fromFile(file))
        } else {
            ivMedicine.setImageResource(R.drawable.ic_notification) // fallback icon
        }

        // ✅ Play alarm sound in loop
        mediaPlayer = MediaPlayer.create(this, R.raw.reminder_sound)?.apply {
            isLooping = true
            start()
        }

        // ✅ Confirm button
        btnConfirm.setOnClickListener {
            stopRingtone()

            // 🔴 Stop foreground service → removes persistent notification
            val stopIntent = Intent(this, ReminderService::class.java)
            stopService(stopIntent)

            finish()
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
            } catch (_: IllegalStateException) {
            }
            player.release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
    }
}

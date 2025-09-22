package com.example.smartmedicinereminder.alarm

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

        // ✅ Show activity on lock screen + wake device
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
        val medicineName = intent.getStringExtra("medicineName") ?: "Medicine"
        val dosage = intent.getStringExtra("dosage") ?: "1"
        val imagePath = intent.getStringExtra("imagePath").orEmpty()

        // ✅ Bind views
        val tvTitle: TextView = findViewById(R.id.tvTitle)
        val tvDose: TextView = findViewById(R.id.tvDose)
        val ivMedicine: ImageView = findViewById(R.id.ivMedicine)
        val btnConfirm: Button = findViewById(R.id.btnConfirm)

        // ✅ Set UI text (using existing hardcoded / current strings)
        tvTitle.text = "Time to take: $medicineName"
        tvDose.text = "Dose: $dosage"

        // ✅ Show medicine image or fallback icon
        val file = File(imagePath)
        if (file.exists()) {
            ivMedicine.setImageURI(Uri.fromFile(file))
        } else {
            ivMedicine.setImageResource(R.drawable.ic_notification) // fallback icon
        }

        // ✅ Start looping alarm sound
        mediaPlayer = MediaPlayer.create(this, R.raw.reminder_sound)?.apply {
            isLooping = true
            start()
        }

        // ✅ Confirm button stops alarm + closes activity
        btnConfirm.setOnClickListener {
            stopRingtone()
            // TODO: Add guardian message or logging here
            finish()
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
            } catch (e: IllegalStateException) {
                // Ignore if player already released
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

package com.example.smartmedicinereminder.data.model

import androidx.annotation.StringRes

data class Option(
    val key: String,                // Stable value saved in DB ("Tablet" / "Syrup")
    @StringRes val label: Int       // Translated label (R.string.tablet / R.string.syrup)
)

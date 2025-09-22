package com.example.smartmedicinereminder.utils

import android.content.Context

object FontSizeManager {
    private const val PREFS = "AppPrefs"
    private const val KEY = "FONT_SIZE"

    // 🔹 Save selected font size level (1 = Small, 2 = Medium, 3 = Large)
    fun saveFontSize(context: Context, level: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY, level)
            .apply()
    }

    // 🔹 Load saved font size level, default = Small (1)
    fun loadFontSize(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY, 1) // ✅ Default = Small
    }

    // 🔹 Convert level → SP values
    fun getFontSizeSp(level: Int): Float {
        return when (level) {
            1 -> 14f // Small
            2 -> 18f // Medium
            3 -> 22f // Large
            else -> 14f
        }
    }

    // 🔹 Helper: get currently applied font size
    fun getFontSize(context: Context): Float {
        val level = loadFontSize(context)
        return getFontSizeSp(level)
    }
}

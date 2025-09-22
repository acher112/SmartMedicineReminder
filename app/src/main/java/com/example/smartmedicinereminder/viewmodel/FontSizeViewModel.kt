package com.example.smartmedicinereminder.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.smartmedicinereminder.utils.FontSizeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FontSizeViewModel(private val context: Context) : ViewModel() {

    // store font level as state
    private val _fontSize = MutableStateFlow(FontSizeManager.loadFontSize(context))
    val fontSize: StateFlow<Int> = _fontSize

    fun saveFontSize(level: Int) {
        FontSizeManager.saveFontSize(context, level)
        _fontSize.value = level
    }

    // ✅ Now we control actual text sizes here
    fun getFontSizeSp(level: Int): Float {
        return when (level) {
            1 -> 14f  // Small (default)
            2 -> 18f  // Medium
            3 -> 22f  // Large
            else -> 14f
        }
    }
}

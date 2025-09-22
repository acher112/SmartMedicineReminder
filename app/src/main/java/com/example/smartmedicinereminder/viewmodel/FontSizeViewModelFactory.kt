package com.example.smartmedicinereminder.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FontSizeViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FontSizeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FontSizeViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

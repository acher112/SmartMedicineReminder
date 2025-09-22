package com.example.smartmedicinereminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartmedicinereminder.data.model.Dose
import com.example.smartmedicinereminder.data.model.MedicineReminder
import com.example.smartmedicinereminder.data.model.ReminderWithDoses
import com.example.smartmedicinereminder.data.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ReminderListViewModel(private val repository: ReminderRepository) : ViewModel() {

    // 🔹 All reminders with their doses (live updates using Flow)
    val reminders: Flow<List<ReminderWithDoses>> = repository.getAllRemindersWithDoses()

    // 🔹 Delete a single dose (by object)
    fun deleteDose(dose: Dose) = viewModelScope.launch {
        repository.deleteDoseById(dose.doseId)
    }

    // 🔹 Delete a single dose (by ID)
    fun deleteDoseById(doseId: Int) = viewModelScope.launch {
        repository.deleteDoseById(doseId)
    }

    // 🔹 Delete a full reminder (removes reminder + all doses)
    fun deleteReminder(reminder: MedicineReminder) = viewModelScope.launch {
        repository.deleteReminder(reminder)
    }

    // 🔹 Update a reminder + its doses (for edit feature)
    fun updateReminder(reminder: MedicineReminder, doses: List<Dose>) = viewModelScope.launch {
        repository.updateReminder(reminder, doses)
    }
}

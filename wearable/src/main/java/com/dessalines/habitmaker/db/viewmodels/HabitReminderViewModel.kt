package com.dessalines.habitmaker.db.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dessalines.habitmaker.db.HabitReminderInsert
import com.dessalines.habitmaker.db.HabitReminderRepository

class HabitReminderViewModel(
    private val repository: HabitReminderRepository,
) : ViewModel() {
    fun listForHabitSync(habitId: Int) = repository.listForHabitSync(habitId)

    fun insert(habitReminder: HabitReminderInsert) = repository.insert(habitReminder)

    fun delete(habitId: Int) = repository.delete(habitId)
}

class HabitReminderViewModelFactory(
    private val repository: HabitReminderRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitReminderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

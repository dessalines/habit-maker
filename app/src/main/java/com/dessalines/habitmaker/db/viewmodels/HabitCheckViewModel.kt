package com.dessalines.habitmaker.db.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dessalines.habitmaker.db.HabitCheckInsert
import com.dessalines.habitmaker.db.HabitCheckRepository

class HabitCheckViewModel(
    private val repository: HabitCheckRepository,
) : ViewModel() {
    fun listForHabit(habitId: Int) = repository.listForHabit(habitId)

    fun listForHabitSync(habitId: Int) = repository.listForHabitSync(habitId)

    fun insert(habitCheck: HabitCheckInsert) = repository.insert(habitCheck)

    fun deleteForDay(
        habitId: Int,
        checkTime: Long,
    ) = repository.deleteForDay(habitId, checkTime)
}

class HabitCheckViewModelFactory(
    private val repository: HabitCheckRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitCheckViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitCheckViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

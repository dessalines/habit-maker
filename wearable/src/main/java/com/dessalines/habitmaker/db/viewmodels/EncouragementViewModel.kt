package com.dessalines.habitmaker.db.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dessalines.habitmaker.db.EncouragementInsert
import com.dessalines.habitmaker.db.EncouragementRepository

class EncouragementViewModel(
    private val repository: EncouragementRepository,
) : ViewModel() {
    fun listForHabitSync(habitId: Int) = repository.listForHabitSync(habitId)

    fun getRandomForHabit(habitId: Int) = repository.getRandomForHabit(habitId)

    fun deleteForHabit(habitId: Int) = repository.deleteForHabit(habitId)

    fun insert(encouragement: EncouragementInsert) = repository.insert(encouragement)
}

class EncouragementViewModelFactory(
    private val repository: EncouragementRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EncouragementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EncouragementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


package com.dessalines.habitmaker.db.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.HabitInsert
import com.dessalines.habitmaker.db.HabitRepository
import com.dessalines.habitmaker.db.HabitUpdate
import com.dessalines.habitmaker.db.HabitUpdateStats
import kotlinx.coroutines.launch
import kotlin.jvm.java

class HabitViewModel(
    private val repository: HabitRepository,
) : ViewModel() {
    val getAll = repository.getAll

    val getAllSync = repository.getAllSync

    fun getById(id: Int) = repository.getById(id)

    fun getByIdSync(id: Int) = repository.getByIdSync(id)

    fun insert(habit: HabitInsert): Long {
        val insertedId = repository.insert(habit)
        return insertedId
    }

    fun update(habit: HabitUpdate) =
        viewModelScope.launch {
            repository.update(habit)
        }

    fun updateStats(
        habit: HabitUpdateStats,
        updateDataClient: Boolean,
    ) = viewModelScope.launch {
        repository.updateStats(habit)
    }

    fun delete(habit: Habit) =
        viewModelScope.launch {
            repository.delete(habit)
        }
}

class HabitViewModelFactory(
    private val repository: HabitRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

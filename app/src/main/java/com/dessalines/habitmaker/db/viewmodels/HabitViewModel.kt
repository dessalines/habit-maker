package com.dessalines.habitmaker.db.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.HabitInsert
import com.dessalines.habitmaker.db.HabitRepository
import com.dessalines.habitmaker.db.HabitUpdate
import com.dessalines.habitmaker.db.HabitUpdateStats
import com.dessalines.habitmaker.utils.sendDataToOtherDevices
import com.google.android.gms.wearable.DataClient
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.jvm.java

class HabitViewModel(
    private val repository: HabitRepository,
) : ViewModel() {
    val getAll = repository.getAll

    val getAllSync = repository.getAllSync

    fun getById(id: Int) = repository.getById(id)

    fun getByIdSync(id: Int) = repository.getByIdSync(id)

    fun insert(habit: HabitInsert, dataClient: DataClient): Long {
        val insertedId = repository.insert(habit)
        val inserted = habit.copy(id = insertedId.toInt())
        viewModelScope.launch {
            sendDataToOtherDevices(Json.encodeToString(inserted), "HabitInsert", dataClient)
        }
        return insertedId
    }

    fun update(habit: HabitUpdate, dataClient: DataClient) =
        viewModelScope.launch {
            sendDataToOtherDevices(Json.encodeToString(habit), "HabitUpdate", dataClient)
            repository.update(habit)
        }

    fun updateStats(habit: HabitUpdateStats, dataClient: DataClient) =
        viewModelScope.launch {
            sendDataToOtherDevices(Json.encodeToString(habit), "HabitUpdateStats", dataClient)
            repository.updateStats(habit)
        }

    fun delete(habit: Habit, dataClient: DataClient) = viewModelScope.launch {
            sendDataToOtherDevices(Json.encodeToString(habit), "HabitDelete", dataClient)
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

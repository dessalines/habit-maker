package com.dessalines.habitmaker.db.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dessalines.habitmaker.datalayer.sendDataToOtherDevices
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.HabitInsert
import com.dessalines.habitmaker.db.HabitRepository
import com.dessalines.habitmaker.db.HabitUpdate
import com.dessalines.habitmaker.db.HabitUpdateStats
import com.google.android.gms.wearable.DataClient
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.jvm.java

class HabitViewModel(
    private val repository: HabitRepository,
    private val dataClient: DataClient,
) : ViewModel() {
    val getAll = repository.getAll

    val getAllSync = repository.getAllSync

    fun getById(id: Int) = repository.getById(id)

    fun getByIdSync(id: Int) = repository.getByIdSync(id)

    fun insert(
        habit: HabitInsert,
    ): Long {
        val insertedId = repository.insert(habit)
        val inserted = habit.copy(id = insertedId.toInt())
        viewModelScope.launch {
            dataClient.sendDataToOtherDevices(Json.encodeToString(inserted), "HabitInsert")
        }
        return insertedId
    }

    fun update(
        habit: HabitUpdate,
    ) = viewModelScope.launch {
        repository.update(habit)
        dataClient.sendDataToOtherDevices(Json.encodeToString(habit), "HabitUpdate")
    }

    fun updateStats(
        habit: HabitUpdateStats,
        updateDataClient: Boolean,
    ) = viewModelScope.launch {
        repository.updateStats(habit)
        if (updateDataClient) {
            dataClient.sendDataToOtherDevices(Json.encodeToString(habit), "HabitUpdateStats")
        }
    }

    fun delete(
        habit: Habit,
    ) = viewModelScope.launch {
        repository.delete(habit)
        dataClient.sendDataToOtherDevices(Json.encodeToString(habit), "HabitDelete")
    }
}

class HabitViewModelFactory(
    private val repository: HabitRepository,
    private val dataClient: DataClient,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(repository, dataClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

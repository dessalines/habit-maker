package com.dessalines.habitmaker.db.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dessalines.habitmaker.datalayer.sendDataToOtherDevices
import com.dessalines.habitmaker.db.HabitCheckDelete
import com.dessalines.habitmaker.db.HabitCheckInsert
import com.dessalines.habitmaker.db.HabitCheckRepository
import com.google.android.gms.wearable.DataClient
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class HabitCheckViewModel(
    private val repository: HabitCheckRepository,
    private val dataClient: DataClient,
) : ViewModel() {
    val getAllSync = repository.getAllSync

    fun listForHabit(habitId: Int) = repository.listForHabit(habitId)

    fun listForHabitSync(habitId: Int) = repository.listForHabitSync(habitId)

    fun insert(
        habitCheck: HabitCheckInsert,
    ): Long {
        val insertedId = repository.insert(habitCheck)
        val inserted = habitCheck.copy(id = insertedId.toInt())
        viewModelScope.launch {
            dataClient.sendDataToOtherDevices(Json.encodeToString(inserted), "HabitCheckInsert")
        }
        return insertedId
    }

    fun deleteForDay(
        habitCheck: HabitCheckDelete,
    ) {
        repository.deleteForDay(habitCheck)
        viewModelScope.launch {
            dataClient.sendDataToOtherDevices(Json.encodeToString(habitCheck), "HabitCheckDelete")
        }
    }
}

class HabitCheckViewModelFactory(
    private val repository: HabitCheckRepository,
    private val dataClient: DataClient,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitCheckViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitCheckViewModel(repository, dataClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.dessalines.habitmaker.db.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dessalines.habitmaker.db.HabitCheckDelete
import com.dessalines.habitmaker.db.HabitCheckInsert
import com.dessalines.habitmaker.db.HabitCheckRepository
import com.dessalines.habitmaker.utils.sendDataToOtherDevices
import com.google.android.gms.wearable.DataClient
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class HabitCheckViewModel(
    private val repository: HabitCheckRepository,
) : ViewModel() {
    fun listForHabit(habitId: Int) = repository.listForHabit(habitId)

    fun listForHabitSync(habitId: Int) = repository.listForHabitSync(habitId)

    fun insert(
        habitCheck: HabitCheckInsert,
        dataClient: DataClient,
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
        dataClient: DataClient,
    ) {
        repository.deleteForDay(habitCheck)
        viewModelScope.launch {
            dataClient.sendDataToOtherDevices(Json.encodeToString(habitCheck), "HabitCheckDelete")
        }
    }
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

/**
 * Checks / toggles a habit for a given check time.
 *
 * If it already exists, it deletes the row in order to toggle it.
 *
 * returns true if successful / check, false for deleted check.
 */
fun checkHabitForDay(
    habitId: Int,
    checkTime: Long,
    habitCheckViewModel: HabitCheckViewModel,
    dataClient: DataClient,
) {
    val data =
        HabitCheckInsert(
            habitId = habitId,
            checkTime = checkTime,
        )
    val success = habitCheckViewModel.insert(data, dataClient)

    // If its -1, that means that its already been checked for today,
    // and you actually need to delete it to toggle
    if (success == -1L) {
        habitCheckViewModel.deleteForDay(HabitCheckDelete(habitId, checkTime), dataClient)
    }
}

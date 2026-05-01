package com.dessalines.habitmaker.db.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dessalines.habitmaker.datalayer.sendDataToOtherDevices
import com.dessalines.habitmaker.db.HabitCheckDelete
import com.dessalines.habitmaker.db.HabitCheckInsert
import com.dessalines.habitmaker.db.HabitCheckRepository
import com.dessalines.habitmaker.db.HabitUpdateStats
import com.dessalines.habitmaker.db.utils.HabitCheckDeleteAndStatsUpdate
import com.dessalines.habitmaker.db.utils.HabitCheckInsertAndStatsUpdate
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

    fun insert(habitCheck: HabitCheckInsert): Long {
        val insertedId = repository.insert(habitCheck)

        return insertedId
    }

    fun sendHabitCheckInsertAndStatsUpdate(
        insertedId: Long,
        habitCheck: HabitCheckInsert,
        stats: HabitUpdateStats,
    ) {
        val inserted = habitCheck.copy(id = insertedId.toInt())
        val data =
            HabitCheckInsertAndStatsUpdate(
                check = inserted,
                stats = stats,
            )

        viewModelScope.launch {
            dataClient.sendDataToOtherDevices(Json.encodeToString(data), "HabitCheckInsert")
        }
    }

    fun deleteForDay(habitCheck: HabitCheckDelete) {
        repository.deleteForDay(habitCheck)
    }

    fun sendHabitCheckDeleteAndStatsUpdate(
        habitCheckDelete: HabitCheckDelete,
        stats: HabitUpdateStats,
    ) {
        val data =
            HabitCheckDeleteAndStatsUpdate(
                check = habitCheckDelete,
                stats = stats,
            )

        viewModelScope.launch {
            dataClient.sendDataToOtherDevices(Json.encodeToString(data), "HabitCheckDelete")
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

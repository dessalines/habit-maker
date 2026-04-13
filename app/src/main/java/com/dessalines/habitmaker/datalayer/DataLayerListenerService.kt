package com.dessalines.habitmaker.datalayer

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.dessalines.habitmaker.db.AppDB
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.HabitCheckDelete
import com.dessalines.habitmaker.db.HabitCheckInsert
import com.dessalines.habitmaker.db.HabitCheckRepository
import com.dessalines.habitmaker.db.HabitInsert
import com.dessalines.habitmaker.db.HabitRepository
import com.dessalines.habitmaker.db.HabitUpdateStats
import com.dessalines.habitmaker.db.utils.BulkInsert
import com.dessalines.habitmaker.db.viewmodels.HabitCheckViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitViewModel
import com.dessalines.habitmaker.utils.TAG
import com.dessalines.habitmaker.utils.isAvailable
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException
import kotlin.getValue

/**
 * A data holder describing a client event.
 */
data class Event(
    val className: String,
    val data: String,
)

class DataLayerListenerService : WearableListenerService() {
    private val database by lazy { AppDB.getDatabase(this) }
    val habitRepository by lazy { HabitRepository(database.habitDao()) }
    val habitCheckRepository by lazy { HabitCheckRepository(database.habitCheckDao()) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        dataEvents.forEach { dataEvent ->
            val uri = dataEvent.dataItem.uri
            when (uri.path) {
                MESSAGE_PATH -> {
                    val dataMapItem = DataMapItem.fromDataItem(dataEvent.dataItem)
                    val data = dataMapItem.dataMap.getString(DATA_KEY)
                    val className = dataMapItem.dataMap.getString(CLASS_KEY)

                    val event =
                        Event(
                            data = data.orEmpty(),
                            className = className.orEmpty(),
                        )
                    Log.d(TAG, "event received: $event")

                    writeEventToDb(event)
                }
            }
        }
    }

    fun writeEventToDb(event: Event) {
        scope.launch {
            // Data client is null for all these, because its not a send
            when (event.className) {
                "HabitUpdateStats" -> {
                    val habit = Json.decodeFromString<HabitUpdateStats>(event.data)
                    habitRepository.updateStats(habit)
                }

                "HabitDelete" -> {
                    val habit = Json.decodeFromString<Habit>(event.data)
                    habitRepository.delete(habit)
                }

                "HabitCheckInsert" -> {
                    val habitCheck = Json.decodeFromString<HabitCheckInsert>(event.data)
                    habitCheckRepository.insert(habitCheck)
                }

                "HabitCheckDelete" -> {
                    val habitCheck = Json.decodeFromString<HabitCheckDelete>(event.data)
                    habitCheckRepository.deleteForDay(habitCheck)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val MESSAGE_PATH = "/message"

        const val DATA_KEY = "data"
        const val CLASS_KEY = "class"
    }
}

suspend fun DataClient.sendDataToOtherDevices(
    data: String,
    className: String,
) {
    if (isAvailable(this)) {
        try {
            val request =
                PutDataMapRequest
                    .create(DataLayerListenerService.MESSAGE_PATH)
                    .apply {
                        dataMap.putString(DataLayerListenerService.CLASS_KEY, className)
                        dataMap.putString(DataLayerListenerService.DATA_KEY, data)
                    }.asPutDataRequest()
//                    .setUrgent()
            this
                .putDataItem(request)
                .await()

            Log.d(TAG, "DataItem saved: class: $className, $data")
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }
}

fun syncDBtoOtherDevices(
    habitViewModel: HabitViewModel,
    habitCheckViewModel: HabitCheckViewModel,
    dataClient: DataClient,
    scope: LifecycleCoroutineScope,
) {
    scope.launch {
        val habitInserts =
            habitViewModel.getAllSync.map { habit ->
                val habitInsert =
                    HabitInsert(
                        id = habit.id,
                        name = habit.name,
                        frequency = habit.frequency,
                        timesPerFrequency = habit.timesPerFrequency,
                        notes = habit.notes,
                        archived = habit.archived,
                        context = habit.context,
                    )
                val statsUpdate =
                    HabitUpdateStats(
                        id = habit.id,
                        points = habit.points,
                        score = habit.score,
                        streak = habit.streak,
                        completed = habit.completed,
                        lastStreakTime = habit.lastStreakTime,
                        lastCompletedTime = habit.lastCompletedTime,
                    )
                Pair(habitInsert, statsUpdate)
            }

        val checkInserts =
            habitCheckViewModel.getAllSync.map { check ->
                HabitCheckInsert(
                    id = check.id,
                    habitId = check.habitId,
                    checkTime = check.checkTime,
                )
            }
        val bulkInsert = BulkInsert(habitInserts, checkInserts)
        dataClient.sendDataToOtherDevices(Json.encodeToString<BulkInsert>(bulkInsert), "BulkInsert")
    }
}

package com.dessalines.habitmaker

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.HabitInsert
import com.dessalines.habitmaker.db.HabitUpdate
import com.dessalines.habitmaker.db.HabitUpdateStats
import com.dessalines.habitmaker.db.viewmodels.HabitViewModel
import com.dessalines.habitmaker.utils.TAG
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
) : AndroidViewModel(application),
    DataClient.OnDataChangedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private val _events = mutableStateListOf<Event>()
    val _eventFlow = MutableStateFlow<Event?>(null)

    /**
     * The list of events from the clients.
     */
    val events: List<Event> = _events
    val eventFlow: MutableStateFlow<Event?> = _eventFlow

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {

        // Add all events to the event log
        _events.addAll(
            dataEvents.map { dataEvent ->
                val title =
                    when (dataEvent.type) {
                        DataEvent.TYPE_CHANGED -> "changed"
                        DataEvent.TYPE_DELETED -> "deleted"
                        else -> "unknown"
                    }
                val text =
                    when (dataEvent.dataItem.uri.path) {
                        DataLayerListenerService.MESSAGE_PATH -> {
                            DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.getString(
                                DataLayerListenerService.MESSAGE_KEY
                            )
                        }

                        else -> {
                            null
                        }
                    }

                val className =
                    when (dataEvent.dataItem.uri.path) {
                        DataLayerListenerService.MESSAGE_PATH -> {
                            DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.getString(
                                DataLayerListenerService.CLASS_KEY
                            )
                        }

                        else -> {
                            null
                        }
                    }

                val event_ = Event(
                    title = title,
                    text = text ?: dataEvent.dataItem.toString(),
                    className = className,
                )
                _eventFlow.value = event_
                event_
            },
        )
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        _events.add(
            Event(
                title = "capability",
                text = capabilityInfo.toString(),
                className = null,
            ),
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY]!!

                    MainViewModel(
                        application,
                    )
                }
            }
    }
}

/**
 * A data holder describing a client event.
 */
data class Event(
    val title: String,
    val text: String,
    val className: String?,
)

fun listenForOtherDeviceDbChanges(
    scope: LifecycleCoroutineScope,
    mainViewModel: MainViewModel,
    habitViewModel: HabitViewModel
) {

    scope.launch {
        mainViewModel.eventFlow.collect { event ->
            Log.d(TAG, "event flow: $event")

            when (event?.className) {
                "HabitInsert" -> {
                    val habit = Json.decodeFromString<HabitInsert>(event.text)
                    habitViewModel.insert(habit)
                }

                "HabitUpdate" -> {
                    val habit = Json.decodeFromString<HabitUpdate>(event.text)
                    habitViewModel.update(habit)

                }

                "HabitUpdateStats" -> {
                    val habit = Json.decodeFromString<HabitUpdateStats>(event.text)
                    habitViewModel.updateStats(habit)

                }

                "HabitDelete" -> {
                    val habit = Json.decodeFromString<Habit>(event.text)
                    habitViewModel.delete(habit)
                }
            }
        }
    }
}


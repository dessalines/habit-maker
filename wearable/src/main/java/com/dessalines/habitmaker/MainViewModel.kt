package com.dessalines.habitmaker

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem

class MainViewModel(
    application: Application,
) : AndroidViewModel(application),
    DataClient.OnDataChangedListener,
    CapabilityClient.OnCapabilityChangedListener {
    private val _events = mutableStateListOf<Event>()
//        private val _dataItems = mutableStateListOf<DataItem>()

    /**
     * The list of events from the clients.
     */
    val events: List<Event> = _events
//    val dataItems: List<DataItem> = _dataItems

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
                val msg =
                    when (dataEvent.dataItem.uri.path) {
                        DataLayerListenerService.MESSAGE_PATH -> {
                            DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.getString(DataLayerListenerService.MESSAGE_KEY)
                        }

                        else -> {
                            null
                        }
                    }
                Event(
                    title = title,
                    text = msg ?: dataEvent.dataItem.toString(),
                )
            },
        )
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        _events.add(
            Event(
                title = "capability",
                text = capabilityInfo.toString(),
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
)

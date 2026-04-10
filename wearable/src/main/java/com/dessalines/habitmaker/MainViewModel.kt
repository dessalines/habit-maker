package com.dessalines.habitmaker

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) :
    AndroidViewModel(application),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private val _events = mutableStateListOf<Event>()

    /**
     * The list of events from the clients.
     */
    val events: List<Event> = _events

    /**
     * The currently received image (if any), available to display.
     */
    var image by mutableStateOf<Bitmap?>(null)
        private set

    private var loadPhotoJob: Job = Job().apply { complete() }

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        // Add all events to the event log
        _events.addAll(
            dataEvents.map { dataEvent ->
                val title = when (dataEvent.type) {
                    DataEvent.TYPE_CHANGED -> "changed"
                    DataEvent.TYPE_DELETED -> "deleted"
                    else -> "unknown"
                }
                Event(
                    title = title,
                    text = dataEvent.dataItem.toString()

                )
            }
        )
        // Do additional work for specific events
        dataEvents.forEach { dataEvent ->
            when (dataEvent.type) {
                DataEvent.TYPE_CHANGED -> {
                    when (dataEvent.dataItem.uri.path) {
                        DataLayerListenerService.IMAGE_PATH -> {
                            loadPhotoJob.cancel()
                            loadPhotoJob = viewModelScope.launch {
                                image = loadBitmap(
                                    DataMapItem.fromDataItem(dataEvent.dataItem)
                                        .dataMap
                                        .getAsset(DataLayerListenerService.IMAGE_KEY)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        _events.add(
            Event(
                title = "message",
                text = messageEvent.toString()
            )
        )
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        _events.add(
            Event(
                title = "capability",
                text = capabilityInfo.toString()
            )
        )
    }

    private suspend fun loadBitmap(asset: Asset?): Bitmap? {
        if (asset == null) return null
        val response =
            Wearable.getDataClient(getApplication<Application>()).getFdForAsset(asset).await()
        return response.inputStream.use { inputStream ->
            withContext(Dispatchers.IO) {
                BitmapFactory.decodeStream(inputStream)
            }
        }
    }
    companion object {
        private const val TAG = "MainViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY]!!
                MainViewModel(
                    application
                )
            }
        }
    }
}

/**
 * A data holder describing a client event.
 */
data class Event(val title: String, val text: String)
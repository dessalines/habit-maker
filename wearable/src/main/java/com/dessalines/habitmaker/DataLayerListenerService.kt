package com.dessalines.habitmaker

import android.annotation.SuppressLint
import android.util.Log
import com.dessalines.habitmaker.utils.TAG
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class DataLayerListenerService : WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        dataEvents.forEach { dataEvent ->
            val uri = dataEvent.dataItem.uri
            when (uri.path) {
                MESSAGE_PATH -> {
                    val dataMapItem = DataMapItem.fromDataItem(dataEvent.dataItem)
                    val msg = dataMapItem.dataMap.getString(MESSAGE_KEY)
                    Log.d(TAG, "msg: $msg")
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
        const val MESSAGE_KEY = "message"
    }
}

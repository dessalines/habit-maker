package com.dessalines.habitmaker.utils

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.tasks.await
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

const val TAG = "com.habitmaker"

suspend fun isAvailable(api: GoogleApi<*>): Boolean =
    try {
        GoogleApiAvailability
            .getInstance()
            .checkApiAvailability(api)
            .await()

        true
    } catch (_: AvailabilityException) {
        Log.d(
            TAG,
            "${api.javaClass.simpleName} API is not available in this device.",
        )
        false
    }

suspend fun DataClient.sendDataToOtherDevices(
    data: String,
    className: String,
) {
    if (isAvailable(this)) {
        try {
            val request =
                PutDataMapRequest
                    .create("/message")
                    .apply {
                        dataMap.putLong("time", Instant.now().epochSecond)
                        dataMap.putString("class", className)
                        dataMap.putString("message", data)
                    }.asPutDataRequest()
                    .setUrgent()
            val result =
                this
                    .putDataItem(request)
                    .await()

            Log.d(TAG, "DataItem saved: $result")
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }
}

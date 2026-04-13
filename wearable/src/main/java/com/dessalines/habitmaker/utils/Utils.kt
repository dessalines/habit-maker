package com.dessalines.habitmaker.utils

import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.gms.common.api.GoogleApi
import kotlinx.coroutines.tasks.await

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

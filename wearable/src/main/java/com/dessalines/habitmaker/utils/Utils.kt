package com.dessalines.habitmaker.utils

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.dessalines.habitmaker.complication.MainComplicationService
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.gms.common.api.GoogleApi
import kotlinx.coroutines.tasks.await

const val TAG = "com.habitmaker"

/**
 * Update the complication data
 */
fun updateComplication(ctx: Context) {
    val component =
        ComponentName(
            ctx,
            MainComplicationService::class.java,
        )

    val request =
        ComplicationDataSourceUpdateRequester.create(
            ctx,
            component,
        )
    request.requestUpdateAll()
}

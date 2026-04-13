package com.dessalines.habitmaker.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import com.dessalines.habitmaker.datalayer.DataLayerListenerService
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.wearable.DataApi
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.tasks.await
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

const val TAG = "com.habitmaker"

const val GITHUB_URL = "https://github.com/dessalines/habit-maker"
const val USER_GUIDE_URL = GITHUB_URL
const val USER_GUIDE_URL_ENCOURAGEMENTS = "$GITHUB_URL/#encouragements"
const val MATRIX_CHAT_URL = "https://matrix.to/#/#habit-maker:matrix.org"
const val DONATE_URL = "https://liberapay.com/dessalines"
const val LEMMY_URL = "https://lemmy.ml/c/habitmaker"
const val MASTODON_URL = "https://mastodon.social/@dessalines"

val SUCCESS_EMOJIS = listOf("🎉", "🥳", "🎈", "🎊", "🪇", "🎂", "🙌", "💯", "⭐")

fun openLink(
    url: String,
    ctx: Context,
) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    ctx.startActivity(intent)
}

fun Context.getPackageInfo(): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        packageManager.getPackageInfo(packageName, 0)
    }

fun Context.getVersionCode(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        getPackageInfo().longVersionCode.toInt()
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo().versionCode
    }

sealed interface SelectionVisibilityState<out Item> {
    object NoSelection : SelectionVisibilityState<Nothing>

    data class ShowSelection<Item>(
        val selectedItem: Item,
    ) : SelectionVisibilityState<Item>
}

fun Int.toBool() = this == 1

fun Boolean.toInt() = this.compareTo(false)

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

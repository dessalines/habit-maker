package com.dessalines.habitmaker.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

const val TAG = "com.habitmaker"

const val GITHUB_URL = "https://github.com/dessalines/habit-maker"
const val MATRIX_CHAT_URL = "https://matrix.to/#/#habit-maker:matrix.org"
const val DONATE_URL = "https://liberapay.com/dessalines"
const val LEMMY_URL = "https://lemmy.ml/c/habitmaker"
const val MASTODON_URL = "https://mastodon.social/@dessalines"

fun openLink(
    url: String,
    ctx: Context,
) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
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

fun writeData(
    ctx: Context,
    uri: Uri,
    data: String,
) {
    ctx.contentResolver.openOutputStream(uri)?.use {
        val bytes = data.toByteArray()
        it.write(bytes)
    }
}

sealed interface SelectionVisibilityState<out Item> {
    object NoSelection : SelectionVisibilityState<Nothing>

    data class ShowSelection<Item>(
        val selectedItem: Item,
    ) : SelectionVisibilityState<Item>
}

fun Int.toBool() = this == 1

fun Boolean.toInt() = this.compareTo(false)

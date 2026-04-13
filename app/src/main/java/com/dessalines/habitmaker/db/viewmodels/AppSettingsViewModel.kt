package com.dessalines.habitmaker.db.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dessalines.habitmaker.db.AppSettingsRepository
import com.dessalines.habitmaker.db.SettingsUpdateBehavior
import com.dessalines.habitmaker.db.SettingsUpdateHideCompleted
import com.dessalines.habitmaker.db.SettingsUpdateTheme
import com.dessalines.habitmaker.utils.TAG
import kotlinx.coroutines.launch

class AppSettingsViewModel(
    private val repository: AppSettingsRepository,
) : ViewModel() {
    val appSettings = repository.appSettings
    val appSettingsSync = repository.appSettingsSync
    val changelog = repository.changelog

    fun updateHideCompleted(settings: SettingsUpdateHideCompleted) =
        viewModelScope.launch {
            repository.updateHideCompleted(settings)
        }

    fun updateTheme(settings: SettingsUpdateTheme) =
        viewModelScope.launch {
            repository.updateTheme(settings)
        }

    fun updateBehavior(settings: SettingsUpdateBehavior) =
        viewModelScope.launch {
            repository.updateBehavior(settings)
        }

    fun updateLastVersionCodeViewed(versionCode: Int) =
        viewModelScope.launch {
            repository.updateLastVersionCodeViewed(versionCode)
        }

    fun updateChangelog(ctx: Context) =
        viewModelScope.launch {
            try {
                val releasesStr =
                    ctx.assets
                        .open("RELEASES.md")
                        .bufferedReader()
                        .use { it.readText() }
                repository.updateChangelog(releasesStr)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load changelog: $e")
            }
        }
}

class AppSettingsViewModelFactory(
    private val repository: AppSettingsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppSettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

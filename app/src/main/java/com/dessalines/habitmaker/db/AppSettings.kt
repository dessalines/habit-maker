package com.dessalines.habitmaker.db

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.dessalines.habitmaker.utils.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val DEFAULT_COMPLETED_COUNT = 66
const val MIN_COMPLETED_COUNT = 7
const val MAX_COMPLETED_COUNT = 100

@Entity
data class AppSettings(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "theme",
        defaultValue = "0"
    )
    val theme: Int,
    @ColumnInfo(
        name = "theme_color",
        defaultValue = "0"
    )
    val themeColor: Int,
    @ColumnInfo(
        name = "last_version_code_viewed",
        defaultValue = "0",
    )
    val lastVersionCodeViewed: Int,
    @ColumnInfo(
        name = "sort",
        defaultValue = "0"
    )
    val sort: Int,
    @ColumnInfo(
        name = "sort_order",
        defaultValue = "0"
    )
    val sortOrder: Int,
    @ColumnInfo(
        name = "completed_count",
        defaultValue = DEFAULT_COMPLETED_COUNT.toString(),
    )
    val completedCount: Int,
    @ColumnInfo(
        name = "hide_completed",
        defaultValue = "0"
    )
    val hideCompleted: Int,
    @ColumnInfo(
        name = "hide_archived",
        defaultValue = "0"
    )
    val hideArchived: Int,
    @ColumnInfo(
        name = "hide_points_on_home",
        defaultValue = "0"
    )
    val hidePointsOnHome: Int,
    @ColumnInfo(
        name = "hide_score_on_home",
        defaultValue = "0"
    )
    val hideScoreOnHome: Int,
    @ColumnInfo(
        name = "hide_streak_on_home",
        defaultValue = "0"
    )
    val hideStreakOnHome: Int,
)

data class SettingsUpdate(
    val id: Int,
    @ColumnInfo(
        name = "theme",
        defaultValue = "0"
    )
    val theme: Int,
    @ColumnInfo(
        name = "theme_color",
        defaultValue = "0"
    )
    val themeColor: Int,
    @ColumnInfo(
        name = "sort",
        defaultValue = "0"
    )
    val sort: Int,
    @ColumnInfo(
        name = "sort_order",
        defaultValue = "0"
    )
    val sortOrder: Int,
    @ColumnInfo(
        name = "completed_count",
        defaultValue = DEFAULT_COMPLETED_COUNT.toString(),
    )
    val completedCount: Int,
    @ColumnInfo(
        name = "hide_completed",
        defaultValue = "0"
    )
    val hideCompleted: Int,
    @ColumnInfo(
        name = "hide_archived",
        defaultValue = "0"
    )
    val hideArchived: Int,
    @ColumnInfo(
        name = "hide_points_on_home",
        defaultValue = "0"
    )
    val hidePointsOnHome: Int,
    @ColumnInfo(
        name = "hide_score_on_home",
        defaultValue = "0"
    )
    val hideScoreOnHome: Int,
    @ColumnInfo(
        name = "hide_streak_on_home",
        defaultValue = "0"
    )
    val hideStreakOnHome: Int,
)

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM AppSettings limit 1")
    fun getSettings(): Flow<AppSettings>

    @Update(entity = AppSettings::class)
    suspend fun updateSettings(settings: SettingsUpdate)

    @Query("UPDATE AppSettings SET last_version_code_viewed = :versionCode")
    suspend fun updateLastVersionCode(versionCode: Int)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class AppSettingsRepository(
    private val appSettingsDao: AppSettingsDao,
) {
    private val _changelog = MutableStateFlow("")
    val changelog = _changelog.asStateFlow()

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val appSettings = appSettingsDao.getSettings()

    @WorkerThread
    suspend fun updateSettings(settings: SettingsUpdate) {
        appSettingsDao.updateSettings(settings)
    }

    @WorkerThread
    suspend fun updateLastVersionCodeViewed(versionCode: Int) {
        appSettingsDao.updateLastVersionCode(versionCode)
    }

    @WorkerThread
    suspend fun updateChangelog(ctx: Context) {
        withContext(Dispatchers.IO) {
            try {
                val releasesStr =
                    ctx.assets
                        .open("RELEASES.md")
                        .bufferedReader()
                        .use { it.readText() }
                _changelog.value = releasesStr
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load changelog: $e")
            }
        }
    }
}

class AppSettingsViewModel(
    private val repository: AppSettingsRepository,
) : ViewModel() {
    val appSettings = repository.appSettings
    val changelog = repository.changelog

    fun updateSettings(settings: SettingsUpdate) =
        viewModelScope.launch {
            repository.updateSettings(settings)
        }

    fun updateLastVersionCodeViewed(versionCode: Int) =
        viewModelScope.launch {
            repository.updateLastVersionCodeViewed(versionCode)
        }

    fun updateChangelog(ctx: Context) =
        viewModelScope.launch {
            repository.updateChangelog(ctx)
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

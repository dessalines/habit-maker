package com.dessalines.habitmaker.db

import androidx.annotation.WorkerThread
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.DayOfWeek

const val DEFAULT_COMPLETED_COUNT = 66
const val MIN_COMPLETED_COUNT = 7
const val MAX_COMPLETED_COUNT = 100

@Entity
data class AppSettings(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "theme",
        defaultValue = "0",
    )
    val theme: Int,
    @ColumnInfo(
        name = "theme_color",
        defaultValue = "0",
    )
    val themeColor: Int,
    @ColumnInfo(
        name = "last_version_code_viewed",
        defaultValue = "0",
    )
    val lastVersionCodeViewed: Int,
    @ColumnInfo(
        name = "sort",
        defaultValue = "0",
    )
    val sort: Int,
    @ColumnInfo(
        name = "sort_order",
        defaultValue = "0",
    )
    val sortOrder: Int,
    @ColumnInfo(
        name = "completed_count",
        defaultValue = DEFAULT_COMPLETED_COUNT.toString(),
    )
    val completedCount: Int,
    @ColumnInfo(
        name = "hide_completed",
        defaultValue = "0",
    )
    val hideCompleted: Int,
    @ColumnInfo(
        name = "hide_archived",
        defaultValue = "0",
    )
    val hideArchived: Int,
    @ColumnInfo(
        name = "hide_points_on_home",
        defaultValue = "0",
    )
    val hidePointsOnHome: Int,
    @ColumnInfo(
        name = "hide_score_on_home",
        defaultValue = "0",
    )
    val hideScoreOnHome: Int,
    @ColumnInfo(
        name = "hide_streak_on_home",
        defaultValue = "0",
    )
    val hideStreakOnHome: Int,
    @ColumnInfo(
        name = "hide_chip_descriptions",
        defaultValue = "0",
    )
    val hideChipDescriptions: Int,
    @ColumnInfo(
        name = "hide_days_completed_on_home",
        defaultValue = "0",
    )
    val hideDaysCompletedOnHome: Int,
    @ColumnInfo(
        name = "first_day_of_week",
        // 6 is Sunday
        defaultValue = "6",
    )
    val firstDayOfWeek: DayOfWeek,
    @ColumnInfo(
        name = "hide_totals",
        defaultValue = "0",
    )
    val hideTotals: Int,
)

data class SettingsUpdateHideCompleted(
    val id: Int,
    @ColumnInfo(
        name = "hide_completed",
        defaultValue = "0",
    )
    val hideCompleted: Int,
)

data class SettingsUpdateTheme(
    val id: Int,
    @ColumnInfo(
        name = "theme",
        defaultValue = "0",
    )
    val theme: Int,
    @ColumnInfo(
        name = "theme_color",
        defaultValue = "0",
    )
    val themeColor: Int,
)

data class SettingsUpdateBehavior(
    val id: Int,
    @ColumnInfo(
        name = "sort",
        defaultValue = "0",
    )
    val sort: Int,
    @ColumnInfo(
        name = "sort_order",
        defaultValue = "0",
    )
    val sortOrder: Int,
    @ColumnInfo(
        name = "completed_count",
        defaultValue = DEFAULT_COMPLETED_COUNT.toString(),
    )
    val completedCount: Int,
    @ColumnInfo(
        name = "hide_completed",
        defaultValue = "0",
    )
    val hideCompleted: Int,
    @ColumnInfo(
        name = "hide_archived",
        defaultValue = "0",
    )
    val hideArchived: Int,
    @ColumnInfo(
        name = "hide_points_on_home",
        defaultValue = "0",
    )
    val hidePointsOnHome: Int,
    @ColumnInfo(
        name = "hide_score_on_home",
        defaultValue = "0",
    )
    val hideScoreOnHome: Int,
    @ColumnInfo(
        name = "hide_streak_on_home",
        defaultValue = "0",
    )
    val hideStreakOnHome: Int,
    @ColumnInfo(
        name = "hide_chip_descriptions",
        defaultValue = "0",
    )
    val hideChipDescriptions: Int,
    @ColumnInfo(
        name = "hide_days_completed_on_home",
        defaultValue = "0",
    )
    val hideDaysCompletedOnHome: Int,
    @ColumnInfo(
        name = "first_day_of_week",
        defaultValue = "6",
    )
    val firstDayOfWeek: DayOfWeek,
    @ColumnInfo(
        name = "hide_totals",
        defaultValue = "0",
    )
    val hideTotals: Int,
)

data class SettingsUpdateWearable(
    val id: Int,
    @ColumnInfo(
        name = "sort",
        defaultValue = "0",
    )
    val sort: Int,
    @ColumnInfo(
        name = "sort_order",
        defaultValue = "0",
    )
    val sortOrder: Int,
    @ColumnInfo(
        name = "hide_completed",
        defaultValue = "0",
    )
    val hideCompleted: Int,
    @ColumnInfo(
        name = "hide_archived",
        defaultValue = "0",
    )
    val hideArchived: Int,
    @ColumnInfo(
        name = "hide_points_on_home",
        defaultValue = "0",
    )
    val hidePointsOnHome: Int,
    @ColumnInfo(
        name = "hide_score_on_home",
        defaultValue = "0",
    )
    val hideScoreOnHome: Int,
    @ColumnInfo(
        name = "hide_streak_on_home",
        defaultValue = "0",
    )
    val hideStreakOnHome: Int,
)

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM AppSettings limit 1")
    fun getSettings(): Flow<AppSettings>

    @Query("SELECT * FROM AppSettings limit 1")
    fun getSettingsSync(): AppSettings?

    @Update(entity = AppSettings::class)
    suspend fun updateHideCompleted(settings: SettingsUpdateHideCompleted)

    @Update(entity = AppSettings::class)
    suspend fun updateTheme(settings: SettingsUpdateTheme)

    @Update(entity = AppSettings::class)
    suspend fun updateBehavior(settings: SettingsUpdateBehavior)

    @Update(entity = AppSettings::class)
    suspend fun updateSettingsWearable(settings: SettingsUpdateWearable)

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

    val appSettingsSync = appSettingsDao.getSettingsSync()

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val appSettings = appSettingsDao.getSettings()

    @WorkerThread
    suspend fun updateHideCompleted(settings: SettingsUpdateHideCompleted) {
        appSettingsDao.updateHideCompleted(settings)
    }

    @WorkerThread
    suspend fun updateTheme(settings: SettingsUpdateTheme) {
        appSettingsDao.updateTheme(settings)
    }

    @WorkerThread
    suspend fun updateBehavior(settings: SettingsUpdateBehavior) {
        appSettingsDao.updateBehavior(settings)
    }

    @WorkerThread
    suspend fun updateSettingsWearable(settings: SettingsUpdateWearable) {
        appSettingsDao.updateSettingsWearable(settings)
    }

    @WorkerThread
    suspend fun updateLastVersionCodeViewed(versionCode: Int) {
        appSettingsDao.updateLastVersionCode(versionCode)
    }

    @WorkerThread
    suspend fun updateChangelog(releasesStr: String) {
        withContext(Dispatchers.IO) {
            _changelog.value = releasesStr
        }
    }
}

val sampleAppSettings =
    AppSettings(
        id = 0,
        theme = 0,
        themeColor = 0,
        lastVersionCodeViewed = 0,
        sort = 0,
        sortOrder = 0,
        completedCount = 0,
        hideCompleted = 0,
        hideArchived = 0,
        hideTotals = 0,
        hidePointsOnHome = 0,
        hideScoreOnHome = 0,
        hideStreakOnHome = 0,
        hideChipDescriptions = 0,
        hideDaysCompletedOnHome = 0,
        firstDayOfWeek = DayOfWeek.SUNDAY,
    )

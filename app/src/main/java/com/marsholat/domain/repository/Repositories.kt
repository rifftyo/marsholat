package com.marsholat.domain.repository

import com.marsholat.core.model.AsrJuristicMethod
import com.marsholat.core.model.CalculationMethod
import com.marsholat.core.model.PrayerStatus
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ThemeMode
import com.marsholat.domain.model.DailyTrackerSummary
import com.marsholat.domain.model.HijriDate
import com.marsholat.domain.model.LocationData
import com.marsholat.domain.model.MonthlyTrackerStats
import com.marsholat.domain.model.NextPrayerCountdown
import com.marsholat.domain.model.PrayerAdjustment
import com.marsholat.domain.model.PrayerNotificationSetting
import com.marsholat.domain.model.PrayerSchedule
import com.marsholat.domain.model.TrackerEntry
import com.marsholat.domain.model.UserSettings
import com.marsholat.domain.model.WeeklyTrackerStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface SettingsRepository {
    val userSettingsFlow: Flow<UserSettings>
    val themeModeFlow: Flow<ThemeMode>
    val allNotificationSettingsFlow: Flow<List<PrayerNotificationSetting>>
    fun getNotificationSettingFlow(prayer: PrayerType): Flow<PrayerNotificationSetting>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCalculationMethod(method: CalculationMethod)
    suspend fun setAsrJuristicMethod(method: AsrJuristicMethod)
    suspend fun setIs24HourFormat(is24Hour: Boolean)
    suspend fun saveLocation(location: LocationData)
    suspend fun saveAdjustments(adjustments: PrayerAdjustment)
    suspend fun saveNotificationSetting(setting: PrayerNotificationSetting)
}

interface PrayerScheduleRepository {
    suspend fun getScheduleForDate(date: LocalDate, settings: UserSettings): PrayerSchedule
    suspend fun getMonthlySchedule(yearMonth: YearMonth, settings: UserSettings): Map<LocalDate, PrayerSchedule>
    fun getNextPrayerCountdownFlow(settings: UserSettings): Flow<NextPrayerCountdown>
    fun getHijriDate(date: LocalDate): HijriDate
}

interface PrayerTrackerRepository {
    fun getDailySummaryFlow(date: LocalDate): Flow<DailyTrackerSummary>
    suspend fun updatePrayerStatus(date: LocalDate, prayer: PrayerType, status: PrayerStatus)
    fun getWeeklyStatsFlow(endDate: LocalDate = LocalDate.now()): Flow<WeeklyTrackerStats>
    fun getMonthlyStatsFlow(yearMonth: YearMonth = YearMonth.now()): Flow<MonthlyTrackerStats>
}

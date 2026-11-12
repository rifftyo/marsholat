package com.marsholat.presentation.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsholat.core.model.PrayerStatus
import com.marsholat.core.model.PrayerType
import com.marsholat.domain.model.DailyTrackerSummary
import com.marsholat.domain.model.MonthlyTrackerStats
import com.marsholat.domain.model.PrayerSchedule
import com.marsholat.domain.model.UserSettings
import com.marsholat.domain.model.WeeklyTrackerStats
import com.marsholat.domain.repository.PrayerScheduleRepository
import com.marsholat.domain.repository.PrayerTrackerRepository
import com.marsholat.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class TrackerUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val schedule: PrayerSchedule? = null,
    val userSettings: UserSettings = UserSettings()
)

class TrackerViewModel(
    private val trackerRepository: PrayerTrackerRepository,
    private val scheduleRepository: PrayerScheduleRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _schedule = MutableStateFlow<PrayerSchedule?>(null)
    val schedule = _schedule.asStateFlow()

    val userSettings: StateFlow<UserSettings> = settingsRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailySummary: StateFlow<DailyTrackerSummary?> = _selectedDate
        .flatMapLatest { date ->
            trackerRepository.getDailySummaryFlow(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val weeklyStats: StateFlow<WeeklyTrackerStats?> = trackerRepository
        .getWeeklyStatsFlow(LocalDate.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val monthlyStats: StateFlow<MonthlyTrackerStats?> = trackerRepository
        .getMonthlyStatsFlow(YearMonth.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            _selectedDate.collect { date ->
                updateScheduleForDate(date, userSettings.value)
            }
        }

        viewModelScope.launch {
            userSettings.collect { settings ->
                updateScheduleForDate(_selectedDate.value, settings)
            }
        }
    }

    private suspend fun updateScheduleForDate(date: LocalDate, settings: UserSettings) {
        _schedule.value = scheduleRepository.getScheduleForDate(date, settings)
    }

    fun selectDate(date: LocalDate) {
        if (!date.isAfter(LocalDate.now())) {
            _selectedDate.value = date
        }
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        val next = _selectedDate.value.plusDays(1)
        if (!next.isAfter(LocalDate.now())) {
            _selectedDate.value = next
        }
    }

    fun updateStatus(prayer: PrayerType, status: PrayerStatus) {
        viewModelScope.launch {
            trackerRepository.updatePrayerStatus(_selectedDate.value, prayer, status)
        }
    }
}

package com.marsholat.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsholat.core.model.PrayerType
import com.marsholat.domain.model.HijriDate
import com.marsholat.domain.model.PrayerSchedule
import com.marsholat.domain.model.UserSettings
import com.marsholat.domain.repository.PrayerScheduleRepository
import com.marsholat.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class ScheduleUiState(
    val selectedTab: Int = 0, // 0 = Hari Ini, 1 = Kalender Bulanan
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val todaySchedule: PrayerSchedule? = null,
    val selectedDateSchedule: PrayerSchedule? = null,
    val selectedDateHijri: HijriDate? = null,
    val monthlySchedule: Map<LocalDate, PrayerSchedule> = emptyMap(),
    val userSettings: UserSettings = UserSettings(),
    val isLoading: Boolean = false
)

class ScheduleViewModel(
    private val settingsRepository: SettingsRepository,
    private val prayerScheduleRepository: PrayerScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.userSettingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(userSettings = settings)
                loadSchedules(settings, _uiState.value.currentMonth, _uiState.value.selectedDate)
            }
        }
    }

    private suspend fun loadSchedules(settings: UserSettings, yearMonth: YearMonth, selectedDate: LocalDate) {
        val today = LocalDate.now()
        val todaySched = prayerScheduleRepository.getScheduleForDate(today, settings)
        val selectedSched = prayerScheduleRepository.getScheduleForDate(selectedDate, settings)
        val selectedHijri = prayerScheduleRepository.getHijriDate(selectedDate)
        val monthSched = prayerScheduleRepository.getMonthlySchedule(yearMonth, settings)

        _uiState.value = _uiState.value.copy(
            todaySchedule = todaySched,
            selectedDateSchedule = selectedSched,
            selectedDateHijri = selectedHijri,
            monthlySchedule = monthSched
        )
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            val sched = prayerScheduleRepository.getScheduleForDate(date, _uiState.value.userSettings)
            val hijri = prayerScheduleRepository.getHijriDate(date)
            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                selectedDateSchedule = sched,
                selectedDateHijri = hijri
            )
        }
    }

    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        val newDate = newMonth.atDay(1)
        _uiState.value = _uiState.value.copy(currentMonth = newMonth, selectedDate = newDate)
        viewModelScope.launch {
            loadSchedules(_uiState.value.userSettings, newMonth, newDate)
        }
    }

    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        val newDate = newMonth.atDay(1)
        _uiState.value = _uiState.value.copy(currentMonth = newMonth, selectedDate = newDate)
        viewModelScope.launch {
            loadSchedules(_uiState.value.userSettings, newMonth, newDate)
        }
    }
}

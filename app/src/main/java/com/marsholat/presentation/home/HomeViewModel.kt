package com.marsholat.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsholat.core.util.CityDatabase
import com.marsholat.core.util.LocationHelper
import com.marsholat.domain.model.DailyTrackerSummary
import com.marsholat.domain.model.HijriDate
import com.marsholat.domain.model.NextPrayerCountdown
import com.marsholat.domain.model.PrayerSchedule
import com.marsholat.domain.model.UserSettings
import com.marsholat.domain.repository.PrayerScheduleRepository
import com.marsholat.domain.repository.PrayerTrackerRepository
import com.marsholat.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val userSettings: UserSettings = UserSettings(),
    val todaySchedule: PrayerSchedule? = null,
    val hijriDate: HijriDate? = null,
    val countdown: NextPrayerCountdown? = null,
    val trackerSummary: DailyTrackerSummary? = null,
    val isRefreshingLocation: Boolean = false,
    val locationMessage: String? = null
)

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val prayerScheduleRepository: PrayerScheduleRepository,
    private val trackerRepository: PrayerTrackerRepository,
    context: Context
) : ViewModel() {

    private val locationHelper = LocationHelper(context)

    private val _isRefreshingLocation = MutableStateFlow(false)
    val isRefreshingLocation = _isRefreshingLocation.asStateFlow()

    private val _locationMessage = MutableStateFlow<String?>(null)
    val locationMessage = _locationMessage.asStateFlow()

    val userSettings: StateFlow<UserSettings> = settingsRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _todaySchedule = MutableStateFlow<PrayerSchedule?>(null)
    val todaySchedule = _todaySchedule.asStateFlow()

    private val _hijriDate = MutableStateFlow<HijriDate?>(null)
    val hijriDate = _hijriDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val countdown: StateFlow<NextPrayerCountdown?> = userSettings
        .flatMapLatest { settings ->
            prayerScheduleRepository.getNextPrayerCountdownFlow(settings)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val trackerSummary: StateFlow<DailyTrackerSummary?> = trackerRepository
        .getDailySummaryFlow(LocalDate.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            userSettings.collect { settings ->
                val today = LocalDate.now()
                _todaySchedule.value = prayerScheduleRepository.getScheduleForDate(today, settings)
                _hijriDate.value = prayerScheduleRepository.getHijriDate(today)
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _isRefreshingLocation.value = true
            _locationMessage.value = null

            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                settingsRepository.saveLocation(loc)
                _locationMessage.value = "Lokasi diperbarui: ${loc.cityName}"
            } else {
                _locationMessage.value = "Menggunakan lokasi offline: ${userSettings.value.location.cityName}"
            }
            _isRefreshingLocation.value = false
        }
    }

    fun dismissLocationMessage() {
        _locationMessage.value = null
    }
}

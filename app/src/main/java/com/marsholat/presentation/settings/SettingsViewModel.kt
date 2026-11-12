package com.marsholat.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsholat.core.model.AsrJuristicMethod
import com.marsholat.core.model.CalculationMethod
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ThemeMode
import com.marsholat.core.notification.AlarmScheduler
import com.marsholat.core.util.LocationHelper
import com.marsholat.domain.model.LocationData
import com.marsholat.domain.model.PrayerAdjustment
import com.marsholat.domain.model.UserSettings
import com.marsholat.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {

    private val locationHelper = LocationHelper(context)

    val userSettings: StateFlow<UserSettings> = settingsRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _isRefreshingLocation = MutableStateFlow(false)
    val isRefreshingLocation: StateFlow<Boolean> = _isRefreshingLocation.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch {
            settingsRepository.setCalculationMethod(method)
            AlarmScheduler.scheduleAllAlarms(context)
        }
    }

    fun setAsrJuristicMethod(method: AsrJuristicMethod) {
        viewModelScope.launch {
            settingsRepository.setAsrJuristicMethod(method)
            AlarmScheduler.scheduleAllAlarms(context)
        }
    }

    fun setIs24HourFormat(is24Hour: Boolean) {
        viewModelScope.launch {
            settingsRepository.setIs24HourFormat(is24Hour)
        }
    }

    fun setLocation(location: LocationData) {
        viewModelScope.launch {
            settingsRepository.saveLocation(location)
            AlarmScheduler.scheduleAllAlarms(context)
            _toastMessage.value = "Lokasi diatur: ${location.cityName}"
        }
    }

    fun refreshLocationGps() {
        viewModelScope.launch {
            _isRefreshingLocation.value = true
            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                settingsRepository.saveLocation(loc)
                AlarmScheduler.scheduleAllAlarms(context)
                _toastMessage.value = "GPS berhasil: ${loc.cityName}"
            } else {
                _toastMessage.value = "Tidak dapat mengambil sinyal GPS, silakan pilih kota secara offline."
            }
            _isRefreshingLocation.value = false
        }
    }

    fun updateAdjustment(prayer: PrayerType, deltaMinutes: Int) {
        viewModelScope.launch {
            val cur = userSettings.value.adjustments
            val updated = when (prayer) {
                PrayerType.SUBUH -> cur.copy(subuhMinutes = (cur.subuhMinutes + deltaMinutes).coerceIn(-30, 30))
                PrayerType.DZUHUR -> cur.copy(dzuhurMinutes = (cur.dzuhurMinutes + deltaMinutes).coerceIn(-30, 30))
                PrayerType.ASHAR -> cur.copy(asharMinutes = (cur.asharMinutes + deltaMinutes).coerceIn(-30, 30))
                PrayerType.MAGHRIB -> cur.copy(maghribMinutes = (cur.maghribMinutes + deltaMinutes).coerceIn(-30, 30))
                PrayerType.ISYA -> cur.copy(isyaMinutes = (cur.isyaMinutes + deltaMinutes).coerceIn(-30, 30))
            }
            settingsRepository.saveAdjustments(updated)
            AlarmScheduler.scheduleAllAlarms(context)
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

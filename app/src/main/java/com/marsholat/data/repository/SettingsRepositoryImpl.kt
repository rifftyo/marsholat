package com.marsholat.data.repository

import com.marsholat.core.model.AsrJuristicMethod
import com.marsholat.core.model.CalculationMethod
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ThemeMode
import com.marsholat.data.preferences.AppPreferencesManager
import com.marsholat.domain.model.LocationData
import com.marsholat.domain.model.PrayerAdjustment
import com.marsholat.domain.model.PrayerNotificationSetting
import com.marsholat.domain.model.UserSettings
import com.marsholat.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val preferencesManager: AppPreferencesManager
) : SettingsRepository {

    override val userSettingsFlow: Flow<UserSettings> = preferencesManager.userSettingsFlow

    override val themeModeFlow: Flow<ThemeMode> = preferencesManager.themeModeFlow

    override val allNotificationSettingsFlow: Flow<List<PrayerNotificationSetting>> =
        preferencesManager.allNotificationSettingsFlow

    override fun getNotificationSettingFlow(prayer: PrayerType): Flow<PrayerNotificationSetting> {
        return preferencesManager.getNotificationSettingFlow(prayer)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        preferencesManager.setThemeMode(mode)
    }

    override suspend fun setCalculationMethod(method: CalculationMethod) {
        preferencesManager.setCalculationMethod(method)
    }

    override suspend fun setAsrJuristicMethod(method: AsrJuristicMethod) {
        preferencesManager.setAsrJuristicMethod(method)
    }

    override suspend fun setIs24HourFormat(is24Hour: Boolean) {
        preferencesManager.setIs24HourFormat(is24Hour)
    }

    override suspend fun saveLocation(location: LocationData) {
        preferencesManager.saveLocation(location)
    }

    override suspend fun saveAdjustments(adjustments: PrayerAdjustment) {
        preferencesManager.saveAdjustments(adjustments)
    }

    override suspend fun saveNotificationSetting(setting: PrayerNotificationSetting) {
        preferencesManager.saveNotificationSetting(setting)
    }
}

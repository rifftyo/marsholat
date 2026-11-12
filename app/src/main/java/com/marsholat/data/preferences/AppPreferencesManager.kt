package com.marsholat.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.marsholat.core.model.AsrJuristicMethod
import com.marsholat.core.model.CalculationMethod
import com.marsholat.core.model.PrePrayerReminderInterval
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ReminderType
import com.marsholat.core.model.ThemeMode
import com.marsholat.domain.model.LocationData
import com.marsholat.domain.model.PrayerAdjustment
import com.marsholat.domain.model.PrayerNotificationSetting
import com.marsholat.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "marsholat_prefs")

class AppPreferencesManager(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        val ASR_JURISTIC = stringPreferencesKey("asr_juristic")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")

        val LATITUDE = doublePreferencesKey("loc_latitude")
        val LONGITUDE = doublePreferencesKey("loc_longitude")
        val CITY_NAME = stringPreferencesKey("loc_city_name")
        val REGION = stringPreferencesKey("loc_region")
        val TIMEZONE_ID = stringPreferencesKey("loc_timezone_id")

        val ADJ_SUBUH = intPreferencesKey("adj_subuh")
        val ADJ_DZUHUR = intPreferencesKey("adj_dzuhur")
        val ADJ_ASHAR = intPreferencesKey("adj_ashar")
        val ADJ_MAGHRIB = intPreferencesKey("adj_maghrib")
        val ADJ_ISYA = intPreferencesKey("adj_isya")

        fun notifEnabled(prayer: PrayerType) = booleanPreferencesKey("notif_${prayer.name}_enabled")
        fun notifType(prayer: PrayerType) = stringPreferencesKey("notif_${prayer.name}_type")
        fun notifPreInterval(prayer: PrayerType) = intPreferencesKey("notif_${prayer.name}_pre_interval")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = runCatching { ThemeMode.valueOf(themeModeStr) }.getOrDefault(ThemeMode.SYSTEM)

        val calcMethodStr = prefs[Keys.CALCULATION_METHOD] ?: CalculationMethod.KEMENAG.name
        val calcMethod = runCatching { CalculationMethod.valueOf(calcMethodStr) }.getOrDefault(CalculationMethod.KEMENAG)

        val asrStr = prefs[Keys.ASR_JURISTIC] ?: AsrJuristicMethod.STANDARD.name
        val asrMethod = runCatching { AsrJuristicMethod.valueOf(asrStr) }.getOrDefault(AsrJuristicMethod.STANDARD)

        val is24Hour = prefs[Keys.IS_24_HOUR] ?: true

        val location = LocationData(
            latitude = prefs[Keys.LATITUDE] ?: -6.2088,
            longitude = prefs[Keys.LONGITUDE] ?: 106.8456,
            cityName = prefs[Keys.CITY_NAME] ?: "Jakarta",
            region = prefs[Keys.REGION] ?: "DKI Jakarta, Indonesia",
            timeZoneId = prefs[Keys.TIMEZONE_ID] ?: "Asia/Jakarta"
        )

        val adjustments = PrayerAdjustment(
            subuhMinutes = prefs[Keys.ADJ_SUBUH] ?: 0,
            dzuhurMinutes = prefs[Keys.ADJ_DZUHUR] ?: 0,
            asharMinutes = prefs[Keys.ADJ_ASHAR] ?: 0,
            maghribMinutes = prefs[Keys.ADJ_MAGHRIB] ?: 0,
            isyaMinutes = prefs[Keys.ADJ_ISYA] ?: 0
        )

        UserSettings(
            themeMode = themeMode,
            calculationMethod = calcMethod,
            asrJuristicMethod = asrMethod,
            adjustments = adjustments,
            location = location,
            is24HourFormat = is24Hour
        )
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val name = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(name) }.getOrDefault(ThemeMode.SYSTEM)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setCalculationMethod(method: CalculationMethod) {
        context.dataStore.edit { it[Keys.CALCULATION_METHOD] = method.name }
    }

    suspend fun setAsrJuristicMethod(method: AsrJuristicMethod) {
        context.dataStore.edit { it[Keys.ASR_JURISTIC] = method.name }
    }

    suspend fun setIs24HourFormat(is24Hour: Boolean) {
        context.dataStore.edit { it[Keys.IS_24_HOUR] = is24Hour }
    }

    suspend fun saveLocation(location: LocationData) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LATITUDE] = location.latitude
            prefs[Keys.LONGITUDE] = location.longitude
            prefs[Keys.CITY_NAME] = location.cityName
            prefs[Keys.REGION] = location.region
            prefs[Keys.TIMEZONE_ID] = location.timeZoneId
        }
    }

    suspend fun saveAdjustments(adjustments: PrayerAdjustment) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ADJ_SUBUH] = adjustments.subuhMinutes
            prefs[Keys.ADJ_DZUHUR] = adjustments.dzuhurMinutes
            prefs[Keys.ADJ_ASHAR] = adjustments.asharMinutes
            prefs[Keys.ADJ_MAGHRIB] = adjustments.maghribMinutes
            prefs[Keys.ADJ_ISYA] = adjustments.isyaMinutes
        }
    }

    fun getNotificationSettingFlow(prayer: PrayerType): Flow<PrayerNotificationSetting> {
        return context.dataStore.data.map { prefs ->
            val isEnabled = prefs[Keys.notifEnabled(prayer)] ?: true
            val typeStr = prefs[Keys.notifType(prayer)] ?: ReminderType.ADZAN.name
            val reminderType = runCatching { ReminderType.valueOf(typeStr) }.getOrDefault(ReminderType.ADZAN)
            val preMinutes = prefs[Keys.notifPreInterval(prayer)] ?: 0
            val preInterval = PrePrayerReminderInterval.fromMinutes(preMinutes)

            PrayerNotificationSetting(
                prayerType = prayer,
                isEnabled = isEnabled,
                reminderType = reminderType,
                preReminderInterval = preInterval
            )
        }
    }

    val allNotificationSettingsFlow: Flow<List<PrayerNotificationSetting>> = context.dataStore.data.map { prefs ->
        PrayerType.entries.map { prayer ->
            val isEnabled = prefs[Keys.notifEnabled(prayer)] ?: true
            val typeStr = prefs[Keys.notifType(prayer)] ?: ReminderType.ADZAN.name
            val reminderType = runCatching { ReminderType.valueOf(typeStr) }.getOrDefault(ReminderType.ADZAN)
            val preMinutes = prefs[Keys.notifPreInterval(prayer)] ?: 0
            val preInterval = PrePrayerReminderInterval.fromMinutes(preMinutes)

            PrayerNotificationSetting(
                prayerType = prayer,
                isEnabled = isEnabled,
                reminderType = reminderType,
                preReminderInterval = preInterval
            )
        }
    }

    suspend fun saveNotificationSetting(setting: PrayerNotificationSetting) {
        context.dataStore.edit { prefs ->
            prefs[Keys.notifEnabled(setting.prayerType)] = setting.isEnabled
            prefs[Keys.notifType(setting.prayerType)] = setting.reminderType.name
            prefs[Keys.notifPreInterval(setting.prayerType)] = setting.preReminderInterval.minutes
        }
    }
}

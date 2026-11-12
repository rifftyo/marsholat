package com.marsholat.presentation.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsholat.core.model.PrePrayerReminderInterval
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ReminderType
import com.marsholat.core.notification.AdzanPlaybackService
import com.marsholat.core.notification.AlarmScheduler
import com.marsholat.core.notification.NotificationHelper
import com.marsholat.domain.model.PrayerNotificationSetting
import com.marsholat.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {

    private val notificationHelper = NotificationHelper(context)

    val notificationSettings: StateFlow<List<PrayerNotificationSetting>> =
        settingsRepository.allNotificationSettingsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateToggle(prayer: PrayerType, isEnabled: Boolean) {
        val current = notificationSettings.value.firstOrNull { it.prayerType == prayer }
            ?: PrayerNotificationSetting(prayerType = prayer)
        val updated = current.copy(isEnabled = isEnabled)
        saveAndReschedule(updated)
    }

    fun updateReminderType(prayer: PrayerType, type: ReminderType) {
        val current = notificationSettings.value.firstOrNull { it.prayerType == prayer }
            ?: PrayerNotificationSetting(prayerType = prayer)
        val updated = current.copy(reminderType = type)
        saveAndReschedule(updated)
    }

    fun updatePreReminderInterval(prayer: PrayerType, interval: PrePrayerReminderInterval) {
        val current = notificationSettings.value.firstOrNull { it.prayerType == prayer }
            ?: PrayerNotificationSetting(prayerType = prayer)
        val updated = current.copy(preReminderInterval = interval)
        saveAndReschedule(updated)
    }

    private fun saveAndReschedule(setting: PrayerNotificationSetting) {
        viewModelScope.launch {
            settingsRepository.saveNotificationSetting(setting)
            AlarmScheduler.scheduleAllAlarms(context)
        }
    }

    fun testAudioAndNotification(prayer: PrayerType, type: ReminderType) {
        when (type) {
            ReminderType.ADZAN -> {
                val intent = Intent(context, AdzanPlaybackService::class.java).apply {
                    putExtra(AdzanPlaybackService.EXTRA_PRAYER_NAME, prayer.name)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
            ReminderType.NOTIFICATION -> {
                notificationHelper.showRegularNotification(prayer)
            }
            ReminderType.ALARM -> {
                notificationHelper.showAlarmNotification(prayer)
            }
        }
    }
}

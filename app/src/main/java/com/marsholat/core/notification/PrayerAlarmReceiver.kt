package com.marsholat.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.marsholat.MarSholatApplication
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ReminderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER) ?: return
        val prayer = runCatching { PrayerType.valueOf(prayerName) }.getOrDefault(PrayerType.SUBUH)

        val notificationHelper = NotificationHelper(context)
        val app = context.applicationContext as MarSholatApplication

        CoroutineScope(Dispatchers.IO).launch {
            val notifSetting = app.settingsRepository.getNotificationSettingFlow(prayer).first()
            if (!notifSetting.isEnabled) return@launch

            when (notifSetting.reminderType) {
                ReminderType.ADZAN -> {
                    val serviceIntent = Intent(context, AdzanPlaybackService::class.java).apply {
                        putExtra(AdzanPlaybackService.EXTRA_PRAYER_NAME, prayer.name)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
                ReminderType.NOTIFICATION -> {
                    notificationHelper.showRegularNotification(prayer)
                }
                ReminderType.ALARM -> {
                    notificationHelper.showAlarmNotification(prayer)
                }
            }

            // Reschedule for next day
            AlarmScheduler.scheduleAllAlarms(context)
        }
    }
}

package com.marsholat.core.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.marsholat.MarSholatApplication
import com.marsholat.core.model.PrePrayerReminderInterval
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ReminderType
import com.marsholat.domain.model.UserSettings
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object AlarmScheduler {

    const val EXTRA_PRAYER = "extra_prayer"
    const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"

    private const val REQUEST_CODE_PRAYER_BASE = 5000
    private const val REQUEST_CODE_REMINDER_BASE = 6000

    @SuppressLint("ScheduleExactAlarm")
    suspend fun scheduleAllAlarms(context: Context) {
        val app = context.applicationContext as MarSholatApplication
        val settings = app.settingsRepository.userSettingsFlow.first()
        val notifSettings = app.settingsRepository.allNotificationSettingsFlow.first()
        val notifMap = notifSettings.associateBy { it.prayerType }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val zoneId = runCatching { ZoneId.of(settings.location.timeZoneId) }.getOrDefault(ZoneId.systemDefault())

        val now = LocalDateTime.now(zoneId)
        val today = now.toLocalDate()
        val todaySchedule = app.prayerScheduleRepository.getScheduleForDate(today, settings)
        val tomorrowSchedule = app.prayerScheduleRepository.getScheduleForDate(today.plusDays(1), settings)

        PrayerType.entries.forEach { prayer ->
            val notifConfig = notifMap[prayer]
            if (notifConfig == null || !notifConfig.isEnabled) {
                cancelAlarm(context, prayer)
                cancelSmartReminder(context, prayer)
                return@forEach
            }

            // Target time for this prayer (today or tomorrow)
            val todayPrayerTime = todaySchedule.getTime(prayer).atDate(today)
            val targetPrayerTime = if (todayPrayerTime.isAfter(now)) {
                todayPrayerTime
            } else {
                tomorrowSchedule.getTime(prayer).atDate(today.plusDays(1))
            }

            val targetMillis = targetPrayerTime.atZone(zoneId).toInstant().toEpochMilli()

            // Schedule main prayer alarm
            val prayerIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                putExtra(EXTRA_PRAYER, prayer.name)
            }
            val prayerPendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_PRAYER_BASE + prayer.order,
                prayerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            setExactAlarm(alarmManager, targetMillis, prayerPendingIntent)

            // Schedule Smart Reminder if active
            if (notifConfig.preReminderInterval != PrePrayerReminderInterval.OFF) {
                val reminderTime = targetPrayerTime.minusMinutes(notifConfig.preReminderInterval.minutes.toLong())
                if (reminderTime.isAfter(now)) {
                    val reminderMillis = reminderTime.atZone(zoneId).toInstant().toEpochMilli()
                    val reminderIntent = Intent(context, SmartReminderReceiver::class.java).apply {
                        putExtra(EXTRA_PRAYER, prayer.name)
                        putExtra(EXTRA_MINUTES_BEFORE, notifConfig.preReminderInterval.minutes)
                    }
                    val reminderPendingIntent = PendingIntent.getBroadcast(
                        context,
                        REQUEST_CODE_REMINDER_BASE + prayer.order,
                        reminderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setExactAlarm(alarmManager, reminderMillis, reminderPendingIntent)
                }
            } else {
                cancelSmartReminder(context, prayer)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setExactAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, operation: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        }
    }

    private fun cancelAlarm(context: Context, prayer: PrayerType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PRAYER_BASE + prayer.order,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun cancelSmartReminder(context: Context, prayer: PrayerType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, SmartReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER_BASE + prayer.order,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}

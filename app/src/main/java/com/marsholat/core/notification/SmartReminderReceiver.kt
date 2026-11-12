package com.marsholat.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.marsholat.core.model.PrayerType

class SmartReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER) ?: return
        val minutesBefore = intent.getIntExtra(AlarmScheduler.EXTRA_MINUTES_BEFORE, 10)
        val prayer = runCatching { PrayerType.valueOf(prayerName) }.getOrDefault(PrayerType.SUBUH)

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showPrePrayerReminder(prayer, minutesBefore)
    }
}

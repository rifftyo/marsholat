package com.marsholat.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.marsholat.MainActivity
import com.marsholat.R
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ReminderType

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ADZAN = "channel_adzan"
        const val CHANNEL_REGULAR = "channel_regular"
        const val CHANNEL_ALARM = "channel_alarm"
        const val CHANNEL_REMINDER = "channel_reminder"

        const val NOTIF_ID_ADZAN = 1001
        const val NOTIF_ID_REGULAR_BASE = 2000
        const val NOTIF_ID_REMINDER_BASE = 3000

        const val ACTION_STOP_ADZAN = "com.marsholat.action.STOP_ADZAN"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel 1: Adzan Playback
            val adzanChannel = NotificationChannel(
                CHANNEL_ADZAN,
                "Adzan Waktu Shalat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi adzan saat waktu shalat tiba"
                setSound(null, null) // Audio is handled by AdzanPlaybackService
                enableVibration(true)
            }

            // Channel 2: Notifikasi Biasa
            val regularChannel = NotificationChannel(
                CHANNEL_REGULAR,
                "Notifikasi Shalat Biasa",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pengingat waktu shalat dengan nada notifikasi sistem"
                val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(defaultSound, audioAttributes)
                enableVibration(true)
            }

            // Channel 3: Alarm Shalat
            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM,
                "Alarm Shalat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm dengan suara alarm sistem saat waktu shalat tiba"
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(alarmSound, audioAttributes)
                enableVibration(true)
            }

            // Channel 4: Smart Reminder (Sebelum Shalat)
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER,
                "Pengingat Sebelum Shalat",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pengingat persiapan beberapa menit sebelum waktu shalat"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(adzanChannel, regularChannel, alarmChannel, reminderChannel)
            )
        }
    }

    fun buildAdzanNotification(prayer: PrayerType): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, AdzanPlaybackService::class.java).apply {
            action = ACTION_STOP_ADZAN
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ADZAN)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Waktu Shalat ${prayer.displayName} Tiba")
            .setContentText("Hayya 'alash Shalaah - Mari tunaikan shalat ${prayer.displayName}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(android.R.drawable.ic_media_pause, "Hentikan Adzan", stopPendingIntent)
            .build()
    }

    fun showRegularNotification(prayer: PrayerType) {
        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayer.order,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REGULAR)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Waktu Shalat ${prayer.displayName}")
            .setContentText("Saatnya menunaikan ibadah shalat ${prayer.displayName}.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIF_ID_REGULAR_BASE + prayer.order, notification)
    }

    fun showAlarmNotification(prayer: PrayerType) {
        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayer.order,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm Shalat ${prayer.displayName}")
            .setContentText("Waktu shalat ${prayer.displayName} telah tiba!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIF_ID_REGULAR_BASE + prayer.order, notification)
    }

    fun showPrePrayerReminder(prayer: PrayerType, minutesBefore: Int) {
        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayer.order + 10,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Pengingat Shalat ${prayer.displayName}")
            .setContentText("$minutesBefore menit lagi menuju waktu shalat ${prayer.displayName}. Siapkan wudhu Anda.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIF_ID_REMINDER_BASE + prayer.order, notification)
    }
}

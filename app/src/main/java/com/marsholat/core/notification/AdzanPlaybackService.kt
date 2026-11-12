package com.marsholat.core.notification

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.marsholat.R
import com.marsholat.core.model.PrayerType

class AdzanPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var notificationHelper: NotificationHelper

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == NotificationHelper.ACTION_STOP_ADZAN) {
            stopPlaybackAndService()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: PrayerType.SUBUH.name
        val prayer = runCatching { PrayerType.valueOf(prayerName) }.getOrDefault(PrayerType.SUBUH)

        val notification = notificationHelper.buildAdzanNotification(prayer)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NotificationHelper.NOTIF_ID_ADZAN,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NotificationHelper.NOTIF_ID_ADZAN, notification)
        }

        playAdzanAudio()

        return START_NOT_STICKY
    }

    private fun playAdzanAudio() {
        // Respect silent and vibrate modes
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager != null) {
            if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) {
                // Device in silent mode, do not blast audio
                return
            }
        }

        try {
            stopMediaPlayer()
            mediaPlayer = MediaPlayer.create(this, R.raw.adzan).apply {
                setOnCompletionListener {
                    stopPlaybackAndService()
                }
                setOnErrorListener { _, _, _ ->
                    stopPlaybackAndService()
                    true
                }
                start()
            }
        } catch (e: Exception) {
            stopPlaybackAndService()
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    private fun stopPlaybackAndService() {
        stopMediaPlayer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopMediaPlayer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

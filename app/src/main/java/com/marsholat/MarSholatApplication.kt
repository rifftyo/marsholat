package com.marsholat

import android.app.Application
import com.marsholat.data.local.MarSholatDatabase
import com.marsholat.data.preferences.AppPreferencesManager
import com.marsholat.data.repository.PrayerScheduleRepositoryImpl
import com.marsholat.data.repository.SettingsRepositoryImpl
import com.marsholat.domain.repository.PrayerScheduleRepository
import com.marsholat.domain.repository.SettingsRepository

class MarSholatApplication : Application() {

    lateinit var database: MarSholatDatabase
        private set

    lateinit var preferencesManager: AppPreferencesManager
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var prayerScheduleRepository: PrayerScheduleRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = MarSholatDatabase.getDatabase(this)
        preferencesManager = AppPreferencesManager(this)
        settingsRepository = SettingsRepositoryImpl(preferencesManager)
        prayerScheduleRepository = PrayerScheduleRepositoryImpl()
    }

    companion object {
        lateinit var instance: MarSholatApplication
            private set
    }
}

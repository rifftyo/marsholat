package com.marsholat

import com.marsholat.core.model.AsrJuristicMethod
import com.marsholat.core.model.CalculationMethod
import com.marsholat.core.model.PrePrayerReminderInterval
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ReminderType
import com.marsholat.core.model.ThemeMode
import com.marsholat.domain.model.PrayerAdjustment
import com.marsholat.domain.model.PrayerNotificationSetting
import com.marsholat.domain.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsAndNotificationTest {

    @Test
    fun testDefaultUserSettings() {
        val defaultSettings = UserSettings()
        assertEquals(ThemeMode.SYSTEM, defaultSettings.themeMode)
        assertEquals(CalculationMethod.KEMENAG, defaultSettings.calculationMethod)
        assertEquals(AsrJuristicMethod.STANDARD, defaultSettings.asrJuristicMethod)
        assertEquals("Jakarta", defaultSettings.location.cityName)
        assertTrue(defaultSettings.is24HourFormat)
    }

    @Test
    fun testPrayerNotificationDefaultValues() {
        val notifSubuh = PrayerNotificationSetting(prayerType = PrayerType.SUBUH)
        assertTrue(notifSubuh.isEnabled)
        assertEquals(ReminderType.ADZAN, notifSubuh.reminderType)
        assertEquals(PrePrayerReminderInterval.OFF, notifSubuh.preReminderInterval)

        val updated = notifSubuh.copy(
            reminderType = ReminderType.NOTIFICATION,
            preReminderInterval = PrePrayerReminderInterval.MINUTES_10
        )
        assertEquals(ReminderType.NOTIFICATION, updated.reminderType)
        assertEquals(10, updated.preReminderInterval.minutes)
    }

    @Test
    fun testPrayerAdjustments() {
        val adjustments = PrayerAdjustment(subuhMinutes = 2, maghribMinutes = -1)
        assertEquals(2, adjustments.getOffset(PrayerType.SUBUH))
        assertEquals(-1, adjustments.getOffset(PrayerType.MAGHRIB))
        assertEquals(0, adjustments.getOffset(PrayerType.DZUHUR))
    }
}

package com.marsholat

import com.marsholat.core.model.AsrJuristicMethod
import com.marsholat.core.model.CalculationMethod
import com.marsholat.core.util.CityDatabase
import com.marsholat.core.util.HijriCalendarHelper
import com.marsholat.core.util.PrayerCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PrayerCalculatorTest {

    @Test
    fun testJakartaPrayerTimesOrderAndSanity() {
        // Jakarta: lat -6.2088, lon 106.8456, UTC+7
        val testDate = LocalDate.of(2026, 3, 15)
        val schedule = PrayerCalculator.calculateSchedule(
            date = testDate,
            latitude = -6.2088,
            longitude = 106.8456,
            timeZoneOffsetHours = 7.0,
            method = CalculationMethod.KEMENAG,
            asrJuristic = AsrJuristicMethod.STANDARD
        )

        // Subuh should be around 04:30 - 05:00
        assertTrue("Subuh should be before sunrise", schedule.subuh.isBefore(schedule.terbit))
        assertTrue("Sunrise should be before Dzuhur", schedule.terbit.isBefore(schedule.dzuhur))
        assertTrue("Dzuhur should be before Ashar", schedule.dzuhur.isBefore(schedule.ashar))
        assertTrue("Ashar should be before Maghrib", schedule.ashar.isBefore(schedule.maghrib))
        assertTrue("Maghrib should be before Isya", schedule.maghrib.isBefore(schedule.isya))

        // Check hour ranges for Jakarta
        assertEquals(4, schedule.subuh.hour)
        assertEquals(12, schedule.dzuhur.hour)
        assertEquals(15, schedule.ashar.hour)
        assertEquals(18, schedule.maghrib.hour)
        assertEquals(19, schedule.isya.hour)
    }

    @Test
    fun testHijriCalendarConverter() {
        val testDate = LocalDate.of(2026, 11, 12)
        val hijriDate = HijriCalendarHelper.getHijriDate(testDate)

        assertNotNull(hijriDate)
        assertTrue("Year should be around 1448 H", hijriDate.year in 1447..1449)
        assertTrue("Day should be between 1 and 30", hijriDate.day in 1..30)
        assertTrue("Month should be between 1 and 12", hijriDate.monthNumber in 1..12)
        assertTrue("Month name should not be empty", hijriDate.monthName.isNotEmpty())
    }

    @Test
    fun testCityDatabaseNearest() {
        // Point close to Bandung (-6.9, 107.6)
        val nearest = CityDatabase.findNearestCity(-6.91, 107.61)
        assertEquals("Bandung", nearest.cityName)
    }
}

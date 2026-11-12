package com.marsholat

import com.marsholat.core.model.PrayerStatus
import com.marsholat.core.model.PrayerType
import com.marsholat.domain.model.DailyTrackerSummary
import com.marsholat.domain.model.TrackerEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PrayerTrackerTest {

    @Test
    fun testDailyTrackerProgressCalculation() {
        val today = LocalDate.of(2026, 11, 12)
        val entries = listOf(
            TrackerEntry(date = today, prayerType = PrayerType.SUBUH, status = PrayerStatus.COMPLETED),
            TrackerEntry(date = today, prayerType = PrayerType.DZUHUR, status = PrayerStatus.COMPLETED),
            TrackerEntry(date = today, prayerType = PrayerType.ASHAR, status = PrayerStatus.COMPLETED),
            TrackerEntry(date = today, prayerType = PrayerType.MAGHRIB, status = PrayerStatus.NOT_COMPLETED),
            TrackerEntry(date = today, prayerType = PrayerType.ISYA, status = PrayerStatus.MISSED)
        )

        val summary = DailyTrackerSummary(
            date = today,
            entries = entries,
            completedCount = 3,
            totalCount = 5
        )

        assertEquals(3, summary.completedCount)
        assertEquals(5, summary.totalCount)
        assertEquals(60, summary.percentage)
        assertEquals(0.6f, summary.progressFraction, 0.001f)
    }

    @Test
    fun testPrayerStatusTransitions() {
        var status = PrayerStatus.NOT_COMPLETED
        status = PrayerStatus.COMPLETED
        assertEquals(PrayerStatus.COMPLETED, status)

        status = PrayerStatus.MISSED
        assertEquals(PrayerStatus.MISSED, status)
    }
}

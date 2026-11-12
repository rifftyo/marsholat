package com.marsholat.data.repository

import com.marsholat.core.model.PrayerType
import com.marsholat.core.util.HijriCalendarHelper
import com.marsholat.core.util.PrayerCalculator
import com.marsholat.domain.model.HijriDate
import com.marsholat.domain.model.NextPrayerCountdown
import com.marsholat.domain.model.PrayerSchedule
import com.marsholat.domain.model.UserSettings
import com.marsholat.domain.repository.PrayerScheduleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

class PrayerScheduleRepositoryImpl : PrayerScheduleRepository {

    override suspend fun getScheduleForDate(date: LocalDate, settings: UserSettings): PrayerSchedule {
        val zoneId = runCatching { ZoneId.of(settings.location.timeZoneId) }.getOrDefault(ZoneId.systemDefault())
        val offsetHours = zoneId.rules.getOffset(date.atStartOfDay()).totalSeconds / 3600.0

        return PrayerCalculator.calculateSchedule(
            date = date,
            latitude = settings.location.latitude,
            longitude = settings.location.longitude,
            timeZoneOffsetHours = offsetHours,
            method = settings.calculationMethod,
            asrJuristic = settings.asrJuristicMethod,
            adjustments = settings.adjustments
        )
    }

    override suspend fun getMonthlySchedule(
        yearMonth: YearMonth,
        settings: UserSettings
    ): Map<LocalDate, PrayerSchedule> {
        val result = mutableMapOf<LocalDate, PrayerSchedule>()
        val zoneId = runCatching { ZoneId.of(settings.location.timeZoneId) }.getOrDefault(ZoneId.systemDefault())
        val daysInMonth = yearMonth.lengthOfMonth()

        for (day in 1..daysInMonth) {
            val date = yearMonth.atDay(day)
            val offsetHours = zoneId.rules.getOffset(date.atStartOfDay()).totalSeconds / 3600.0
            val schedule = PrayerCalculator.calculateSchedule(
                date = date,
                latitude = settings.location.latitude,
                longitude = settings.location.longitude,
                timeZoneOffsetHours = offsetHours,
                method = settings.calculationMethod,
                asrJuristic = settings.asrJuristicMethod,
                adjustments = settings.adjustments
            )
            result[date] = schedule
        }
        return result
    }

    override fun getNextPrayerCountdownFlow(settings: UserSettings): Flow<NextPrayerCountdown> = flow {
        while (true) {
            val now = LocalDateTime.now()
            val today = now.toLocalDate()
            val todaySchedule = getScheduleForDate(today, settings)
            val tomorrowSchedule = getScheduleForDate(today.plusDays(1), settings)

            // Determine next prayer and time window
            val prayersToday = listOf(
                PrayerType.SUBUH to todaySchedule.subuh.atDate(today),
                PrayerType.DZUHUR to todaySchedule.dzuhur.atDate(today),
                PrayerType.ASHAR to todaySchedule.ashar.atDate(today),
                PrayerType.MAGHRIB to todaySchedule.maghrib.atDate(today),
                PrayerType.ISYA to todaySchedule.isya.atDate(today)
            )

            val nextPrayerPair: Pair<PrayerType, LocalDateTime>
            val previousPrayerTime: LocalDateTime

            val nextIndex = prayersToday.indexOfFirst { it.second.isAfter(now) }
            if (nextIndex != -1) {
                nextPrayerPair = prayersToday[nextIndex]
                previousPrayerTime = if (nextIndex > 0) {
                    prayersToday[nextIndex - 1].second
                } else {
                    // Before Subuh: previous was yesterday's Isya
                    val yesterday = today.minusDays(1)
                    val yesterdaySchedule = getScheduleForDate(yesterday, settings)
                    yesterdaySchedule.isya.atDate(yesterday)
                }
            } else {
                // After Isya: next is tomorrow's Subuh
                val tomorrowSubuh = tomorrowSchedule.subuh.atDate(today.plusDays(1))
                nextPrayerPair = PrayerType.SUBUH to tomorrowSubuh
                previousPrayerTime = todaySchedule.isya.atDate(today)
            }

            val nextPrayer = nextPrayerPair.first
            val nextDateTime = nextPrayerPair.second
            val duration = Duration.between(now, nextDateTime)
            val remainingMillis = duration.toMillis().coerceAtLeast(0)

            val totalWindowDuration = Duration.between(previousPrayerTime, nextDateTime).toMillis().coerceAtLeast(1)
            val elapsed = Duration.between(previousPrayerTime, now).toMillis().coerceAtLeast(0)
            val progress = (elapsed.toFloat() / totalWindowDuration.toFloat()).coerceIn(0f, 1f)

            val hours = duration.toHours()
            val minutes = (duration.toMinutes() % 60)
            val seconds = (duration.seconds % 60)

            emit(
                NextPrayerCountdown(
                    nextPrayer = nextPrayer,
                    nextPrayerTime = nextDateTime.toLocalTime(),
                    remainingMillis = remainingMillis,
                    progress = progress,
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds
                )
            )

            delay(1000)
        }
    }

    override fun getHijriDate(date: LocalDate): HijriDate {
        return HijriCalendarHelper.getHijriDate(date)
    }
}

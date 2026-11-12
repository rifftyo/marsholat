package com.marsholat.domain.model

import com.marsholat.core.model.AsrJuristicMethod
import com.marsholat.core.model.CalculationMethod
import com.marsholat.core.model.PrePrayerReminderInterval
import com.marsholat.core.model.PrayerStatus
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ReminderType
import com.marsholat.core.model.ThemeMode
import java.time.LocalDate
import java.time.LocalTime

data class LocationData(
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val cityName: String = "Jakarta",
    val region: String = "DKI Jakarta, Indonesia",
    val timeZoneId: String = "Asia/Jakarta"
)

data class PrayerSchedule(
    val date: LocalDate,
    val imsak: LocalTime,
    val subuh: LocalTime,
    val terbit: LocalTime,
    val dhuha: LocalTime,
    val dzuhur: LocalTime,
    val ashar: LocalTime,
    val maghrib: LocalTime,
    val isya: LocalTime
) {
    fun getTime(prayerType: PrayerType): LocalTime {
        return when (prayerType) {
            PrayerType.SUBUH -> subuh
            PrayerType.DZUHUR -> dzuhur
            PrayerType.ASHAR -> ashar
            PrayerType.MAGHRIB -> maghrib
            PrayerType.ISYA -> isya
        }
    }
}

data class PrayerTimeItem(
    val type: PrayerType,
    val time: LocalTime,
    val isNext: Boolean = false,
    val isCurrent: Boolean = false,
    val isPassed: Boolean = false
)

data class HijriDate(
    val day: Int,
    val monthNumber: Int,
    val monthName: String,
    val year: Int
) {
    val formatted: String
        get() = "$day $monthName $year H"
}

data class NextPrayerCountdown(
    val nextPrayer: PrayerType,
    val nextPrayerTime: LocalTime,
    val remainingMillis: Long,
    val progress: Float, // 0f to 1f
    val hours: Long,
    val minutes: Long,
    val seconds: Long
) {
    val formattedTime: String
        get() = "%02d:%02d:%02d".format(hours, minutes, seconds)
}

data class PrayerNotificationSetting(
    val prayerType: PrayerType,
    val isEnabled: Boolean = true,
    val reminderType: ReminderType = ReminderType.ADZAN,
    val preReminderInterval: PrePrayerReminderInterval = PrePrayerReminderInterval.OFF
)

data class PrayerAdjustment(
    val subuhMinutes: Int = 0,
    val dzuhurMinutes: Int = 0,
    val asharMinutes: Int = 0,
    val maghribMinutes: Int = 0,
    val isyaMinutes: Int = 0
) {
    fun getOffset(prayerType: PrayerType): Int {
        return when (prayerType) {
            PrayerType.SUBUH -> subuhMinutes
            PrayerType.DZUHUR -> dzuhurMinutes
            PrayerType.ASHAR -> asharMinutes
            PrayerType.MAGHRIB -> maghribMinutes
            PrayerType.ISYA -> isyaMinutes
        }
    }
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val calculationMethod: CalculationMethod = CalculationMethod.KEMENAG,
    val asrJuristicMethod: AsrJuristicMethod = AsrJuristicMethod.STANDARD,
    val adjustments: PrayerAdjustment = PrayerAdjustment(),
    val location: LocationData = LocationData(),
    val is24HourFormat: Boolean = true
)

data class TrackerEntry(
    val id: Long = 0,
    val date: LocalDate,
    val prayerType: PrayerType,
    val status: PrayerStatus = PrayerStatus.NOT_COMPLETED,
    val updatedAt: Long = System.currentTimeMillis()
)

data class DailyTrackerSummary(
    val date: LocalDate,
    val entries: List<TrackerEntry>,
    val completedCount: Int,
    val totalCount: Int = 5
) {
    val progressFraction: Float
        get() = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    val percentage: Int
        get() = (progressFraction * 100).toInt()
}

data class DayStats(
    val date: LocalDate,
    val completedCount: Int,
    val totalCount: Int = 5
)

data class WeeklyTrackerStats(
    val days: List<DayStats>,
    val totalCompleted: Int,
    val totalPrayers: Int,
    val consistencyPercentage: Int
)

data class MonthlyTrackerStats(
    val yearMonth: String,
    val totalCompleted: Int,
    val totalExpected: Int,
    val consistencyPercentage: Int,
    val streakDays: Int
)

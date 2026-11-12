package com.marsholat.core.util

import com.marsholat.domain.model.HijriDate
import java.time.LocalDate
import kotlin.math.floor

/**
 * 100% Offline algorithmic Hijri calendar converter based on the
 * standard astronomical tabular Islamic calendar algorithm.
 */
object HijriCalendarHelper {

    private val MONTH_NAMES_ID = listOf(
        "Muharram",
        "Safar",
        "Rabi'ul Awwal",
        "Rabi'ul Akhir",
        "Jumadil Awwal",
        "Jumadil Akhir",
        "Rajab",
        "Sya'ban",
        "Ramadhan",
        "Syawal",
        "Dzulqa'dah",
        "Dzulhijjah"
    )

    fun getHijriDate(date: LocalDate): HijriDate {
        val jd = PrayerCalculator.toJulianDay(date.year, date.monthValue, date.dayOfMonth)

        // Julian Day corresponding to Islamic epoch (July 16, 622 CE) is 1948439.5
        val l = jd - 1948440 + 10632
        val n = floor((l - 1) / 10631.0)
        val lSub = l - 10631 * n + 354
        val j = (floor((10985 - lSub) / 5316.0)) * (floor((50 * lSub) / 17719.0)) +
                (floor(lSub / 5670.0)) * (floor((43 * lSub) / 15238.0))
        val lSub2 = lSub - (floor((30 - j) / 15.0)) * (floor((17719 * j) / 50.0)) -
                (floor(j / 16.0)) * (floor((15238 * j) / 43.0)) + 29
        val month = (floor((24 * lSub2) / 709.0)).toInt()
        val day = (lSub2 - floor((709 * month) / 24.0)).toInt()
        val year = (30 * n + j - 30).toInt()

        val clampedMonth = month.coerceIn(1, 12)
        val monthName = MONTH_NAMES_ID.getOrElse(clampedMonth - 1) { "Muharram" }

        return HijriDate(
            day = day.coerceIn(1, 30),
            monthNumber = clampedMonth,
            monthName = monthName,
            year = year
        )
    }

    fun getIndonesianMonthName(monthNumber: Int): String {
        return MONTH_NAMES_ID.getOrElse(monthNumber - 1) { "" }
    }
}

package com.marsholat.core.util

import com.marsholat.core.model.AsrJuristicMethod
import com.marsholat.core.model.CalculationMethod
import com.marsholat.core.model.PrayerType
import com.marsholat.domain.model.PrayerAdjustment
import com.marsholat.domain.model.PrayerSchedule
import com.marsholat.domain.model.UserSettings
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * High-precision offline astronomical prayer times calculator.
 * Supports Kemenag RI (Indonesian standard with +2m Ihtiyat), MWL, Egyptian,
 * Umm Al-Qura, Karachi, and ISNA calculation methods.
 */
object PrayerCalculator {

    private const val RAD = Math.PI / 180.0
    private const val DEG = 180.0 / Math.PI

    data class SolarCoordinates(
        val declination: Double,    // in degrees
        val equationOfTime: Double  // in minutes
    )

    /**
     * Compute solar coordinates for a given Julian Day
     */
    fun getSolarCoordinates(julianDay: Double): SolarCoordinates {
        val d = julianDay - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(g * RAD) + 0.020 * sin(2 * g * RAD))

        val e = 23.439 - 0.00000036 * d
        val ra = atan2(cos(e * RAD) * sin(l * RAD), cos(l * RAD)) * DEG
        val fixedRa = fixHour(ra / 15.0)

        val declination = asin(sin(e * RAD) * sin(l * RAD)) * DEG
        val equationOfTime = (q / 15.0 - fixedRa) * 60.0

        return SolarCoordinates(declination, equationOfTime)
    }

    /**
     * Convert Julian Day from Gregorian date
     */
    fun toJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    /**
     * Calculate sun angle time (time difference in hours from solar noon for a given sun angle)
     */
    private fun sunAngleTime(angle: Double, latitude: Double, declination: Double, ccw: Boolean): Double {
        val cosH = (-sin(angle * RAD) - sin(latitude * RAD) * sin(declination * RAD)) /
                (cos(latitude * RAD) * cos(declination * RAD))

        if (cosH > 1.0 || cosH < -1.0) {
            return if (ccw) 0.0 else 12.0 // polar day/night handling
        }

        val h = acos(cosH) * DEG / 15.0
        return if (ccw) -h else h
    }

    /**
     * Calculate Asr time difference from solar noon based on shadow multiplier
     */
    private fun asrTime(shadowMultiplier: Double, latitude: Double, declination: Double): Double {
        // Sun elevation angle for Asr: arccot(m + tan(|lat - dec|)) = atan(1 / (m + tan(|lat - dec|)))
        val altitude = atan(1.0 / (shadowMultiplier + tan(abs(latitude - declination) * RAD))) * DEG
        val cosH = (sin(altitude * RAD) - sin(latitude * RAD) * sin(declination * RAD)) /
                (cos(latitude * RAD) * cos(declination * RAD))

        if (cosH > 1.0 || cosH < -1.0) return 3.0 // sensible fallback in hours (~3 hours after noon)
        return acos(cosH) * DEG / 15.0
    }

    /**
     * Calculate complete prayer schedule for a given date and settings
     */
    fun calculateSchedule(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        timeZoneOffsetHours: Double,
        method: CalculationMethod = CalculationMethod.KEMENAG,
        asrJuristic: AsrJuristicMethod = AsrJuristicMethod.STANDARD,
        adjustments: PrayerAdjustment = PrayerAdjustment()
    ): PrayerSchedule {
        val jd = toJulianDay(date.year, date.monthValue, date.dayOfMonth)
        val solar = getSolarCoordinates(jd)

        // Solar noon (Dhuhr base)
        val midDay = 12.0 + timeZoneOffsetHours - (longitude / 15.0) - (solar.equationOfTime / 60.0)

        // Fajr (Subuh)
        val fajrAngle = method.fajrAngle
        val fajrDiff = sunAngleTime(fajrAngle, latitude, solar.declination, ccw = true)
        var fajrHours = midDay + fajrDiff

        // Sunrise (Terbit)
        val sunriseDiff = sunAngleTime(0.833, latitude, solar.declination, ccw = true)
        val sunriseHours = midDay + sunriseDiff

        // Dhuha (approx 4.5 degrees sun altitude, ~20 mins after sunrise)
        val dhuhaDiff = sunAngleTime(-4.5, latitude, solar.declination, ccw = true)
        val dhuhaHours = midDay + dhuhaDiff

        // Dhuhr (Dzuhur)
        var dhuhrHours = midDay

        // Asr (Ashar)
        val asrDiff = asrTime(asrJuristic.shadowMultiplier, latitude, solar.declination)
        var asrHours = midDay + asrDiff

        // Maghrib (Sunset)
        val sunsetDiff = sunAngleTime(0.833, latitude, solar.declination, ccw = false)
        var maghribHours = midDay + sunsetDiff

        // Isha (Isya)
        var ishaHours = if (method == CalculationMethod.UMM_AL_QURA) {
            maghribHours + (90.0 / 60.0) // 90 minutes after Maghrib
        } else {
            val ishaDiff = sunAngleTime(method.ishaAngle, latitude, solar.declination, ccw = false)
            midDay + ishaDiff
        }

        // Kemenag RI Standard: Apply +2 minutes Ihtiyat (safety buffer) to prayer times
        val isKemenag = method == CalculationMethod.KEMENAG
        val ihtiyatMinutes = if (isKemenag) 2 else 0

        // Convert decimal hours to LocalTime with Ihtiyat and manual user adjustments
        val subuhTime = toLocalTime(fajrHours, ihtiyatMinutes + adjustments.subuhMinutes)
        val imsakTime = subuhTime.minusMinutes(10) // Standard Indonesian Imsak: 10 mins before Subuh
        val terbitTime = toLocalTime(sunriseHours, 0)
        val dhuhaTime = toLocalTime(dhuhaHours, 0)
        val dzuhurTime = toLocalTime(dhuhrHours, ihtiyatMinutes + adjustments.dzuhurMinutes)
        val asharTime = toLocalTime(asrHours, ihtiyatMinutes + adjustments.asharMinutes)
        val maghribTime = toLocalTime(maghribHours, ihtiyatMinutes + adjustments.maghribMinutes)
        val isyaTime = toLocalTime(ishaHours, ihtiyatMinutes + adjustments.isyaMinutes)

        return PrayerSchedule(
            date = date,
            imsak = imsakTime,
            subuh = subuhTime,
            terbit = terbitTime,
            dhuha = dhuhaTime,
            dzuhur = dzuhurTime,
            ashar = asharTime,
            maghrib = maghribTime,
            isya = isyaTime
        )
    }

    private fun toLocalTime(decimalHours: Double, addedMinutes: Int): LocalTime {
        var totalMinutes = (decimalHours * 60.0) + addedMinutes
        totalMinutes = (totalMinutes % (24 * 60) + (24 * 60)) % (24 * 60)

        val hours = (totalMinutes / 60).toInt()
        val minutes = (totalMinutes % 60).toInt()
        return LocalTime.of(hours.coerceIn(0, 23), minutes.coerceIn(0, 59))
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour - 24.0 * floor(hour / 24.0)
        if (h < 0) h += 24.0
        return h
    }
}

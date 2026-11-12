package com.marsholat.core.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {

    private val ID_LOCALE = Locale.forLanguageTag("id-ID")

    fun formatGregorianDate(date: LocalDate): String {
        val dayName = when (date.dayOfWeek.value) {
            1 -> "Senin"
            2 -> "Selasa"
            3 -> "Rabu"
            4 -> "Kamis"
            5 -> "Jumat"
            6 -> "Sabtu"
            7 -> "Minggu"
            else -> ""
        }

        val monthName = when (date.monthValue) {
            1 -> "Januari"
            2 -> "Februari"
            3 -> "Maret"
            4 -> "April"
            5 -> "Mei"
            6 -> "Juni"
            7 -> "Juli"
            8 -> "Agustus"
            9 -> "September"
            10 -> "Oktober"
            11 -> "November"
            12 -> "Desember"
            else -> ""
        }

        return "$dayName, ${date.dayOfMonth} $monthName ${date.year}"
    }

    fun formatShortDate(date: LocalDate): String {
        val monthShort = when (date.monthValue) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "Mei"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Agu"
            9 -> "Sep"
            10 -> "Okt"
            11 -> "Nov"
            12 -> "Des"
            else -> ""
        }
        return "${date.dayOfMonth} $monthShort"
    }

    fun formatTime(time: LocalTime, is24Hour: Boolean = true): String {
        return if (is24Hour) {
            "%02d:%02d".format(time.hour, time.minute)
        } else {
            val amPm = if (time.hour < 12) "AM" else "PM"
            val hour12 = when (val h = time.hour % 12) {
                0 -> 12
                else -> h
            }
            "%02d:%02d %s".format(hour12, time.minute, amPm)
        }
    }

    fun getIslamicGreeting(): String {
        val currentHour = LocalTime.now().hour
        return when (currentHour) {
            in 3..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..17 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }
}

package com.marsholat.core.model

enum class PrayerType(val displayName: String, val order: Int) {
    SUBUH("Subuh", 1),
    DZUHUR("Dzuhur", 2),
    ASHAR("Ashar", 3),
    MAGHRIB("Maghrib", 4),
    ISYA("Isya", 5);

    companion object {
        fun fromOrder(order: Int): PrayerType = entries.firstOrNull { it.order == order } ?: SUBUH
        fun fromName(name: String): PrayerType = entries.firstOrNull {
            it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true)
        } ?: SUBUH
    }
}

enum class PrayerStatus(val displayName: String) {
    NOT_COMPLETED("Belum"),
    COMPLETED("Sudah"),
    MISSED("Terlewat")
}

enum class ReminderType(val displayName: String) {
    ADZAN("Adzan"),
    NOTIFICATION("Notifikasi"),
    ALARM("Alarm")
}

enum class PrePrayerReminderInterval(val minutes: Int, val displayName: String) {
    OFF(0, "Nonaktif"),
    MINUTES_5(5, "5 menit sebelumnya"),
    MINUTES_10(10, "10 menit sebelumnya"),
    MINUTES_15(15, "15 menit sebelumnya"),
    MINUTES_30(30, "30 menit sebelumnya");

    companion object {
        fun fromMinutes(minutes: Int): PrePrayerReminderInterval {
            return entries.firstOrNull { it.minutes == minutes } ?: OFF
        }
    }
}

enum class ThemeMode(val displayName: String) {
    SYSTEM("Default Sistem"),
    LIGHT("Mode Terang"),
    DARK("Mode Gelap")
}

enum class CalculationMethod(val displayName: String, val fajrAngle: Double, val ishaAngle: Double) {
    KEMENAG("Kemenag RI (Indonesia)", 20.0, 18.0),
    MWL("Muslim World League", 18.0, 17.0),
    EGYPT("Egyptian General Authority", 19.5, 17.5),
    UMM_AL_QURA("Umm Al-Qura (Makkah)", 18.5, 19.0),
    KARACHI("Univ. Islamic Sciences Karachi", 18.0, 18.0),
    ISNA("ISNA (North America)", 15.0, 15.0)
}

enum class AsrJuristicMethod(val displayName: String, val shadowMultiplier: Double) {
    STANDARD("Standar (Syafi'i, Maliki, Hanbali)", 1.0),
    HANAFI("Hanafi", 2.0)
}

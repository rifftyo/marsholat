package com.marsholat.core.util

import com.marsholat.domain.model.LocationData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object CityDatabase {

    val INDONESIAN_CITIES = listOf(
        LocationData(-6.2088, 106.8456, "Jakarta", "DKI Jakarta, Indonesia", "Asia/Jakarta"),
        LocationData(-7.2575, 112.7521, "Surabaya", "Jawa Timur, Indonesia", "Asia/Jakarta"),
        LocationData(-6.9175, 107.6191, "Bandung", "Jawa Barat, Indonesia", "Asia/Jakarta"),
        LocationData(3.5952, 98.6722, "Medan", "Sumatera Utara, Indonesia", "Asia/Jakarta"),
        LocationData(-6.9667, 110.4167, "Semarang", "Jawa Tengah, Indonesia", "Asia/Jakarta"),
        LocationData(-5.1477, 119.4327, "Makassar", "Sulawesi Selatan, Indonesia", "Asia/Makassar"),
        LocationData(-2.9761, 104.7754, "Palembang", "Sumatera Selatan, Indonesia", "Asia/Jakarta"),
        LocationData(-5.4500, 105.2667, "Bandar Lampung", "Lampung, Indonesia", "Asia/Jakarta"),
        LocationData(1.0456, 104.0305, "Batam", "Kepulauan Riau, Indonesia", "Asia/Jakarta"),
        LocationData(-0.9471, 100.4172, "Padang", "Sumatera Barat, Indonesia", "Asia/Jakarta"),
        LocationData(0.5071, 101.4478, "Pekanbaru", "Riau, Indonesia", "Asia/Jakarta"),
        LocationData(-8.6705, 115.2126, "Denpasar", "Bali, Indonesia", "Asia/Makassar"),
        LocationData(-0.5022, 117.1537, "Samarinda", "Kalimantan Timur, Indonesia", "Asia/Makassar"),
        LocationData(-3.3194, 114.5908, "Banjarmasin", "Kalimantan Selatan, Indonesia", "Asia/Makassar"),
        LocationData(-0.0263, 109.3425, "Pontianak", "Kalimantan Barat, Indonesia", "Asia/Jakarta"),
        LocationData(-1.2379, 116.8529, "Balikpapan", "Kalimantan Timur, Indonesia", "Asia/Makassar"),
        LocationData(-7.7956, 110.3695, "Yogyakarta", "D.I. Yogyakarta, Indonesia", "Asia/Jakarta"),
        LocationData(-7.9797, 112.6304, "Malang", "Jawa Timur, Indonesia", "Asia/Jakarta"),
        LocationData(-7.5755, 110.8243, "Surakarta", "Jawa Tengah, Indonesia", "Asia/Jakarta"),
        LocationData(5.5483, 95.3238, "Banda Aceh", "Aceh, Indonesia", "Asia/Jakarta"),
        LocationData(-8.5833, 116.1167, "Mataram", "Nusa Tenggara Barat, Indonesia", "Asia/Makassar"),
        LocationData(-10.1772, 123.6070, "Kupang", "Nusa Tenggara Timur, Indonesia", "Asia/Makassar"),
        LocationData(1.4748, 124.8421, "Manado", "Sulawesi Utara, Indonesia", "Asia/Makassar"),
        LocationData(-3.6554, 128.1908, "Ambon", "Maluku, Indonesia", "Asia/Jayapura"),
        LocationData(-2.5489, 140.7181, "Jayapura", "Papua, Indonesia", "Asia/Jayapura"),
        LocationData(-0.8917, 119.8707, "Palu", "Sulawesi Tengah, Indonesia", "Asia/Makassar"),
        LocationData(-3.9722, 122.5833, "Kendari", "Sulawesi Tenggara, Indonesia", "Asia/Makassar"),
        LocationData(0.5435, 123.0568, "Gorontalo", "Gorontalo, Indonesia", "Asia/Makassar"),
        LocationData(-0.8988, 131.2599, "Sorong", "Papua Barat Daya, Indonesia", "Asia/Jayapura"),
        LocationData(21.4225, 39.8262, "Makkah", "Makkah, Saudi Arabia", "Asia/Riyadh"),
        LocationData(24.5247, 39.5692, "Madinah", "Madinah, Saudi Arabia", "Asia/Riyadh")
    )

    fun findNearestCity(latitude: Double, longitude: Double): LocationData {
        var minDistance = Double.MAX_VALUE
        var closest = INDONESIAN_CITIES.first()

        for (city in INDONESIAN_CITIES) {
            val dist = haversineDistanceKm(latitude, longitude, city.latitude, city.longitude)
            if (dist < minDistance) {
                minDistance = dist
                closest = city
            }
        }
        return closest
    }

    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

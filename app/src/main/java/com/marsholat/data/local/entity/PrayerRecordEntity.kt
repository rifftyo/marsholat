package com.marsholat.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prayer_records",
    indices = [
        Index(value = ["date", "prayer_name"], unique = true),
        Index(value = ["date"])
    ]
)
data class PrayerRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "date")
    val date: String, // ISO-8601 YYYY-MM-DD
    @ColumnInfo(name = "prayer_name")
    val prayerName: String, // SUBUH, DZUHUR, ASHAR, MAGHRIB, ISYA
    @ColumnInfo(name = "status")
    val status: String, // NOT_COMPLETED, COMPLETED, MISSED
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

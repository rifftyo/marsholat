package com.marsholat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marsholat.data.local.entity.PrayerRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerRecordDao {

    @Query("SELECT * FROM prayer_records WHERE date = :date")
    fun getRecordsForDateFlow(date: String): Flow<List<PrayerRecordEntity>>

    @Query("SELECT * FROM prayer_records WHERE date = :date")
    fun getRecordsForDate(date: String): List<PrayerRecordEntity>

    @Query("SELECT * FROM prayer_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRecordsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<PrayerRecordEntity>>

    @Query("SELECT * FROM prayer_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRecordsBetweenDates(startDate: String, endDate: String): List<PrayerRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(record: PrayerRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateAll(records: List<PrayerRecordEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM prayer_records WHERE status = 'COMPLETED' AND date BETWEEN :startDate AND :endDate")
    fun countCompletedBetween(startDate: String, endDate: String): Int

    @Query("SELECT COUNT(*) FROM prayer_records WHERE date BETWEEN :startDate AND :endDate")
    fun countTotalRecordedBetween(startDate: String, endDate: String): Int
}

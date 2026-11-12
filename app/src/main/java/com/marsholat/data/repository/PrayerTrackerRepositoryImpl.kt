package com.marsholat.data.repository

import com.marsholat.core.model.PrayerStatus
import com.marsholat.core.model.PrayerType
import com.marsholat.data.local.dao.PrayerRecordDao
import com.marsholat.data.local.entity.PrayerRecordEntity
import com.marsholat.domain.model.DailyTrackerSummary
import com.marsholat.domain.model.DayStats
import com.marsholat.domain.model.MonthlyTrackerStats
import com.marsholat.domain.model.TrackerEntry
import com.marsholat.domain.model.WeeklyTrackerStats
import com.marsholat.domain.repository.PrayerTrackerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class PrayerTrackerRepositoryImpl(
    private val prayerRecordDao: PrayerRecordDao
) : PrayerTrackerRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getDailySummaryFlow(date: LocalDate): Flow<DailyTrackerSummary> {
        val dateStr = date.format(dateFormatter)
        return prayerRecordDao.getRecordsForDateFlow(dateStr).map { entities ->
            val entriesMap = entities.associateBy { it.prayerName }

            val trackerEntries = PrayerType.entries.map { prayer ->
                val entity = entriesMap[prayer.name]
                val status = entity?.let {
                    runCatching { PrayerStatus.valueOf(it.status) }.getOrDefault(PrayerStatus.NOT_COMPLETED)
                } ?: PrayerStatus.NOT_COMPLETED

                TrackerEntry(
                    id = entity?.id ?: 0,
                    date = date,
                    prayerType = prayer,
                    status = status,
                    updatedAt = entity?.updatedAt ?: System.currentTimeMillis()
                )
            }

            val completed = trackerEntries.count { it.status == PrayerStatus.COMPLETED }
            DailyTrackerSummary(
                date = date,
                entries = trackerEntries,
                completedCount = completed,
                totalCount = 5
            )
        }
    }

    override suspend fun updatePrayerStatus(date: LocalDate, prayer: PrayerType, status: PrayerStatus) {
        withContext(Dispatchers.IO) {
            val dateStr = date.format(dateFormatter)
            val existing = prayerRecordDao.getRecordsForDate(dateStr).firstOrNull { it.prayerName == prayer.name }

            val entity = PrayerRecordEntity(
                id = existing?.id ?: 0,
                date = dateStr,
                prayerName = prayer.name,
                status = status.name,
                updatedAt = System.currentTimeMillis()
            )
            prayerRecordDao.insertOrUpdate(entity)
        }
    }

    override fun getWeeklyStatsFlow(endDate: LocalDate): Flow<WeeklyTrackerStats> {
        val startDate = endDate.minusDays(6)
        val startStr = startDate.format(dateFormatter)
        val endStr = endDate.format(dateFormatter)

        return prayerRecordDao.getRecordsBetweenDatesFlow(startStr, endStr).map { records ->
            val recordsByDate = records.groupBy { it.date }

            val daysList = (0..6).map { dayOffset ->
                val day = startDate.plusDays(dayOffset.toLong())
                val dayStr = day.format(dateFormatter)
                val dayRecords = recordsByDate[dayStr] ?: emptyList()
                val completed = dayRecords.count { it.status == PrayerStatus.COMPLETED.name }
                DayStats(
                    date = day,
                    completedCount = completed,
                    totalCount = 5
                )
            }

            val totalCompleted = daysList.sumOf { it.completedCount }
            val totalPrayers = 7 * 5
            val consistency = if (totalPrayers > 0) (totalCompleted * 100) / totalPrayers else 0

            WeeklyTrackerStats(
                days = daysList,
                totalCompleted = totalCompleted,
                totalPrayers = totalPrayers,
                consistencyPercentage = consistency
            )
        }
    }

    override fun getMonthlyStatsFlow(yearMonth: YearMonth): Flow<MonthlyTrackerStats> {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        val startStr = startDate.format(dateFormatter)
        val endStr = endDate.format(dateFormatter)

        return prayerRecordDao.getRecordsBetweenDatesFlow(startStr, endStr).map { records ->
            val completed = records.count { it.status == PrayerStatus.COMPLETED.name }
            val daysInMonth = yearMonth.lengthOfMonth()
            val totalExpected = daysInMonth * 5
            val consistency = if (totalExpected > 0) (completed * 100) / totalExpected else 0

            // Calculate streak days
            val groupedByDate = records.groupBy { it.date }
            var streak = 0
            var checkDate = LocalDate.now()
            while (!checkDate.isBefore(startDate)) {
                val dStr = checkDate.format(dateFormatter)
                val dayRecs = groupedByDate[dStr] ?: emptyList()
                val dayCompleted = dayRecs.count { it.status == PrayerStatus.COMPLETED.name }
                if (dayCompleted >= 1) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    break
                }
            }

            MonthlyTrackerStats(
                yearMonth = "${yearMonth.monthValue}/${yearMonth.year}",
                totalCompleted = completed,
                totalExpected = totalExpected,
                consistencyPercentage = consistency,
                streakDays = streak
            )
        }
    }
}

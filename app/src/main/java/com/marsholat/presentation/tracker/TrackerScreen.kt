package com.marsholat.presentation.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marsholat.core.model.PrayerStatus
import com.marsholat.core.model.PrayerType
import com.marsholat.core.util.DateTimeUtils
import com.marsholat.domain.model.DailyTrackerSummary
import com.marsholat.domain.model.MonthlyTrackerStats
import com.marsholat.domain.model.WeeklyTrackerStats
import com.marsholat.presentation.components.MarSholatCard
import com.marsholat.ui.theme.StatusSuccess
import com.marsholat.ui.theme.extendedColors
import java.time.LocalDate

@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val schedule by viewModel.schedule.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()

    val isToday = selectedDate == LocalDate.now()
    val canGoForward = !selectedDate.isEqual(LocalDate.now()) && !selectedDate.isAfter(LocalDate.now())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Catatan Ibadah",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.extendedColors.textPrimary
            )
            Text(
                text = "Pantau dan jaga konsistensi shalat lima waktu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.extendedColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Navigation Bar
        MarSholatCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentPadding = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousDay() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Hari Sebelumnya",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isToday) "Hari Ini" else DateTimeUtils.formatShortDate(selectedDate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.extendedColors.textPrimary
                    )
                    Text(
                        text = DateTimeUtils.formatGregorianDate(selectedDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.extendedColors.textSecondary
                    )
                }

                IconButton(
                    onClick = { viewModel.nextDay() },
                    enabled = canGoForward
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Hari Berikutnya",
                        tint = if (canGoForward) MaterialTheme.colorScheme.primary else MaterialTheme.extendedColors.textSecondary.copy(alpha = 0.3f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Progress Card
        dailySummary?.let { summary ->
            DailyProgressCard(summary = summary)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5 Prayers Checklist
        Text(
            text = "Checklist Lima Waktu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.extendedColors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        val entriesMap = dailySummary?.entries?.associateBy { it.prayerType } ?: emptyMap()

        PrayerType.entries.forEach { prayer ->
            val status = entriesMap[prayer]?.status ?: PrayerStatus.NOT_COMPLETED
            val time = schedule?.getTime(prayer)

            PrayerTrackerRowCard(
                prayer = prayer,
                time = time,
                status = status,
                onStatusChanged = { newStatus ->
                    viewModel.updateStatus(prayer, newStatus)
                },
                is24Hour = userSettings.is24HourFormat
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Weekly Statistics Section
        weeklyStats?.let { stats ->
            WeeklyStatsCard(stats = stats)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monthly Statistics Section
        monthlyStats?.let { stats ->
            MonthlyStatsCard(stats = stats)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DailyProgressCard(summary: DailyTrackerSummary) {
    MarSholatCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        contentPadding = 18.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Progres Hari Ini",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${summary.completedCount} dari 5 Shalat Selesai",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.extendedColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (summary.completedCount) {
                        5 -> "MasyaAllah, shalat lima waktu tuntas hari ini!"
                        4 -> "Sedikit lagi, tinggal satu waktu lagi!"
                        in 1..3 -> "Bagus, lanjutkan shalat waktu berikutnya!"
                        else -> "Yuk mulai laksanakan shalat tepat waktu."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.extendedColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Circular progress indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(68.dp)
            ) {
                CircularProgressIndicator(
                    progress = { summary.progressFraction },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${summary.percentage}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun WeeklyStatsCard(stats: WeeklyTrackerStats) {
    MarSholatCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Statistik 7 Hari Terakhir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.extendedColors.textPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${stats.consistencyPercentage}% Konsisten",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7 Day Bar Columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            stats.days.forEach { dayStat ->
                val dayShort = when (dayStat.date.dayOfWeek.value) {
                    1 -> "Sen"; 2 -> "Sel"; 3 -> "Rab"; 4 -> "Kam"; 5 -> "Jum"; 6 -> "Sab"; else -> "Min"
                }
                val fraction = dayStat.completedCount.toFloat() / 5f
                val isToday = dayStat.date == LocalDate.now()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${dayStat.completedCount}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.extendedColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bar container
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((60 * fraction).dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isToday) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = dayShort,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.extendedColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyStatsCard(stats: MonthlyTrackerStats) {
    MarSholatCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Statistik Bulanan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.extendedColors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Consistency % Box
            MarSholatCard(
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                borderWidth = 0.dp,
                contentPadding = 12.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${stats.consistencyPercentage}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Konsistensi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.extendedColors.textSecondary
                    )
                }
            }

            // Streak Days Box
            MarSholatCard(
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                borderWidth = 0.dp,
                contentPadding = 12.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stats.streakDays}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.extendedColors.textPrimary
                        )
                    }
                    Text(
                        text = "Hari Beruntun",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.extendedColors.textSecondary
                    )
                }
            }

            // Total Completed Box
            MarSholatCard(
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                borderWidth = 0.dp,
                contentPadding = 12.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${stats.totalCompleted}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.extendedColors.textPrimary
                    )
                    Text(
                        text = "Shalat Selesai",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.extendedColors.textSecondary
                    )
                }
            }
        }
    }
}

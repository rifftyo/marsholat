package com.marsholat.presentation.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marsholat.core.model.PrayerType
import com.marsholat.core.util.DateTimeUtils
import com.marsholat.domain.model.PrayerSchedule
import com.marsholat.presentation.components.MarSholatCard
import com.marsholat.presentation.components.PrayerRowCard
import com.marsholat.ui.theme.extendedColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Title & Location
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Jadwal Shalat",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.extendedColors.textPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${uiState.userSettings.location.cityName} • ${uiState.userSettings.calculationMethod.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.extendedColors.textSecondary
                )
            }
        }

        // Segmented Tabs: "Hari Ini" vs "Kalender Bulanan"
        PrimaryTabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = uiState.selectedTab == 0,
                onClick = { viewModel.selectTab(0) },
                text = {
                    Text(
                        text = "Hari Ini",
                        fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = { Icon(Icons.Outlined.Today, contentDescription = null) }
            )
            Tab(
                selected = uiState.selectedTab == 1,
                onClick = { viewModel.selectTab(1) },
                text = {
                    Text(
                        text = "Kalender Bulanan",
                        fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) }
            )
        }

        if (uiState.selectedTab == 0) {
            TodayScheduleContent(uiState)
        } else {
            MonthlyCalendarContent(
                uiState = uiState,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onSelectDate = { viewModel.selectDate(it) }
            )
        }
    }
}

@Composable
private fun TodayScheduleContent(uiState: ScheduleUiState) {
    val schedule = uiState.todaySchedule ?: return
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Date Banner
        MarSholatCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            contentPadding = 14.dp
        ) {
            Text(
                text = DateTimeUtils.formatGregorianDate(today),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.extendedColors.textPrimary
            )
            uiState.selectedDateHijri?.let {
                Text(
                    text = it.formatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Extra Astronomical Times: Imsak, Terbit, Dhuha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubTimePill(label = "Imsak", time = schedule.imsak, is24Hour = uiState.userSettings.is24HourFormat, modifier = Modifier.weight(1f))
            SubTimePill(label = "Terbit", time = schedule.terbit, is24Hour = uiState.userSettings.is24HourFormat, modifier = Modifier.weight(1f))
            SubTimePill(label = "Dhuha", time = schedule.dhuha, is24Hour = uiState.userSettings.is24HourFormat, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lima Waktu Utama",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.extendedColors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrayerType.entries.forEach { prayer ->
            PrayerRowCard(
                prayer = prayer,
                time = schedule.getTime(prayer),
                isNext = false,
                is24Hour = uiState.userSettings.is24HourFormat
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SubTimePill(
    label: String,
    time: LocalTime,
    is24Hour: Boolean,
    modifier: Modifier = Modifier
) {
    MarSholatCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentPadding = 10.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.extendedColors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = DateTimeUtils.formatTime(time, is24Hour),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.extendedColors.textPrimary
            )
        }
    }
}

@Composable
private fun MonthlyCalendarContent(
    uiState: ScheduleUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val month = uiState.currentMonth
    val monthName = when (month.monthValue) {
        1 -> "Januari"; 2 -> "Februari"; 3 -> "Maret"; 4 -> "April"; 5 -> "Mei"; 6 -> "Juni"
        7 -> "Juli"; 8 -> "Agustus"; 9 -> "September"; 10 -> "Oktober"; 11 -> "November"; else -> "Desember"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Month Navigation
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
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Bulan Sebelumnya",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "$monthName ${month.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.extendedColors.textPrimary
                )

                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Bulan Berikutnya",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar Grid
        MarSholatCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentPadding = 12.dp
        ) {
            // Day headers
            val daysOfWeek = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.extendedColors.textSecondary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days cells
            val firstDayOfMonth = month.atDay(1)
            // Day of week: 1=Mon .. 7=Sun. In Indonesian calendar starting Sunday: Sun=0, Mon=1, etc.
            val startDayOffset = if (firstDayOfMonth.dayOfWeek == DayOfWeek.SUNDAY) 0 else firstDayOfMonth.dayOfWeek.value
            val totalDays = month.lengthOfMonth()
            val totalCells = ((startDayOffset + totalDays + 6) / 7) * 7

            for (week in 0 until (totalCells / 7)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayCol in 0..6) {
                        val cellIndex = week * 7 + dayCol
                        val dayNumber = cellIndex - startDayOffset + 1

                        if (dayNumber in 1..totalDays) {
                            val cellDate = month.atDay(dayNumber)
                            val isSelected = cellDate == uiState.selectedDate
                            val isToday = cellDate == LocalDate.now()

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onSelectDate(cellDate) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.extendedColors.textPrimary
                                        }
                                    )
                                    if (isToday && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Date Schedule Detail
        uiState.selectedDateSchedule?.let { selectedSched ->
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
                    Column {
                        Text(
                            text = DateTimeUtils.formatGregorianDate(uiState.selectedDate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.extendedColors.textPrimary
                        )
                        uiState.selectedDateHijri?.let {
                            Text(
                                text = it.formatted,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                PrayerType.entries.forEach { prayer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prayer.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.extendedColors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = DateTimeUtils.formatTime(selectedSched.getTime(prayer), uiState.userSettings.is24HourFormat),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

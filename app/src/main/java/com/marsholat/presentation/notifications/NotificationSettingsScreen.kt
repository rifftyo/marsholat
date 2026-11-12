package com.marsholat.presentation.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marsholat.core.model.PrePrayerReminderInterval
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ReminderType
import com.marsholat.domain.model.PrayerNotificationSetting
import com.marsholat.presentation.components.MarSholatCard
import com.marsholat.presentation.components.getPrayerIcon
import com.marsholat.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settingsList by viewModel.notificationSettings.collectAsState()
    val settingsMap = settingsList.associateBy { it.prayerType }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Pengaturan Pengingat",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.extendedColors.textPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Explanatory Banner
            MarSholatCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                contentPadding = 14.dp
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Setiap waktu shalat dapat diatur independen dengan pilihan Adzan lokal, Notifikasi biasa, atau Alarm, serta Smart Reminder sebelum waktu shalat tiba.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.extendedColors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrayerType.entries.forEach { prayer ->
                val setting = settingsMap[prayer] ?: PrayerNotificationSetting(prayerType = prayer)

                PrayerNotificationCard(
                    setting = setting,
                    onToggleChanged = { viewModel.updateToggle(prayer, it) },
                    onTypeChanged = { viewModel.updateReminderType(prayer, it) },
                    onPreIntervalChanged = { viewModel.updatePreReminderInterval(prayer, it) },
                    onTestClicked = { viewModel.testAudioAndNotification(prayer, setting.reminderType) }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PrayerNotificationCard(
    setting: PrayerNotificationSetting,
    onToggleChanged: (Boolean) -> Unit,
    onTypeChanged: (ReminderType) -> Unit,
    onPreIntervalChanged: (PrePrayerReminderInterval) -> Unit,
    onTestClicked: () -> Unit
) {
    MarSholatCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentPadding = 16.dp
    ) {
        // Prayer Header + Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPrayerIcon(setting.prayerType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = setting.prayerType.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.extendedColors.textPrimary
                    )
                    Text(
                        text = if (setting.isEnabled) setting.reminderType.displayName else "Nonaktif",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (setting.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.extendedColors.textSecondary
                    )
                }
            }

            Switch(
                checked = setting.isEnabled,
                onCheckedChange = onToggleChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        // Expanded Configuration Options
        AnimatedVisibility(visible = setting.isEnabled) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Jenis Pengingat Saat Waktu Shalat:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.extendedColors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Reminder Type Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReminderType.entries.forEach { type ->
                        val isSelected = setting.reminderType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onTypeChanged(type) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.extendedColors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smart Reminder (Pre-prayer) Selector
                Text(
                    text = "Smart Reminder (Sebelum Shalat):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.extendedColors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                var expandedDropdown by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { expandedDropdown = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = setting.preReminderInterval.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.extendedColors.textPrimary
                    )

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        PrePrayerReminderInterval.entries.forEach { interval ->
                            DropdownMenuItem(
                                text = { Text(interval.displayName) },
                                onClick = {
                                    onPreIntervalChanged(interval)
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Test Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(onClick = onTestClicked)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = "Uji Suara",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Uji Suara / Notifikasi Sekarang",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

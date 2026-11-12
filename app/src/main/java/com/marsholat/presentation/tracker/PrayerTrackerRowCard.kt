package com.marsholat.presentation.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marsholat.core.model.PrayerStatus
import com.marsholat.core.model.PrayerType
import com.marsholat.core.util.DateTimeUtils
import com.marsholat.presentation.components.MarSholatCard
import com.marsholat.presentation.components.getPrayerIcon
import com.marsholat.ui.theme.StatusMissed
import com.marsholat.ui.theme.StatusSuccess
import com.marsholat.ui.theme.extendedColors
import java.time.LocalTime

@Composable
fun PrayerTrackerRowCard(
    prayer: PrayerType,
    time: LocalTime?,
    status: PrayerStatus,
    onStatusChanged: (PrayerStatus) -> Unit,
    is24Hour: Boolean = true,
    modifier: Modifier = Modifier
) {
    MarSholatCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (status) {
                                PrayerStatus.COMPLETED -> StatusSuccess.copy(alpha = 0.15f)
                                PrayerStatus.MISSED -> StatusMissed.copy(alpha = 0.15f)
                                PrayerStatus.NOT_COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPrayerIcon(prayer),
                        contentDescription = prayer.displayName,
                        tint = when (status) {
                            PrayerStatus.COMPLETED -> StatusSuccess
                            PrayerStatus.MISSED -> StatusMissed
                            PrayerStatus.NOT_COMPLETED -> MaterialTheme.extendedColors.textSecondary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = prayer.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.extendedColors.textPrimary
                    )
                    time?.let {
                        Text(
                            text = DateTimeUtils.formatTime(it, is24Hour),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.extendedColors.textSecondary
                        )
                    }
                }
            }

            // 3 Interactive Status Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPillButton(
                    label = "Sudah",
                    isSelected = status == PrayerStatus.COMPLETED,
                    activeColor = StatusSuccess,
                    onClick = { onStatusChanged(PrayerStatus.COMPLETED) }
                )

                StatusPillButton(
                    label = "Belum",
                    isSelected = status == PrayerStatus.NOT_COMPLETED,
                    activeColor = MaterialTheme.extendedColors.textSecondary,
                    onClick = { onStatusChanged(PrayerStatus.NOT_COMPLETED) }
                )

                StatusPillButton(
                    label = "Terlewat",
                    isSelected = status == PrayerStatus.MISSED,
                    activeColor = StatusMissed,
                    onClick = { onStatusChanged(PrayerStatus.MISSED) }
                )
            }
        }
    }
}

@Composable
private fun StatusPillButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val bg = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val textCol = if (isSelected) Color.White else MaterialTheme.extendedColors.textSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textCol,
            fontSize = 11.sp
        )
    }
}

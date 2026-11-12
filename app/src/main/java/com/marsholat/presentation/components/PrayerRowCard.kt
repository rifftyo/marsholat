package com.marsholat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness3
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marsholat.core.model.PrayerType
import com.marsholat.core.util.DateTimeUtils
import com.marsholat.ui.theme.extendedColors
import java.time.LocalTime

fun getPrayerIcon(prayer: PrayerType): ImageVector {
    return when (prayer) {
        PrayerType.SUBUH -> Icons.Outlined.WbTwilight
        PrayerType.DZUHUR -> Icons.Outlined.WbSunny
        PrayerType.ASHAR -> Icons.Outlined.Brightness5
        PrayerType.MAGHRIB -> Icons.Outlined.Brightness6
        PrayerType.ISYA -> Icons.Outlined.Brightness3
    }
}

@Composable
fun PrayerRowCard(
    prayer: PrayerType,
    time: LocalTime,
    isNext: Boolean,
    is24Hour: Boolean = true,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isNext) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.extendedColors.cardBorder
    }

    val backgroundColor = if (isNext) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    MarSholatCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        borderWidth = if (isNext) 1.5.dp else 1.dp,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNext) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPrayerIcon(prayer),
                        contentDescription = prayer.displayName,
                        tint = if (isNext) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.extendedColors.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.extendedColors.textPrimary
                        )

                        if (isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Berikutnya",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = DateTimeUtils.formatTime(time, is24Hour),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                color = if (isNext) MaterialTheme.colorScheme.primary
                else MaterialTheme.extendedColors.textPrimary
            )
        }
    }
}

package com.marsholat.presentation.settings

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marsholat.core.model.AsrJuristicMethod
import com.marsholat.core.model.CalculationMethod
import com.marsholat.core.model.PrayerType
import com.marsholat.core.model.ThemeMode
import com.marsholat.core.util.CityDatabase
import com.marsholat.presentation.components.MarSholatCard
import com.marsholat.ui.theme.extendedColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsState()
    val isRefreshingLocation by viewModel.isRefreshingLocation.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    var showCityDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

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
                text = "Pengaturan",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.extendedColors.textPrimary
            )
            Text(
                text = "Sesuaikan perhitungan jadwal shalat dan tampilan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.extendedColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: LOKASI
        SettingsSectionHeader(title = "Lokasi Pengguna", icon = Icons.Outlined.LocationOn)

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userSettings.location.cityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.extendedColors.textPrimary
                    )
                    Text(
                        text = "${userSettings.location.region} • ${userSettings.location.timeZoneId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.extendedColors.textSecondary
                    )
                    Text(
                        text = "Lat: %.4f, Lon: %.4f".format(userSettings.location.latitude, userSettings.location.longitude),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }

                if (isRefreshingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    IconButton(onClick = { viewModel.refreshLocationGps() }) {
                        Icon(
                            imageVector = Icons.Outlined.MyLocation,
                            contentDescription = "Deteksi GPS",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Choose city button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showCityDialog = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationCity,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pilih Kota Indonesia (Offline)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.extendedColors.textPrimary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.extendedColors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: PENGINGAT & NOTIFIKASI
        SettingsSectionHeader(title = "Pengingat & Notifikasi", icon = Icons.Outlined.Notifications)

        MarSholatCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToNotificationSettings() },
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
                        text = "Pengaturan Adzan & Notifikasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.extendedColors.textPrimary
                    )
                    Text(
                        text = "Adzan, alarm, atau notifikasi biasa & smart reminder",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.extendedColors.textSecondary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: METODE PERHITUNGAN
        SettingsSectionHeader(title = "Perhitungan Jadwal Shalat", icon = Icons.Outlined.Calculate)

        MarSholatCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentPadding = 16.dp
        ) {
            // Calculation Method Picker
            var expandedCalc by remember { mutableStateOf(false) }

            Text(
                text = "Metode Perhitungan:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.extendedColors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { expandedCalc = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = userSettings.calculationMethod.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.extendedColors.textPrimary
                )
                DropdownMenu(
                    expanded = expandedCalc,
                    onDismissRequest = { expandedCalc = false }
                ) {
                    CalculationMethod.entries.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method.displayName) },
                            onClick = {
                                viewModel.setCalculationMethod(method)
                                expandedCalc = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Asr Juristic Method Picker
            var expandedAsr by remember { mutableStateOf(false) }

            Text(
                text = "Metode Waktu Ashar:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.extendedColors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { expandedAsr = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = userSettings.asrJuristicMethod.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.extendedColors.textPrimary
                )
                DropdownMenu(
                    expanded = expandedAsr,
                    onDismissRequest = { expandedAsr = false }
                ) {
                    AsrJuristicMethod.entries.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method.displayName) },
                            onClick = {
                                viewModel.setAsrJuristicMethod(method)
                                expandedAsr = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manual Time Adjustments
            Text(
                text = "Koreksi Waktu Shalat Manual (Menit):",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.extendedColors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            PrayerType.entries.forEach { prayer ->
                val offset = userSettings.adjustments.getOffset(prayer)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prayer.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.extendedColors.textPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.updateAdjustment(prayer, -1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Outlined.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = if (offset > 0) "+$offset m" else "$offset m",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(50.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        IconButton(
                            onClick = { viewModel.updateAdjustment(prayer, 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: TAMPILAN
        SettingsSectionHeader(title = "Tampilan & Format", icon = Icons.Outlined.Palette)

        MarSholatCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentPadding = 16.dp
        ) {
            // Theme Mode Selector
            Text(
                text = "Tema Aplikasi:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.extendedColors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = userSettings.themeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { viewModel.setThemeMode(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (mode) {
                                ThemeMode.SYSTEM -> "Sistem"
                                ThemeMode.LIGHT -> "Terang"
                                ThemeMode.DARK -> "Gelap"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.extendedColors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 24 Hour Format Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Format Waktu 24 Jam",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.extendedColors.textPrimary
                    )
                    Text(
                        text = if (userSettings.is24HourFormat) "Contoh: 15:30" else "Contoh: 03:30 PM",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.extendedColors.textSecondary
                    )
                }

                Switch(
                    checked = userSettings.is24HourFormat,
                    onCheckedChange = { viewModel.setIs24HourFormat(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 5: TENTANG APLIKASI
        SettingsSectionHeader(title = "Informasi", icon = Icons.Outlined.Info)

        MarSholatCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAboutDialog = true },
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
                        text = "Tentang MarSholat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.extendedColors.textPrimary
                    )
                    Text(
                        text = "Versi 1.0 • 100% Offline Muslim Prayer Companion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.extendedColors.textSecondary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // City Picker Dialog
    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text(
                    text = "Pilih Kota Offline",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                var search by remember { mutableStateOf("") }
                val filteredCities = CityDatabase.INDONESIAN_CITIES.filter {
                    it.cityName.contains(search, ignoreCase = true) || it.region.contains(search, ignoreCase = true)
                }

                Column(modifier = Modifier.height(350.dp)) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Cari kota...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn {
                        items(filteredCities) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setLocation(city)
                                        showCityDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = city.cityName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.extendedColors.textPrimary
                                    )
                                    Text(
                                        text = city.region,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.extendedColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "MarSholat V1.0",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "MarSholat adalah aplikasi pendamping ibadah Muslim harian yang bekerja 100% offline tanpa internet, akun, iklan, maupun backend.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.extendedColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Perhitungan astronomi lokal akurat (Kemenag RI)\n• Pengingat Adzan & Alarm otomatis\n• Smart Reminder persiapan shalat\n• Daily Prayer Tracker & Statistik ibadah\n• Kalender Hijriah & jadwal bulanan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.extendedColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Simple. Offline. Reliable. Beautiful.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

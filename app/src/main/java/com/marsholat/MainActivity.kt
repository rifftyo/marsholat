package com.marsholat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marsholat.core.model.ThemeMode
import com.marsholat.core.notification.AlarmScheduler
import com.marsholat.presentation.home.HomeScreen
import com.marsholat.presentation.home.HomeViewModel
import com.marsholat.presentation.navigation.MainNavigation
import com.marsholat.presentation.notifications.NotificationSettingsScreen
import com.marsholat.presentation.notifications.NotificationSettingsViewModel
import com.marsholat.presentation.schedule.ScheduleScreen
import com.marsholat.presentation.schedule.ScheduleViewModel
import com.marsholat.presentation.settings.SettingsScreen
import com.marsholat.presentation.settings.SettingsViewModel
import com.marsholat.presentation.theme.ThemeViewModel
import com.marsholat.presentation.tracker.TrackerScreen
import com.marsholat.presentation.tracker.TrackerViewModel
import com.marsholat.ui.theme.MarsholatTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MarSholatApplication

        lifecycleScope.launch {
            AlarmScheduler.scheduleAllAlarms(applicationContext)
        }

        setContent {
            val themeViewModel: ThemeViewModel = viewModel {
                ThemeViewModel(app.settingsRepository)
            }
            val themeMode by themeViewModel.themeMode.collectAsState()

            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MarsholatTheme(darkTheme = isDark) {
                val homeViewModel: HomeViewModel = viewModel {
                    HomeViewModel(
                        settingsRepository = app.settingsRepository,
                        prayerScheduleRepository = app.prayerScheduleRepository,
                        trackerRepository = app.prayerTrackerRepository,
                        context = applicationContext
                    )
                }

                val scheduleViewModel: ScheduleViewModel = viewModel {
                    ScheduleViewModel(
                        settingsRepository = app.settingsRepository,
                        prayerScheduleRepository = app.prayerScheduleRepository
                    )
                }

                val trackerViewModel: TrackerViewModel = viewModel {
                    TrackerViewModel(
                        trackerRepository = app.prayerTrackerRepository,
                        scheduleRepository = app.prayerScheduleRepository,
                        settingsRepository = app.settingsRepository
                    )
                }

                val settingsViewModel: SettingsViewModel = viewModel {
                    SettingsViewModel(
                        settingsRepository = app.settingsRepository,
                        context = applicationContext
                    )
                }

                val notificationSettingsViewModel: NotificationSettingsViewModel = viewModel {
                    NotificationSettingsViewModel(
                        settingsRepository = app.settingsRepository,
                        context = applicationContext
                    )
                }

                MainNavigation(
                    homeScreenContent = { toSettings, toSchedule, toTracker ->
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToSettings = toSettings,
                            onNavigateToSchedule = toSchedule,
                            onNavigateToTracker = toTracker
                        )
                    },
                    scheduleScreenContent = {
                        ScheduleScreen(viewModel = scheduleViewModel)
                    },
                    trackerScreenContent = {
                        TrackerScreen(viewModel = trackerViewModel)
                    },
                    settingsScreenContent = { toNotifSettings ->
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateToNotificationSettings = toNotifSettings
                        )
                    },
                    notificationSettingsContent = { onBack ->
                        NotificationSettingsScreen(
                            viewModel = notificationSettingsViewModel,
                            onNavigateBack = onBack
                        )
                    }
                )
            }
        }
    }
}

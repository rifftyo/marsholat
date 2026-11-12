package com.marsholat.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Schedule : Screen("schedule")
    data object Tracker : Screen("tracker")
    data object Settings : Screen("settings")
    data object NotificationSettings : Screen("notification_settings")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Outlined.Home)
    data object Schedule : BottomNavItem(Screen.Schedule.route, "Jadwal", Icons.Outlined.Schedule)
    data object Tracker : BottomNavItem(Screen.Tracker.route, "Ibadah", Icons.Outlined.CheckCircle)
    data object Settings : BottomNavItem(Screen.Settings.route, "Settings", Icons.Outlined.Settings)

    companion object {
        val items = listOf(Home, Schedule, Tracker, Settings)
    }
}

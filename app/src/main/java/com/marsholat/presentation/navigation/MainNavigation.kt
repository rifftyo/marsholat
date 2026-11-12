package com.marsholat.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marsholat.presentation.components.MarSholatBottomNavigationBar

@Composable
fun MainNavigation(
    homeScreenContent: @Composable (navigateToSettings: () -> Unit, navigateToSchedule: () -> Unit, navigateToTracker: () -> Unit) -> Unit,
    scheduleScreenContent: @Composable () -> Unit,
    trackerScreenContent: @Composable () -> Unit,
    settingsScreenContent: @Composable (navigateToNotificationSettings: () -> Unit) -> Unit,
    notificationSettingsContent: @Composable (onNavigateBack: () -> Unit) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = currentRoute in BottomNavItem.items.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) {
                MarSholatBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                homeScreenContent(
                    { navController.navigate(Screen.Settings.route) },
                    { navController.navigate(Screen.Schedule.route) },
                    { navController.navigate(Screen.Tracker.route) }
                )
            }
            composable(Screen.Schedule.route) {
                scheduleScreenContent()
            }
            composable(Screen.Tracker.route) {
                trackerScreenContent()
            }
            composable(Screen.Settings.route) {
                settingsScreenContent {
                    navController.navigate(Screen.NotificationSettings.route)
                }
            }
            composable(Screen.NotificationSettings.route) {
                notificationSettingsContent {
                    navController.popBackStack()
                }
            }
        }
    }
}

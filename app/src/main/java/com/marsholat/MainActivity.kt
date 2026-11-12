package com.marsholat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marsholat.core.model.ThemeMode
import com.marsholat.presentation.navigation.MainNavigation
import com.marsholat.presentation.theme.ThemeViewModel
import com.marsholat.ui.theme.MarsholatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MarSholatApplication

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
                MainNavigation(
                    homeScreenContent = { _, _, _ -> PlaceholderScreen("MarSholat Home") },
                    scheduleScreenContent = { PlaceholderScreen("Jadwal Shalat") },
                    trackerScreenContent = { PlaceholderScreen("Ibadah Tracker") },
                    settingsScreenContent = { _ -> PlaceholderScreen("Settings") },
                    notificationSettingsContent = { _ -> PlaceholderScreen("Pengaturan Notifikasi") }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}

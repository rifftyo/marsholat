package com.marsholat.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsholat.core.model.ThemeMode
import com.marsholat.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }
}

package com.soundwave.app.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundwave.app.domain.model.*
import com.soundwave.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrefsRepo: UserPreferencesRepository
) : ViewModel() {

    val userPreferences = userPrefsRepo.userPreferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateStreamingQuality(q: AudioQuality) = viewModelScope.launch { userPrefsRepo.updateStreamingQuality(q) }
    fun updateThemeMode(m: ThemeMode) = viewModelScope.launch { userPrefsRepo.updateThemeMode(m) }
    fun updateDynamicColor(v: Boolean) = viewModelScope.launch { userPrefsRepo.updateDynamicColor(v) }
    fun updateEqualizerEnabled(v: Boolean) = viewModelScope.launch { userPrefsRepo.updateEqualizerEnabled(v) }
    fun updateEqualizerBands(bands: List<EqualizerBand>) = viewModelScope.launch { userPrefsRepo.updateEqualizerBands(bands) }
    fun updateEqualizerPreset(preset: EqualizerPreset) = viewModelScope.launch {
        userPrefsRepo.updateEqualizerBands(preset.bands)
    }
    fun updateNormalization(v: Boolean) {}
    fun updateSkipSilence(v: Boolean) = viewModelScope.launch { userPrefsRepo.updateSkipSilence(v) }
    fun updateDownloadOnWifiOnly(v: Boolean) = viewModelScope.launch { userPrefsRepo.updateDownloadOnWifiOnly(v) }
    fun updateLyricsEnabled(v: Boolean) = viewModelScope.launch { userPrefsRepo.updateLyricsEnabled(v) }
    fun updateVisualizerEnabled(v: Boolean) = viewModelScope.launch { userPrefsRepo.updateVisualizerEnabled(v) }
    fun updateShowExplicit(v: Boolean) = viewModelScope.launch { userPrefsRepo.updateShowExplicit(v) }
}

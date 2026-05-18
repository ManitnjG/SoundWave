package com.soundwave.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.soundwave.app.core.audio.MusicPlayerController
import com.soundwave.app.domain.model.*
import com.soundwave.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isReady: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val dynamicColor: Boolean = true,
    val currentTrack: Track? = null,
    val playbackState: PlaybackState = PlaybackState()
)

@UnstableApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val playerController: MusicPlayerController,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        playerController.connect()
        observePreferences()
        observePlayback()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences
                .filterNotNull()
                .collect { prefs ->
                    _uiState.update {
                        it.copy(
                            isReady = true,
                            themeMode = prefs.themeMode,
                            dynamicColor = prefs.dynamicColor
                        )
                    }
                }
        }
        // Mark ready after a timeout even if prefs not loaded
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(isReady = true) }
        }
    }

    private fun observePlayback() {
        viewModelScope.launch {
            playerController.currentTrack.collect { track ->
                _uiState.update { it.copy(currentTrack = track) }
            }
        }
        viewModelScope.launch {
            playerController.playbackState.collect { state ->
                _uiState.update { it.copy(playbackState = state) }
            }
        }
    }

    fun togglePlayPause() = playerController.togglePlayPause()
    fun skipToNext() = playerController.skipToNext()

    override fun onCleared() {
        super.onCleared()
        playerController.disconnect()
    }
}

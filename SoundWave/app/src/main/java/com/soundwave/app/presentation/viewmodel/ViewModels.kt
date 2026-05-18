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

// ─────────────────────────────────────────────────────────────────────────────

package com.soundwave.app.presentation.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.soundwave.app.core.audio.MusicPlayerController
import com.soundwave.app.domain.model.*
import com.soundwave.app.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistUiState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = false
)

@UnstableApi
@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playerController: MusicPlayerController
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    fun loadPlaylist(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            musicRepository.getPlaylistDetails(id).onSuccess { playlist ->
                _uiState.update { it.copy(playlist = playlist, tracks = playlist.tracks, isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun playAll() {
        val tracks = _uiState.value.tracks
        if (tracks.isNotEmpty()) playerController.playTrack(tracks[0], tracks, 0)
    }

    fun shuffle() {
        val tracks = _uiState.value.tracks.shuffled()
        if (tracks.isNotEmpty()) { playerController.playTrack(tracks[0], tracks, 0) }
    }

    fun playFromIndex(index: Int) {
        val tracks = _uiState.value.tracks
        playerController.playTrack(tracks[index], tracks, index)
    }
}

// ─────────────────────────────────────────────────────────────────────────────

package com.soundwave.app.presentation.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.soundwave.app.core.audio.MusicPlayerController
import com.soundwave.app.data.local.dao.TrackDao
import com.soundwave.app.data.repository.toTrack
import com.soundwave.app.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DownloadsUiState(val downloads: List<Track> = emptyList())

@UnstableApi
@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val trackDao: TrackDao,
    private val playerController: MusicPlayerController
) : ViewModel() {
    val uiState = trackDao.getDownloadedTracks()
        .map { DownloadsUiState(it.map { e -> e.toTrack() }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DownloadsUiState())

    fun play(track: Track, queue: List<Track>) {
        playerController.playTrack(track, queue, queue.indexOf(track).coerceAtLeast(0))
    }
}

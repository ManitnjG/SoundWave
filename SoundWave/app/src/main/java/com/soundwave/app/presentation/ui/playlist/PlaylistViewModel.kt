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

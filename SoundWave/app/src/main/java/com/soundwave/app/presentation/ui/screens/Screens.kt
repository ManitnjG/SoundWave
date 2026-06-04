package com.soundwave.app.presentation.ui.library

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

data class LibraryUiState(
    val playlists: List<Playlist> = emptyList(),
    val favoritePlaylists: List<Playlist> = emptyList(),
    val favoriteTracks: List<Track> = emptyList(),
    val favoriteAlbums: List<Album> = emptyList(),
    val followedArtists: List<Artist> = emptyList()
)

@UnstableApi
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playerController: MusicPlayerController
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                musicRepository.getUserPlaylists(),
                musicRepository.getFavoritePlaylists(),
                musicRepository.getFavoriteTracks()
            ) { playlists, favPl, favTracks ->
                LibraryUiState(playlists = playlists, favoritePlaylists = favPl, favoriteTracks = favTracks)
            }.collect { _uiState.value = it }
        }
    }

    fun playTrack(track: Track, queue: List<Track>) {
        playerController.playTrack(track, queue, queue.indexOf(track).coerceAtLeast(0))
    }

    fun createNewPlaylist() {
        viewModelScope.launch { musicRepository.createPlaylist("New Playlist ${System.currentTimeMillis() % 1000}", "") }
    }
}

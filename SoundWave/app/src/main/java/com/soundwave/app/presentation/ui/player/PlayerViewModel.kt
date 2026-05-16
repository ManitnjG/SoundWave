package com.soundwave.app.presentation.ui.player

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

data class PlayerUiState(
    val currentTrack: Track? = null,
    val playbackState: PlaybackState = PlaybackState(),
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isFavorite: Boolean = false,
    val lyrics: Lyrics? = null,
    val lyricsLoading: Boolean = false,
    val currentLyricLine: Int = -1,
    val currentPosition: Long = 0L
)

@UnstableApi
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: MusicPlayerController,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            combine(
                playerController.currentTrack,
                playerController.playbackState,
                playerController.queue,
                playerController.queueIndex,
                playerController.shuffleEnabled,
                playerController.repeatMode
            ) { values ->
                val track = values[0] as Track?
                val state = values[1] as PlaybackState
                val queue = values[2] as List<Track>
                val index = values[3] as Int
                val shuffle = values[4] as Boolean
                val repeat = values[5] as RepeatMode
                PlayerUiState(
                    currentTrack = track,
                    playbackState = state,
                    queue = queue,
                    queueIndex = index,
                    shuffleEnabled = shuffle,
                    repeatMode = repeat,
                    isFavorite = track?.isFavorite ?: false
                )
            }.collect { state ->
                _uiState.value = state
                // Load lyrics when track changes
                state.currentTrack?.let { track ->
                    if (_uiState.value.lyrics?.trackId != track.id && track.hasLyrics) {
                        loadLyrics(track)
                    }
                }
            }
        }

        // Track position updates
        viewModelScope.launch {
            playerController.currentPosition.collect { pos ->
                _uiState.update { it.copy(currentPosition = pos) }
                updateCurrentLyricLine(pos)
            }
        }
    }

    private fun loadLyrics(track: Track) {
        viewModelScope.launch {
            _uiState.update { it.copy(lyricsLoading = true) }
            musicRepository.getLyrics(track.id, track.title, track.artist, (track.durationMs / 1000).toInt())
                .onSuccess { lyrics ->
                    _uiState.update { it.copy(lyrics = lyrics, lyricsLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(lyricsLoading = false) }
                }
        }
    }

    private fun updateCurrentLyricLine(positionMs: Long) {
        val lyrics = _uiState.value.lyrics ?: return
        if (!lyrics.isSynced) return
        val index = lyrics.lines.indexOfLast { it.timestampMs <= positionMs }
        if (index != _uiState.value.currentLyricLine) {
            _uiState.update { it.copy(currentLyricLine = index) }
        }
    }

    fun togglePlayPause() = playerController.togglePlayPause()
    fun skipToNext() = playerController.skipToNext()
    fun skipToPrevious() = playerController.skipToPrevious()
    fun toggleShuffle() = playerController.toggleShuffle()
    fun toggleRepeat() = playerController.toggleRepeat()
    fun seekTo(fraction: Float) = playerController.seekToFraction(fraction)
    fun skipToQueueItem(index: Int) = playerController.skipToQueueItem(index)
    fun removeFromQueue(index: Int) = playerController.removeFromQueue(index)

    fun toggleFavorite() {
        val track = _uiState.value.currentTrack ?: return
        val newFav = !track.isFavorite
        _uiState.update { it.copy(isFavorite = newFav) }
        viewModelScope.launch {
            musicRepository.toggleFavoriteTrack(track.id, newFav)
        }
    }
}

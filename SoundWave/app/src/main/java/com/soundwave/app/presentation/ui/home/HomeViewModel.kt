package com.soundwave.app.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.soundwave.app.core.audio.MusicPlayerController
import com.soundwave.app.domain.model.*
import com.soundwave.app.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val greeting: String = "Good evening",
    val featuredBanner: List<Track> = emptyList(),
    val trending: List<Track> = emptyList(),
    val recentlyPlayed: List<Track> = emptyList(),
    val recommended: List<Track> = emptyList(),
    val newReleases: List<Album> = emptyList(),
    val featuredPlaylists: List<Playlist> = emptyList(),
    val moodPlaylists: List<MoodCategory> = emptyList(),
    val error: String? = null
)

@UnstableApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playerController: MusicPlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(greeting = getGreeting()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeFeed()
        observeRecentlyPlayed()
    }

    private fun loadHomeFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            musicRepository.getHomeFeed()
                .onSuccess { feed ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            featuredBanner = feed.featuredBanner,
                            trending = feed.trending,
                            newReleases = feed.newReleases,
                            featuredPlaylists = feed.featuredPlaylists,
                            moodPlaylists = feed.moodPlaylists
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    private fun observeRecentlyPlayed() {
        viewModelScope.launch {
            musicRepository.getRecentlyPlayed().collect { tracks ->
                _uiState.update { it.copy(recentlyPlayed = tracks) }
            }
        }
    }

    fun playTrack(track: Track, queue: List<Track>) {
        val startIndex = queue.indexOf(track).coerceAtLeast(0)
        playerController.playTrack(track, queue, startIndex)
        viewModelScope.launch {
            musicRepository.addToRecentlyPlayed(track.id)
        }
    }

    fun refresh() = loadHomeFeed()

    private fun getGreeting(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }
}

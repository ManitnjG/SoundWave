package com.soundwave.app.presentation.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.soundwave.app.core.audio.MusicPlayerController
import com.soundwave.app.domain.model.*
import com.soundwave.app.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val searchResult: SearchResult = SearchResult(),
    val searchHistory: List<String> = emptyList(),
    val error: String? = null
)

@UnstableApi
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playerController: MusicPlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            musicRepository.getSearchHistory().collect { history ->
                _uiState.update { it.copy(searchHistory = history) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) return
        searchJob = viewModelScope.launch {
            delay(350) // Debounce
            _uiState.update { it.copy(isLoading = true) }
            musicRepository.search(query)
                .onSuccess { result -> _uiState.update { it.copy(isLoading = false, searchResult = result) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clearQuery() { _uiState.update { it.copy(query = "", searchResult = SearchResult()) }; searchJob?.cancel() }
    fun clearHistory() { viewModelScope.launch { musicRepository.clearSearchHistory() } }

    fun playTrack(track: Track, queue: List<Track>) {
        val idx = queue.indexOf(track).coerceAtLeast(0)
        playerController.playTrack(track, queue, idx)
    }
}

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

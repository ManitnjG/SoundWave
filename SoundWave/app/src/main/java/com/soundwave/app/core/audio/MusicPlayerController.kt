package com.soundwave.app.core.audio

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.soundwave.app.domain.model.*
import com.soundwave.app.data.local.dao.TrackDao
import com.soundwave.app.data.repository.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class MusicPlayerController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val streamUrlResolver: StreamUrlResolver,
    private val trackDao: TrackDao
) {
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var _controller: MediaController? = null

    // Public state flows
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Position update flow
    val currentPosition: Flow<Long> = flow {
        while (true) {
            _controller?.let { emit(it.currentPosition) }
            delay(250) // Update 4x/sec for smooth progress
        }
    }.conflate()

    fun connect() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken)
            .buildAsync()
        controllerFuture?.addListener({
            try {
                _controller = controllerFuture?.get()
                _controller?.addListener(PlayerListener())
                _isConnected.value = true
                syncStateFromController()
                Timber.d("MediaController connected")
            } catch (e: Exception) {
                Timber.e(e, "Failed to connect MediaController")
            }
        }, MoreExecutors.directExecutor())
    }

    fun disconnect() {
        _controller?.removeListener(PlayerListener())
        MediaController.releaseFuture(controllerFuture ?: return)
        _controller = null
        _isConnected.value = false
    }

    // ─── Playback Controls ────────────────────────────────────────────────────

    fun play() = _controller?.play()
    fun pause() = _controller?.pause()
    fun togglePlayPause() {
        _controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun seekTo(positionMs: Long) = _controller?.seekTo(positionMs)
    fun seekToFraction(fraction: Float) {
        val duration = _controller?.duration ?: return
        if (duration > 0) _controller?.seekTo((duration * fraction).toLong())
    }

    fun skipToNext() = _controller?.seekToNextMediaItem()
    fun skipToPrevious() {
        val controller = _controller ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
        } else {
            controller.seekToPreviousMediaItem()
        }
    }

    fun toggleShuffle() {
        _controller?.let {
            val newShuffle = !it.shuffleModeEnabled
            it.shuffleModeEnabled = newShuffle
            _shuffleEnabled.value = newShuffle
        }
    }

    fun toggleRepeat() {
        _controller?.let { controller ->
            val next = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            controller.repeatMode = next
            _repeatMode.value = when (next) {
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                else -> RepeatMode.OFF
            }
        }
    }

    fun setVolume(volume: Float) = _controller?.setVolume(volume.coerceIn(0f, 1f))
    fun setPlaybackSpeed(speed: Float) {
        _controller?.setPlaybackParameters(PlaybackParameters(speed.coerceIn(0.5f, 2f)))
    }

    // ─── Queue Management ─────────────────────────────────────────────────────

    fun playTrack(track: Track, queue: List<Track> = listOf(track), startIndex: Int = 0) {
        controllerScope.launch {
            // Cache track to DB
            trackDao.insertTrack(track.toEntity())

            // Resolve all tracks in queue with stream URLs
            val mediaItems = queue.map { t ->
                val url = streamUrlResolver.resolve(t.id) ?: t.previewUrl ?: return@map null
                t.copy(streamUrl = url).toMediaItem()
            }.filterNotNull()

            _controller?.let { controller ->
                controller.clearMediaItems()
                controller.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
                controller.prepare()
                controller.play()
            }

            _queue.value = queue
            _queueIndex.value = startIndex
            _currentTrack.value = queue.getOrNull(startIndex)
        }
    }

    fun playNext(track: Track) {
        controllerScope.launch {
            val url = streamUrlResolver.resolve(track.id) ?: return@launch
            val mediaItem = track.copy(streamUrl = url).toMediaItem()
            val insertIndex = (_controller?.currentMediaItemIndex ?: 0) + 1
            _controller?.addMediaItem(insertIndex, mediaItem)
            val newQueue = _queue.value.toMutableList()
            newQueue.add(insertIndex.coerceAtMost(newQueue.size), track)
            _queue.value = newQueue
        }
    }

    fun addToQueue(track: Track) {
        controllerScope.launch {
            val url = streamUrlResolver.resolve(track.id) ?: return@launch
            val mediaItem = track.copy(streamUrl = url).toMediaItem()
            _controller?.addMediaItem(mediaItem)
            _queue.value = _queue.value + track
        }
    }

    fun removeFromQueue(index: Int) {
        _controller?.removeMediaItem(index)
        val newQueue = _queue.value.toMutableList()
        if (index in newQueue.indices) newQueue.removeAt(index)
        _queue.value = newQueue
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        _controller?.moveMediaItem(fromIndex, toIndex)
        val newQueue = _queue.value.toMutableList()
        if (fromIndex in newQueue.indices && toIndex in newQueue.indices) {
            val item = newQueue.removeAt(fromIndex)
            newQueue.add(toIndex, item)
            _queue.value = newQueue
        }
    }

    fun skipToQueueItem(index: Int) {
        _controller?.seekToDefaultPosition(index)
        _queueIndex.value = index
    }

    fun clearQueue() {
        _controller?.clearMediaItems()
        _queue.value = emptyList()
        _currentTrack.value = null
    }

    // ─── State Sync ───────────────────────────────────────────────────────────

    private fun syncStateFromController() {
        _controller?.let { controller ->
            _shuffleEnabled.value = controller.shuffleModeEnabled
            _repeatMode.value = when (controller.repeatMode) {
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                else -> RepeatMode.OFF
            }
            _playbackState.value = PlaybackState(
                isPlaying = controller.isPlaying,
                isBuffering = controller.playbackState == Player.STATE_BUFFERING,
                currentPositionMs = controller.currentPosition,
                bufferedPositionMs = controller.bufferedPosition,
                durationMs = controller.duration.takeIf { it != C.TIME_UNSET } ?: 0L
            )
        }
    }

    private inner class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(state: Int) {
            _playbackState.update {
                it.copy(
                    isBuffering = state == Player.STATE_BUFFERING,
                    isLoading = state == Player.STATE_BUFFERING
                )
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = _controller?.currentMediaItemIndex ?: 0
            _queueIndex.value = index
            _currentTrack.value = _queue.value.getOrNull(index)
            Timber.d("Track transition: ${mediaItem?.mediaId}")
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackState.update { it.copy(error = error.message) }
            Timber.e(error, "Playback error")
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _shuffleEnabled.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = when (repeatMode) {
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                else -> RepeatMode.OFF
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            _playbackState.update {
                it.copy(currentPositionMs = newPosition.positionMs)
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                Player.EVENT_IS_PLAYING_CHANGED,
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED
            )) {
                _playbackState.update { current ->
                    current.copy(
                        isPlaying = player.isPlaying,
                        isBuffering = player.playbackState == Player.STATE_BUFFERING,
                        durationMs = player.duration.takeIf { it != C.TIME_UNSET } ?: current.durationMs,
                        bufferedPositionMs = player.bufferedPosition
                    )
                }
            }
        }
    }
}

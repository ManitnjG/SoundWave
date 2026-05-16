package com.soundwave.app.core.audio

import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.os.Bundle
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.session.*
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.soundwave.app.core.cache.AudioCacheManager
import com.soundwave.app.data.local.dao.TrackDao
import com.soundwave.app.domain.model.EqualizerBand
import com.soundwave.app.domain.repository.UserPreferencesRepository
import com.soundwave.app.presentation.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MusicPlaybackService : MediaSessionService() {

    @Inject lateinit var audioCacheManager: AudioCacheManager
    @Inject lateinit var trackDao: TrackDao
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var streamUrlResolver: StreamUrlResolver
    @Inject lateinit var crossfadeManager: CrossfadeManager

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Audio Effects
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeMediaSession()
        observePreferences()
        Timber.d("MusicPlaybackService created")
    }

    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters()
                .setForceHighestSupportedBitrate(true)
                .setAllowAudioMixedMimeTypeAdaptiveness(true)
                .build()
            )
        }

        val bandwidthMeter = DefaultBandwidthMeter.Builder(this)
            .setResetOnNetworkTypeChange(true)
            .build()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 15_000,
                /* maxBufferMs = */ 50_000,
                /* bufferForPlaybackMs = */ 2_500,
                /* bufferForPlaybackAfterRebufferMs = */ 5_000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val mediaSourceFactory = audioCacheManager.buildCachingMediaSourceFactory(this)

        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(bandwidthMeter)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .build()
            .also { exo ->
                exo.addListener(PlayerListener())
                exo.playWhenReady = true
            }

        // Initialize audio effects after player creation
        initializeAudioEffects()
    }

    private fun initializeAudioEffects() {
        val audioSessionId = player.audioSessionId
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET) return

        runCatching {
            equalizer = Equalizer(0, audioSessionId).apply { enabled = false }
            bassBoost = BassBoost(0, audioSessionId).apply { enabled = false }
            virtualizer = Virtualizer(0, audioSessionId).apply { enabled = false }
        }.onFailure { Timber.e(it, "Failed to initialize audio effects") }
    }

    private fun initializeMediaSession() {
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(MediaSessionCallback())
            .build()
    }

    private fun observePreferences() {
        serviceScope.launch {
            userPreferencesRepository.userPreferences.collect { prefs ->
                // Apply equalizer settings
                prefs?.let { applyEqualizerSettings(it.equalizerEnabled, it.equalizerBands) }
                // Apply bass boost
                prefs?.let { applyBassBoost(it.bassBoostEnabled, it.bassBoostStrength) }
                // Apply virtualizer
                prefs?.let { applyVirtualizer(it.virtualizerEnabled, it.virtualizerStrength) }
                // Apply skip silence
                prefs?.let { player.skipSilenceEnabled = it.skipSilence }
                // Apply crossfade duration
                crossfadeManager.setCrossfadeDuration(prefs?.crossfadeDurationMs ?: 3000)
            }
        }
    }

    fun applyEqualizerSettings(enabled: Boolean, bands: List<EqualizerBand>) {
        equalizer?.let { eq ->
            eq.enabled = enabled
            if (enabled && bands.isNotEmpty()) {
                bands.forEachIndexed { index, band ->
                    if (index < eq.numberOfBands) {
                        val millibelGain = (band.gainDb * 100).toInt().toShort()
                        eq.setBandLevel(index.toShort(), millibelGain)
                    }
                }
            }
        }
    }

    fun applyBassBoost(enabled: Boolean, strength: Short) {
        bassBoost?.let {
            it.enabled = enabled
            if (enabled) it.setStrength(strength.coerceIn(0, 1000))
        }
    }

    fun applyVirtualizer(enabled: Boolean, strength: Short) {
        virtualizer?.let {
            if (it.strengthSupported) {
                it.enabled = enabled
                if (enabled) it.setStrength(strength.coerceIn(0, 1000))
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession.release()
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        player.release()
        audioCacheManager.release()
        super.onDestroy()
        Timber.d("MusicPlaybackService destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    // ─── Inner Classes ────────────────────────────────────────────────────────

    private inner class PlayerListener : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let { item ->
                serviceScope.launch {
                    trackDao.incrementPlayCount(item.mediaId)
                    trackDao.updateLastPlayed(item.mediaId, System.currentTimeMillis())
                    // Prefetch next stream URL for instant switching
                    prefetchNextTrackUrl()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Timber.e(error, "Player error: ${error.errorCodeName}")
            serviceScope.launch {
                handlePlaybackError(error)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> Timber.d("Buffering...")
                Player.STATE_READY -> Timber.d("Ready to play")
                Player.STATE_ENDED -> handleQueueEnd()
                Player.STATE_IDLE -> Unit
            }
        }
    }

    private fun handleQueueEnd() {
        if (player.repeatMode == Player.REPEAT_MODE_ALL && player.mediaItemCount > 0) {
            player.seekToDefaultPosition(0)
        }
    }

    private suspend fun handlePlaybackError(error: PlaybackException) {
        val currentItem = player.currentMediaItem ?: return
        Timber.w("Attempting stream URL fallback for ${currentItem.mediaId}")
        try {
            val fallbackUrl = streamUrlResolver.resolveFallback(currentItem.mediaId)
            if (fallbackUrl != null) {
                val currentIndex = player.currentMediaItemIndex
                val updatedItem = currentItem.buildUpon()
                    .setUri(android.net.Uri.parse(fallbackUrl))
                    .build()
                player.replaceMediaItem(currentIndex, updatedItem)
                player.prepare()
                player.play()
            }
        } catch (e: Exception) {
            Timber.e(e, "Fallback failed for ${currentItem.mediaId}")
        }
    }

    private fun prefetchNextTrackUrl() {
        val nextIndex = player.currentMediaItemIndex + 1
        if (nextIndex < player.mediaItemCount) {
            val nextItem = player.getMediaItemAt(nextIndex)
            serviceScope.launch(Dispatchers.IO) {
                streamUrlResolver.prefetch(nextItem.mediaId)
            }
        }
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            return serviceScope.async {
                mediaItems.map { item ->
                    val resolvedUrl = streamUrlResolver.resolve(item.mediaId)
                    if (resolvedUrl != null) {
                        item.buildUpon().setUri(android.net.Uri.parse(resolvedUrl)).build()
                    } else item
                }.toMutableList()
            }.asListenableFuture()
        }

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val commands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_PLAYER_COMMANDS.buildUpon()
                .add(SessionCommand(ACTION_SET_EQUALIZER, Bundle.EMPTY))
                .add(SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(commands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            return when (customCommand.customAction) {
                ACTION_SET_EQUALIZER -> {
                    // Handle equalizer update from UI
                    Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                else -> super.onCustomCommand(session, controller, customCommand, args)
            }
        }
    }

    companion object {
        const val ACTION_SET_EQUALIZER = "soundwave.SET_EQUALIZER"
        const val ACTION_TOGGLE_SHUFFLE = "soundwave.TOGGLE_SHUFFLE"
    }
}

@UnstableApi
private fun <T> Deferred<T>.asListenableFuture(): ListenableFuture<T> {
    val future = com.google.common.util.concurrent.SettableFuture.create<T>()
    this.invokeOnCompletion { cause ->
        if (cause != null) future.setException(cause)
        else future.set(this.getCompleted())
    }
    return future
}

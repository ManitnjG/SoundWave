package com.soundwave.app.domain.model

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

// ─── Core Track Model ─────────────────────────────────────────────────────────

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String = "",
    val album: String = "",
    val albumId: String = "",
    val albumArtUrl: String = "",
    val durationMs: Long = 0L,
    val previewUrl: String? = null,
    val streamUrl: String? = null,
    val isLocal: Boolean = false,
    val localPath: String? = null,
    val isDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val source: StreamSource = StreamSource.JIOSAAVN,
    val quality: AudioQuality = AudioQuality.HIGH,
    val lyricsId: String? = null,
    val hasLyrics: Boolean = false,
    val genre: String = "",
    val releaseDate: String = "",
    val explicitContent: Boolean = false,
    val language: String = "en",
    val popularity: Int = 0
) {
    fun toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(albumArtUrl.toUriOrNull())
            .setIsPlayable(true)
            .build()

        val uri = when {
            isLocal && localPath != null -> Uri.parse(localPath)
            streamUrl != null -> Uri.parse(streamUrl)
            previewUrl != null -> Uri.parse(previewUrl)
            else -> Uri.EMPTY
        }

        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
    }

    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

private fun String.toUriOrNull(): Uri? = runCatching { Uri.parse(this) }.getOrNull()

// ─── Album Model ──────────────────────────────────────────────────────────────

data class Album(
    val id: String,
    val name: String,
    val artist: String,
    val artistId: String = "",
    val artUrl: String = "",
    val year: Int = 0,
    val trackCount: Int = 0,
    val tracks: List<Track> = emptyList(),
    val isFavorite: Boolean = false,
    val genre: String = "",
    val description: String = "",
    val language: String = "en"
)

// ─── Artist Model ─────────────────────────────────────────────────────────────

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String = "",
    val bio: String = "",
    val followerCount: Long = 0L,
    val genres: List<String> = emptyList(),
    val isFollowing: Boolean = false,
    val popularity: Int = 0,
    val topTracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList()
)

// ─── Playlist Model ───────────────────────────────────────────────────────────

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val artUrl: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val trackCount: Int = 0,
    val tracks: List<Track> = emptyList(),
    val isUserCreated: Boolean = false,
    val isFavorite: Boolean = false,
    val isPublic: Boolean = true,
    val isOffline: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val followerCount: Long = 0L,
    val source: PlaylistSource = PlaylistSource.LOCAL
)

// ─── Queue Model ──────────────────────────────────────────────────────────────

data class QueueState(
    val tracks: List<Track> = emptyList(),
    val currentIndex: Int = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val originalOrder: List<Track> = emptyList()
) {
    val currentTrack: Track? get() = tracks.getOrNull(currentIndex)
    val hasNext: Boolean get() = currentIndex < tracks.size - 1 || repeatMode != RepeatMode.OFF
    val hasPrevious: Boolean get() = currentIndex > 0 || repeatMode != RepeatMode.OFF
}

// ─── Playback State ───────────────────────────────────────────────────────────

data class PlaybackState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1f,
    val volume: Float = 1f,
    val error: String? = null
) {
    val progress: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs) else 0f

    val bufferedProgress: Float
        get() = if (durationMs > 0) (bufferedPositionMs.toFloat() / durationMs) else 0f
}

// ─── Lyrics Models ────────────────────────────────────────────────────────────

data class LyricLine(
    val timestampMs: Long,
    val text: String,
    val translatedText: String? = null
)

data class Lyrics(
    val trackId: String,
    val lines: List<LyricLine>,
    val isSynced: Boolean,
    val source: String = "lrclib"
)

// ─── Search Results ───────────────────────────────────────────────────────────

data class SearchResult(
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val query: String = "",
    val hasMore: Boolean = false
)

// ─── Home Feed ────────────────────────────────────────────────────────────────

data class HomeFeed(
    val featuredBanner: List<Track> = emptyList(),
    val trending: List<Track> = emptyList(),
    val recentlyPlayed: List<Track> = emptyList(),
    val recommended: List<Track> = emptyList(),
    val newReleases: List<Album> = emptyList(),
    val featuredPlaylists: List<Playlist> = emptyList(),
    val topCharts: List<Track> = emptyList(),
    val moodPlaylists: List<MoodCategory> = emptyList()
)

data class MoodCategory(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Long,
    val playlists: List<Playlist> = emptyList()
)

// ─── Download Model ───────────────────────────────────────────────────────────

data class DownloadProgress(
    val trackId: String,
    val progress: Float,
    val state: DownloadState,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L
)

// ─── Audio Visualizer ─────────────────────────────────────────────────────────

data class VisualizerData(
    val waveform: FloatArray = floatArrayOf(),
    val fft: FloatArray = floatArrayOf()
)

// ─── User Preferences ─────────────────────────────────────────────────────────

data class UserPreferences(
    val streamingQuality: AudioQuality = AudioQuality.HIGH,
    val downloadQuality: AudioQuality = AudioQuality.ULTRA,
    val crossfadeDurationMs: Int = 3000,
    val normalizationEnabled: Boolean = true,
    val equalizerEnabled: Boolean = false,
    val equalizerBands: List<EqualizerBand> = EqualizerPreset.FLAT.bands,
    val equalizerPreset: EqualizerPreset = EqualizerPreset.FLAT,
    val bassBoostEnabled: Boolean = false,
    val bassBoostStrength: Short = 0,
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Short = 0,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val dynamicColor: Boolean = true,
    val downloadOnWifiOnly: Boolean = true,
    val autoDownload: Boolean = false,
    val downloadPath: String = "",
    val cacheSize: Long = 500 * 1024 * 1024L, // 500MB
    val lyricsEnabled: Boolean = true,
    val floatingLyricsEnabled: Boolean = false,
    val visualizerEnabled: Boolean = true,
    val visualizerStyle: VisualizerStyle = VisualizerStyle.BAR,
    val language: String = "en",
    val skipSilence: Boolean = false,
    val showExplicit: Boolean = true,
    val proxyEnabled: Boolean = false,
    val proxyHost: String = "",
    val proxyPort: Int = 8080
)

data class EqualizerBand(
    val centerFrequency: Int, // Hz
    val gainDb: Float,
    val minDb: Float = -15f,
    val maxDb: Float = 15f
)

// ─── Enums ────────────────────────────────────────────────────────────────────

enum class StreamSource { JIOSAAVN, YOUTUBE_MUSIC, SPOTIFY, SOUNDCLOUD, AUDIUS, LOCAL }
enum class PlaylistSource { LOCAL, SPOTIFY, JIOSAAVN, YOUTUBE }
enum class AudioQuality(val kbps: Int, val label: String) {
    LOW(96, "Low (96 kbps)"),
    MEDIUM(128, "Medium (128 kbps)"),
    HIGH(320, "High (320 kbps)"),
    ULTRA(320, "Ultra (Lossless)")
}
enum class RepeatMode { OFF, ALL, ONE }
enum class DownloadState { QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED }
enum class ThemeMode { LIGHT, DARK, AMOLED, SYSTEM }
enum class VisualizerStyle { BAR, WAVE, CIRCLE, SPECTRUM }
enum class EqualizerPreset(val displayName: String, val bands: List<EqualizerBand>) {
    FLAT("Flat", listOf(
        EqualizerBand(60, 0f), EqualizerBand(230, 0f), EqualizerBand(910, 0f),
        EqualizerBand(3600, 0f), EqualizerBand(14000, 0f)
    )),
    BASS_BOOST("Bass Boost", listOf(
        EqualizerBand(60, 6f), EqualizerBand(230, 4f), EqualizerBand(910, 0f),
        EqualizerBand(3600, -1f), EqualizerBand(14000, -1f)
    )),
    TREBLE_BOOST("Treble Boost", listOf(
        EqualizerBand(60, -1f), EqualizerBand(230, -1f), EqualizerBand(910, 0f),
        EqualizerBand(3600, 4f), EqualizerBand(14000, 6f)
    )),
    VOCAL("Vocal", listOf(
        EqualizerBand(60, -2f), EqualizerBand(230, 2f), EqualizerBand(910, 4f),
        EqualizerBand(3600, 2f), EqualizerBand(14000, 0f)
    )),
    ELECTRONIC("Electronic", listOf(
        EqualizerBand(60, 5f), EqualizerBand(230, 2f), EqualizerBand(910, -1f),
        EqualizerBand(3600, 2f), EqualizerBand(14000, 4f)
    )),
    ROCK("Rock", listOf(
        EqualizerBand(60, 4f), EqualizerBand(230, 2f), EqualizerBand(910, -1f),
        EqualizerBand(3600, 2f), EqualizerBand(14000, 3f)
    )),
    JAZZ("Jazz", listOf(
        EqualizerBand(60, 2f), EqualizerBand(230, 3f), EqualizerBand(910, 0f),
        EqualizerBand(3600, 3f), EqualizerBand(14000, 2f)
    )),
    CUSTOM("Custom", listOf(
        EqualizerBand(60, 0f), EqualizerBand(230, 0f), EqualizerBand(910, 0f),
        EqualizerBand(3600, 0f), EqualizerBand(14000, 0f)
    ))
}

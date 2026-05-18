package com.soundwave.app.domain.repository

import androidx.paging.PagingData
import com.soundwave.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getHomeFeed(): Result<HomeFeed>
    suspend fun search(query: String, page: Int = 1): Result<SearchResult>
    fun searchPaged(query: String): Flow<PagingData<Track>>
    suspend fun getTrackDetails(trackId: String): Result<Track>
    suspend fun getSuggestions(trackId: String): Result<List<Track>>
    suspend fun getAlbumDetails(albumId: String): Result<Album>
    suspend fun getArtistDetails(artistId: String): Result<Artist>
    suspend fun getPlaylistDetails(playlistId: String): Result<Playlist>
    suspend fun getLyrics(trackId: String, title: String, artist: String, duration: Int): Result<Lyrics>
    fun getFavoriteTracks(): Flow<List<Track>>
    suspend fun toggleFavoriteTrack(trackId: String, isFavorite: Boolean)
    fun getFavoritePlaylists(): Flow<List<Playlist>>
    suspend fun toggleFavoritePlaylist(id: String, isFavorite: Boolean)
    fun getRecentlyPlayed(): Flow<List<Track>>
    suspend fun addToRecentlyPlayed(trackId: String)
    fun getUserPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String, description: String): Playlist
    suspend fun addTrackToPlaylist(playlistId: String, trackId: String)
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String)
    fun getPlaylistTracks(playlistId: String): Flow<List<Track>>
    suspend fun deletePlaylist(playlistId: String)
    fun getSearchHistory(): Flow<List<String>>
    suspend fun clearSearchHistory()
}

interface UserPreferencesRepository {
    val userPreferences: Flow<com.soundwave.app.domain.model.UserPreferences?>
    suspend fun updateStreamingQuality(quality: com.soundwave.app.domain.model.AudioQuality)
    suspend fun updateThemeMode(mode: com.soundwave.app.domain.model.ThemeMode)
    suspend fun updateEqualizerEnabled(enabled: Boolean)
    suspend fun updateEqualizerBands(bands: List<com.soundwave.app.domain.model.EqualizerBand>)
    suspend fun updateCrossfadeDuration(ms: Int)
    suspend fun updateDynamicColor(enabled: Boolean)
    suspend fun updateDownloadOnWifiOnly(wifiOnly: Boolean)
    suspend fun updateLyricsEnabled(enabled: Boolean)
    suspend fun updateVisualizerEnabled(enabled: Boolean)
    suspend fun updateVisualizerStyle(style: com.soundwave.app.domain.model.VisualizerStyle)
    suspend fun updateSkipSilence(enabled: Boolean)
    suspend fun updateShowExplicit(show: Boolean)
}

interface DownloadRepository {
    fun getAllDownloads(): Flow<List<com.soundwave.app.domain.model.Track>>
    suspend fun downloadTrack(track: com.soundwave.app.domain.model.Track, quality: com.soundwave.app.domain.model.AudioQuality)
    suspend fun cancelDownload(trackId: String)
    suspend fun deleteDownload(trackId: String)
    fun getDownloadProgress(trackId: String): Flow<com.soundwave.app.domain.model.DownloadProgress?>
    fun getActiveDownloads(): Flow<List<com.soundwave.app.data.local.entities.DownloadEntity>>
}

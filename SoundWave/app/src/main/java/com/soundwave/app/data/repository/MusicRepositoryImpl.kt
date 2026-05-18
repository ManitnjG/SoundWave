package com.soundwave.app.data.repository

import androidx.paging.*
import com.soundwave.app.data.local.dao.*
import com.soundwave.app.data.local.entities.*
import com.soundwave.app.data.remote.api.JioSaavnApi
import com.soundwave.app.data.remote.api.LyricsApi
import com.soundwave.app.data.remote.dto.*
import com.soundwave.app.domain.model.*
import com.soundwave.app.domain.repository.MusicRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val jioSaavnApi: JioSaavnApi,
    private val lyricsApi: LyricsApi,
    private val trackDao: TrackDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val playlistDao: PlaylistDao,
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val lyricsDao: LyricsDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val gson: Gson
) : MusicRepository {

    // ─── Home Feed ────────────────────────────────────────────────────────────

    override suspend fun getHomeFeed(): Result<HomeFeed> = runCatching {
        val trending = runCatching { jioSaavnApi.getTrending().data?.map { it.toTrack() } ?: emptyList() }.getOrDefault(emptyList())
        val newReleases = runCatching { jioSaavnApi.getNewReleases().results?.map { it.toAlbum() } ?: emptyList() }.getOrDefault(emptyList())
        val featuredPlaylists = runCatching { jioSaavnApi.getFeaturedPlaylists().data?.data?.results?.map { it.toPlaylist() } ?: emptyList() }.getOrDefault(emptyList())
        val charts = runCatching { jioSaavnApi.getCharts().data?.map { it.toPlaylist() } ?: emptyList() }.getOrDefault(emptyList())

        val moodCategories = listOf(
            MoodCategory("happy", "Happy", "😊", 0xFFFFD93D, featuredPlaylists.take(3)),
            MoodCategory("chill", "Chill", "😌", 0xFF6BCB77, featuredPlaylists.drop(3).take(3)),
            MoodCategory("focus", "Focus", "🎯", 0xFF4D96FF, featuredPlaylists.drop(6).take(3)),
            MoodCategory("workout", "Workout", "💪", 0xFFFF6B6B, featuredPlaylists.drop(9).take(3)),
            MoodCategory("sleep", "Sleep", "😴", 0xFF845EC2, featuredPlaylists.drop(12).take(3))
        )

        HomeFeed(
            trending = trending,
            newReleases = newReleases,
            featuredPlaylists = featuredPlaylists,
            topCharts = trending.take(10),
            moodPlaylists = moodCategories,
            featuredBanner = trending.take(5)
        )
    }

    // ─── Search ───────────────────────────────────────────────────────────────

    override suspend fun search(query: String, page: Int): Result<SearchResult> = runCatching {
        if (query.isBlank()) return@runCatching SearchResult()

        val response = jioSaavnApi.search(query = query, page = page)

        // Save to search history
        searchHistoryDao.insert(SearchHistoryEntity(query = query))

        SearchResult(
            tracks = response.songs?.data?.data?.results?.map { it.toTrack() } ?: emptyList(),
            albums = response.albums?.data?.data?.results?.map { it.toAlbum() } ?: emptyList(),
            artists = response.artists?.data?.data?.results?.map { it.toArtist() } ?: emptyList(),
            playlists = response.playlists?.data?.data?.results?.map { it.toPlaylist() } ?: emptyList(),
            query = query,
            hasMore = (response.songs?.data?.data?.results?.size ?: 0) >= 20
        )
    }

    override fun searchPaged(query: String): Flow<PagingData<Track>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                object : PagingSource<Int, Track>() {
                    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
                        val page = params.key ?: 1
                        return try {
                            val result = jioSaavnApi.searchSongs(query = query, page = page, count = params.loadSize)
                            val tracks = result.results?.map { it.toTrack() } ?: emptyList()
                            LoadResult.Page(
                                data = tracks,
                                prevKey = if (page == 1) null else page - 1,
                                nextKey = if (tracks.isEmpty()) null else page + 1
                            )
                        } catch (e: Exception) {
                            LoadResult.Error(e)
                        }
                    }
                    override fun getRefreshKey(state: PagingState<Int, Track>): Int? =
                        state.anchorPosition?.let { max(1, (it / state.config.pageSize) + 1) }
                }
            }
        ).flow
    }

    // ─── Track Details ────────────────────────────────────────────────────────

    override suspend fun getTrackDetails(trackId: String): Result<Track> = runCatching {
        val cached = trackDao.getTrackById(trackId)
        if (cached != null) return@runCatching cached.toTrack()

        val response = jioSaavnApi.getSongDetails(trackId)
        val songDto = response.data?.firstOrNull() ?: throw Exception("Track not found")
        val track = songDto.toTrack()

        trackDao.insertTrack(track.toEntity())
        track
    }

    override suspend fun getSuggestions(trackId: String): Result<List<Track>> = runCatching {
        jioSaavnApi.getSuggestions(trackId).data?.map { it.toTrack() } ?: emptyList()
    }

    // ─── Album Details ────────────────────────────────────────────────────────

    override suspend fun getAlbumDetails(albumId: String): Result<Album> = runCatching {
        val response = jioSaavnApi.getAlbumDetails(albumId)
        Album(
            id = response.id ?: albumId,
            name = response.title ?: "",
            artist = response.primary_artists ?: "",
            artUrl = response.image?.toHighResImageUrl() ?: "",
            year = response.year?.toIntOrNull() ?: 0,
            tracks = response.songs?.map { it.toTrack() } ?: emptyList(),
            trackCount = response.songs?.size ?: 0
        )
    }

    // ─── Artist Details ───────────────────────────────────────────────────────

    override suspend fun getArtistDetails(artistId: String): Result<Artist> = runCatching {
        val response = jioSaavnApi.getArtistDetails(artistId)
        Artist(
            id = response.artistId ?: artistId,
            name = response.name ?: "",
            imageUrl = response.image?.toHighResImageUrl() ?: "",
            followerCount = response.follower_count?.toLongOrNull() ?: 0L,
            topTracks = response.topSongs?.map { it.toTrack() } ?: emptyList(),
            albums = response.topAlbums?.map { it.toAlbum() } ?: emptyList()
        )
    }

    // ─── Playlist Details ─────────────────────────────────────────────────────

    override suspend fun getPlaylistDetails(playlistId: String): Result<Playlist> = runCatching {
        val response = jioSaavnApi.getPlaylistDetails(playlistId)
        Playlist(
            id = response.id ?: playlistId,
            name = response.title ?: "",
            artUrl = response.image?.toHighResImageUrl() ?: "",
            ownerName = response.firstname ?: "",
            tracks = response.list?.map { it.toTrack() } ?: emptyList(),
            trackCount = response.list_count?.toIntOrNull() ?: response.list?.size ?: 0,
            followerCount = response.follower_count?.toLongOrNull() ?: 0L,
            source = PlaylistSource.JIOSAAVN
        )
    }

    // ─── Lyrics ───────────────────────────────────────────────────────────────

    override suspend fun getLyrics(trackId: String, title: String, artist: String, duration: Int): Result<Lyrics> = runCatching {
        // Check local cache first
        val cached = lyricsDao.getLyrics(trackId)
        if (cached != null) {
            val lines = gson.fromJson(cached.linesJson, Array<LyricLineDto>::class.java)
                .map { LyricLine(it.timestampMs, it.text) }
            return@runCatching Lyrics(trackId, lines, cached.isSynced, cached.source)
        }

        // Fetch from API
        val response = lyricsApi.getLyrics(
            artist = artist,
            track = title,
            durationSec = duration
        )

        val (lines, isSynced) = when {
            !response.syncedLyrics.isNullOrBlank() -> {
                Pair(parseSyncedLyrics(response.syncedLyrics), true)
            }
            !response.plainLyrics.isNullOrBlank() -> {
                val plainLines = response.plainLyrics.split("\n")
                    .mapIndexed { i, line -> LyricLine(i * 3000L, line) }
                Pair(plainLines, false)
            }
            else -> Pair(emptyList(), false)
        }

        val lyrics = Lyrics(trackId, lines, isSynced)

        // Cache to database
        val linesJson = gson.toJson(lines.map { LyricLineDto(it.timestampMs, it.text) })
        lyricsDao.insertLyrics(LyricsEntity(trackId, linesJson, isSynced))

        lyrics
    }

    // ─── Favorites ────────────────────────────────────────────────────────────

    override fun getFavoriteTracks(): Flow<List<Track>> =
        trackDao.getFavoriteTracks().map { it.map { entity -> entity.toTrack() } }

    override suspend fun toggleFavoriteTrack(trackId: String, isFavorite: Boolean) {
        trackDao.toggleFavorite(trackId, isFavorite)
    }

    override fun getFavoritePlaylists(): Flow<List<Playlist>> =
        playlistDao.getFavoritePlaylists().map { it.map { entity -> entity.toPlaylist() } }

    override suspend fun toggleFavoritePlaylist(id: String, isFavorite: Boolean) {
        playlistDao.toggleFavorite(id, isFavorite)
    }

    // ─── Recently Played ──────────────────────────────────────────────────────

    override fun getRecentlyPlayed(): Flow<List<Track>> =
        recentlyPlayedDao.getRecentlyPlayed().map { it.map { entity -> entity.toTrack() } }

    override suspend fun addToRecentlyPlayed(trackId: String) {
        recentlyPlayedDao.insert(RecentlyPlayedEntity(trackId = trackId))
        recentlyPlayedDao.trimHistory()
    }

    // ─── User Playlists ───────────────────────────────────────────────────────

    override fun getUserPlaylists(): Flow<List<Playlist>> =
        playlistDao.getUserPlaylists().map { it.map { entity -> entity.toPlaylist() } }

    override suspend fun createPlaylist(name: String, description: String): Playlist {
        val playlist = PlaylistEntity(
            id = "local_${System.currentTimeMillis()}",
            name = name,
            description = description,
            isUserCreated = true
        )
        playlistDao.insertPlaylist(playlist)
        return playlist.toPlaylist()
    }

    override suspend fun addTrackToPlaylist(playlistId: String, trackId: String) {
        val count = playlistDao.getTrackCount(playlistId)
        playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId, trackId, count))
        playlistDao.updatePlaylistMeta(playlistId, System.currentTimeMillis(), count + 1)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        playlistDao.removeTrackFromPlaylist(PlaylistTrackCrossRef(playlistId, trackId))
        val newCount = playlistDao.getTrackCount(playlistId)
        playlistDao.updatePlaylistMeta(playlistId, System.currentTimeMillis(), newCount)
    }

    override fun getPlaylistTracks(playlistId: String): Flow<List<Track>> =
        playlistDao.getPlaylistTracks(playlistId).map { it.map { e -> e.toTrack() } }

    override suspend fun deletePlaylist(playlistId: String) {
        playlistDao.getPlaylistById(playlistId)?.let { playlistDao.deletePlaylist(it) }
    }

    override fun getSearchHistory(): Flow<List<String>> =
        searchHistoryDao.getRecentSearches().map { it.map { e -> e.query } }

    override suspend fun clearSearchHistory() = searchHistoryDao.clearAll()

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private fun parseSyncedLyrics(synced: String): List<LyricLine> {
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")
        return synced.lines().mapNotNull { line ->
            regex.matchEntire(line.trim())?.let { match ->
                val minutes = match.groupValues[1].toLong()
                val seconds = match.groupValues[2].toLong()
                val centis = match.groupValues[3].toLong()
                val millisMultiplier = if (match.groupValues[3].length == 2) 10L else 1L
                val timestampMs = (minutes * 60 + seconds) * 1000 + centis * millisMultiplier
                val text = match.groupValues[4].trim()
                LyricLine(timestampMs, text)
            }
        }
    }

    private data class LyricLineDto(val timestampMs: Long, val text: String)
}

// ─── Extension Functions ──────────────────────────────────────────────────────

fun String.toHighResImageUrl(): String {
    return this.replace("150x150", "500x500")
        .replace("50x50", "500x500")
        .replace("http://", "https://")
}

fun JioSaavnSongDto.toTrack(): Track {
    val moreInfo = this.more_info
    val artistName = moreInfo?.artistMap?.primary_artists?.firstOrNull()?.name
        ?: this.primary_artists
        ?: this.music
        ?: this.subtitle ?: ""

    val imageUrl = this.image?.toHighResImageUrl() ?: ""
    val durationSec = moreInfo?.duration?.toIntOrNull() ?: this.duration?.toIntOrNull() ?: 0
    val hasLyrics = this.has_lyrics?.equals("true", ignoreCase = true) ?: false
    val is320 = moreInfo?.kbps320?.equals("true", ignoreCase = true)
        ?: this.`320kbps`?.equals("true", ignoreCase = true) ?: false

    return Track(
        id = this.id ?: "",
        title = this.title ?: this.song ?: "",
        artist = artistName,
        artistId = moreInfo?.artistMap?.primary_artists?.firstOrNull()?.id ?: this.primary_artists_id ?: "",
        album = moreInfo?.album ?: this.album ?: "",
        albumId = moreInfo?.album_id ?: this.album_id ?: "",
        albumArtUrl = imageUrl,
        durationMs = durationSec * 1000L,
        hasLyrics = hasLyrics,
        releaseDate = moreInfo?.release_date ?: this.release_date ?: this.year ?: "",
        explicitContent = (this.explicitContent ?: 0) == 1,
        language = moreInfo?.language ?: this.language ?: "en",
        quality = if (is320) AudioQuality.HIGH else AudioQuality.MEDIUM,
        downloadUrl = this.downloadUrl
    )
}

// Temporary holder for download URL in Track
private val Track.downloadUrl: List<JioSaavnDownloadUrl>? get() = null

fun JioSaavnAlbumDto.toAlbum(): Album {
    return Album(
        id = this.id ?: "",
        name = this.title ?: "",
        artist = this.primary_artists ?: this.more_info?.artistMap?.primary_artists?.firstOrNull()?.name ?: "",
        artUrl = this.image?.toHighResImageUrl() ?: "",
        year = this.year?.toIntOrNull() ?: 0,
        trackCount = this.song_count?.toIntOrNull() ?: this.list?.size ?: 0,
        tracks = this.list?.map { it.toTrack() } ?: emptyList()
    )
}

fun JioSaavnArtistDto.toArtist(): Artist {
    return Artist(
        id = this.id ?: "",
        name = this.name ?: "",
        imageUrl = this.image?.toHighResImageUrl() ?: ""
    )
}

fun JioSaavnPlaylistDto.toPlaylist(): Playlist {
    return Playlist(
        id = this.id ?: "",
        name = this.title ?: "",
        artUrl = this.image?.toHighResImageUrl() ?: "",
        ownerName = this.firstname ?: this.more_info?.firstname ?: "",
        trackCount = this.list_count?.toIntOrNull() ?: this.list?.size ?: 0,
        tracks = this.list?.map { it.toTrack() } ?: emptyList(),
        followerCount = this.follower_count?.toLongOrNull() ?: 0L,
        source = PlaylistSource.JIOSAAVN
    )
}

fun TrackEntity.toTrack(): Track = Track(
    id = id, title = title, artist = artist, artistId = artistId,
    album = album, albumId = albumId, albumArtUrl = albumArtUrl,
    durationMs = durationMs, previewUrl = previewUrl, streamUrl = streamUrl,
    isLocal = isLocal, localPath = localPath, isDownloaded = isDownloaded,
    isFavorite = isFavorite, playCount = playCount, hasLyrics = hasLyrics,
    lyricsId = lyricsId, genre = genre, releaseDate = releaseDate,
    explicitContent = explicitContent, language = language, popularity = popularity
)

fun Track.toEntity(): TrackEntity = TrackEntity(
    id = id, title = title, artist = artist, artistId = artistId,
    album = album, albumId = albumId, albumArtUrl = albumArtUrl,
    durationMs = durationMs, previewUrl = previewUrl, streamUrl = streamUrl,
    isLocal = isLocal, localPath = localPath, isDownloaded = isDownloaded,
    isFavorite = isFavorite, playCount = playCount, hasLyrics = hasLyrics,
    lyricsId = lyricsId, genre = genre, releaseDate = releaseDate,
    explicitContent = explicitContent, language = language, popularity = popularity
)

fun PlaylistEntity.toPlaylist(): Playlist = Playlist(
    id = id, name = name, description = description, artUrl = artUrl,
    ownerId = ownerId, ownerName = ownerName, trackCount = trackCount,
    isUserCreated = isUserCreated, isFavorite = isFavorite, isPublic = isPublic,
    isOffline = isOffline, createdAt = createdAt, updatedAt = updatedAt,
    source = PlaylistSource.valueOf(source)
)

package com.soundwave.app.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.soundwave.app.data.local.entities.*
import kotlinx.coroutines.flow.Flow

// ─── Track DAO ────────────────────────────────────────────────────────────────

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1 ORDER BY addedAt DESC")
    fun getDownloadedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isLocal = 1 ORDER BY title ASC")
    fun getLocalTracks(): Flow<List<TrackEntity>>

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :trackId")
    suspend fun toggleFavorite(trackId: String, isFavorite: Boolean)

    @Query("UPDATE tracks SET playCount = playCount + 1 WHERE id = :trackId")
    suspend fun incrementPlayCount(trackId: String)

    @Query("UPDATE tracks SET lastPlayedAt = :timestamp WHERE id = :trackId")
    suspend fun updateLastPlayed(trackId: String, timestamp: Long)

    @Query("UPDATE tracks SET isDownloaded = :downloaded, localPath = :path WHERE id = :trackId")
    suspend fun updateDownloadStatus(trackId: String, downloaded: Boolean, path: String?)

    @Query("SELECT * FROM tracks ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayed(limit: Int = 20): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM tracks WHERE isFavorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: String)

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchTracks(query: String): PagingSource<Int, TrackEntity>
}

// ─── Playlist DAO ─────────────────────────────────────────────────────────────

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE isUserCreated = 1 ORDER BY updatedAt DESC")
    fun getUserPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoritePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    @Delete
    suspend fun removeTrackFromPlaylist(crossRef: PlaylistTrackCrossRef)

    @Query("SELECT t.* FROM tracks t INNER JOIN playlist_tracks pt ON t.id = pt.trackId WHERE pt.playlistId = :playlistId ORDER BY pt.position ASC")
    fun getPlaylistTracks(playlistId: String): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getTrackCount(playlistId: String): Int

    @Query("UPDATE playlists SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE playlists SET updatedAt = :timestamp, trackCount = :count WHERE id = :id")
    suspend fun updatePlaylistMeta(id: String, timestamp: Long, count: Int)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearPlaylistTracks(playlistId: String)
}

// ─── Album DAO ────────────────────────────────────────────────────────────────

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Query("SELECT * FROM albums WHERE isFavorite = 1 ORDER BY savedAt DESC")
    fun getFavoriteAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: String): AlbumEntity?

    @Query("UPDATE albums SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
}

// ─── Artist DAO ───────────────────────────────────────────────────────────────

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Query("SELECT * FROM artists WHERE isFollowing = 1 ORDER BY name ASC")
    fun getFollowedArtists(): Flow<List<ArtistEntity>>

    @Query("UPDATE artists SET isFollowing = :following WHERE id = :id")
    suspend fun toggleFollow(id: String, following: Boolean)
}

// ─── Download DAO ─────────────────────────────────────────────────────────────

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("SELECT * FROM downloads ORDER BY addedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE state = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE state IN ('QUEUED', 'DOWNLOADING', 'PAUSED')")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE trackId = :trackId")
    suspend fun getDownload(trackId: String): DownloadEntity?

    @Query("UPDATE downloads SET downloadedBytes = :bytes, state = :state WHERE trackId = :id")
    suspend fun updateProgress(id: String, bytes: Long, state: String)

    @Query("DELETE FROM downloads WHERE trackId = :trackId")
    suspend fun deleteDownload(trackId: String)

    @Query("SELECT COUNT(*) FROM downloads WHERE state = 'COMPLETED'")
    fun getCompletedCount(): Flow<Int>
}

// ─── Queue DAO ────────────────────────────────────────────────────────────────

@Dao
interface QueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItem(item: QueueItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItems(items: List<QueueItemEntity>)

    @Query("SELECT * FROM queue_items ORDER BY position ASC")
    fun getQueue(): Flow<List<QueueItemEntity>>

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()

    @Query("UPDATE queue_items SET isCurrentTrack = 0")
    suspend fun clearCurrentTrack()

    @Query("UPDATE queue_items SET isCurrentTrack = 1 WHERE position = :position")
    suspend fun setCurrentTrack(position: Int)

    @Query("SELECT COUNT(*) FROM queue_items")
    suspend fun getQueueSize(): Int
}

// ─── Recently Played DAO ──────────────────────────────────────────────────────

@Dao
interface RecentlyPlayedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RecentlyPlayedEntity)

    @Query("SELECT t.* FROM tracks t INNER JOIN recently_played rp ON t.id = rp.trackId ORDER BY rp.playedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 30): Flow<List<TrackEntity>>

    @Query("DELETE FROM recently_played WHERE trackId NOT IN (SELECT trackId FROM recently_played ORDER BY playedAt DESC LIMIT 100)")
    suspend fun trimHistory()

    @Query("DELETE FROM recently_played")
    suspend fun clearHistory()
}

// ─── Search History DAO ───────────────────────────────────────────────────────

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SearchHistoryEntity)

    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}

// ─── Lyrics DAO ───────────────────────────────────────────────────────────────

@Dao
interface LyricsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsEntity)

    @Query("SELECT * FROM lyrics WHERE trackId = :trackId")
    suspend fun getLyrics(trackId: String): LyricsEntity?

    @Query("DELETE FROM lyrics WHERE fetchedAt < :cutoff")
    suspend fun deleteOldLyrics(cutoff: Long)
}

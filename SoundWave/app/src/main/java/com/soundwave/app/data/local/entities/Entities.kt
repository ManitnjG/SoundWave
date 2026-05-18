package com.soundwave.app.data.local.entities

import androidx.room.*

// ─── Track Entity ─────────────────────────────────────────────────────────────

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
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
    val lastPlayedAt: Long = 0L,
    val addedAt: Long = System.currentTimeMillis(),
    val source: String = "JIOSAAVN",
    val genre: String = "",
    val releaseDate: String = "",
    val explicitContent: Boolean = false,
    val language: String = "en",
    val popularity: Int = 0,
    val hasLyrics: Boolean = false,
    val lyricsId: String? = null
)

// ─── Album Entity ─────────────────────────────────────────────────────────────

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: String,
    val name: String,
    val artist: String,
    val artistId: String = "",
    val artUrl: String = "",
    val year: Int = 0,
    val trackCount: Int = 0,
    val isFavorite: Boolean = false,
    val genre: String = "",
    val language: String = "en",
    val savedAt: Long = System.currentTimeMillis()
)

// ─── Artist Entity ────────────────────────────────────────────────────────────

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String = "",
    val bio: String = "",
    val followerCount: Long = 0L,
    val genres: String = "", // JSON array
    val isFollowing: Boolean = false,
    val popularity: Int = 0,
    val followedAt: Long = System.currentTimeMillis()
)

// ─── Playlist Entity ──────────────────────────────────────────────────────────

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val artUrl: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val trackCount: Int = 0,
    val isUserCreated: Boolean = false,
    val isFavorite: Boolean = false,
    val isPublic: Boolean = true,
    val isOffline: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val source: String = "LOCAL"
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(entity = PlaylistEntity::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TrackEntity::class, parentColumns = ["id"], childColumns = ["trackId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("playlistId"), Index("trackId")]
)
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val position: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

// ─── Download Entity ──────────────────────────────────────────────────────────

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val trackId: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String = "",
    val filePath: String = "",
    val fileSize: Long = 0L,
    val downloadedBytes: Long = 0L,
    val state: String = "QUEUED",
    val quality: String = "HIGH",
    val addedAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L,
    val error: String? = null,
    val workRequestId: String? = null
)

// ─── Queue Entity ─────────────────────────────────────────────────────────────

@Entity(tableName = "queue_items", indices = [Index("position")])
data class QueueItemEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val trackId: String,
    val position: Int,
    val isCurrentTrack: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

// ─── Recently Played Entity ───────────────────────────────────────────────────

@Entity(tableName = "recently_played", indices = [Index("trackId", unique = true)])
data class RecentlyPlayedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: String,
    val playedAt: Long = System.currentTimeMillis(),
    val contextType: String = "track", // track, album, playlist
    val contextId: String = ""
)

// ─── Search History Entity ────────────────────────────────────────────────────

@Entity(tableName = "search_history", indices = [Index("query", unique = true)])
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val searchedAt: Long = System.currentTimeMillis(),
    val resultCount: Int = 0
)

// ─── Lyrics Entity ────────────────────────────────────────────────────────────

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey val trackId: String,
    val linesJson: String, // JSON serialized list of LyricLine
    val isSynced: Boolean = false,
    val source: String = "lrclib",
    val fetchedAt: Long = System.currentTimeMillis()
)

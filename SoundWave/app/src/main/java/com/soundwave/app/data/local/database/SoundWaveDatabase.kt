package com.soundwave.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.soundwave.app.data.local.dao.*
import com.soundwave.app.data.local.entities.*

@Database(
    entities = [
        TrackEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        DownloadEntity::class,
        QueueItemEntity::class,
        RecentlyPlayedEntity::class,
        SearchHistoryEntity::class,
        LyricsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SoundWaveDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun downloadDao(): DownloadDao
    abstract fun queueDao(): QueueDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun lyricsDao(): LyricsDao
}

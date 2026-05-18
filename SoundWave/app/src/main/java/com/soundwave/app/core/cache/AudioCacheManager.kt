package com.soundwave.app.core.cache

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.*
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class AudioCacheManager @Inject constructor(
    private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val CACHE_DIR = "audio_cache"
        private const val DEFAULT_CACHE_SIZE = 500 * 1024 * 1024L // 500MB
        private const val DOWNLOAD_CACHE_DIR = "downloads"
    }

    private val cacheDir = File(context.cacheDir, CACHE_DIR)
    private val downloadDir = File(context.getExternalFilesDir(null), DOWNLOAD_CACHE_DIR)

    // ExoPlayer SimpleCache for streaming
    private val streamingCache: SimpleCache by lazy {
        cacheDir.mkdirs()
        SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(DEFAULT_CACHE_SIZE),
            StandaloneDatabaseProvider(context)
        )
    }

    // Separate cache for offline downloads (no eviction)
    private val downloadCache: SimpleCache by lazy {
        downloadDir.mkdirs()
        SimpleCache(
            downloadDir,
            NoOpCacheEvictor(),
            StandaloneDatabaseProvider(context)
        )
    }

    fun buildCachingMediaSourceFactory(context: Context): DefaultMediaSourceFactory {
        val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("SoundWave/1.0 (Android)")

        val upstreamFactory = DefaultDataSource.Factory(context, okHttpDataSourceFactory)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(streamingCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            .setCacheWriteDataSinkFactory(
                CacheDataSink.Factory()
                    .setCache(streamingCache)
                    .setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE)
            )

        return DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)
    }

    fun buildDownloadDataSourceFactory(): CacheDataSource.Factory {
        val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        val upstreamFactory = DefaultDataSource.Factory(context, okHttpDataSourceFactory)
        return CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(
                CacheDataSink.Factory()
                    .setCache(downloadCache)
                    .setFragmentSize(Long.MAX_VALUE) // Write entire file for downloads
            )
    }

    fun getCachedTrackPath(trackId: String): String? {
        return try {
            val cachedFile = File(downloadDir, "$trackId.mp3")
            if (cachedFile.exists() && cachedFile.length() > 0) cachedFile.absolutePath else null
        } catch (e: Exception) {
            null
        }
    }

    fun isTrackCached(trackId: String): Boolean {
        return getCachedTrackPath(trackId) != null
    }

    fun getStreamingCacheSize(): Long {
        return try {
            cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        } catch (e: Exception) { 0L }
    }

    fun getDownloadCacheSize(): Long {
        return try {
            downloadDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        } catch (e: Exception) { 0L }
    }

    fun clearStreamingCache() {
        try {
            streamingCache.keys.forEach { key ->
                streamingCache.removeResource(key)
            }
            Timber.d("Streaming cache cleared")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear streaming cache")
        }
    }

    fun deleteDownload(trackId: String) {
        try {
            val file = File(downloadDir, "$trackId.mp3")
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete download: $trackId")
        }
    }

    fun getStreamingCacheDataSource(): DataSource.Factory {
        val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        val upstreamFactory = DefaultDataSource.Factory(context, okHttpDataSourceFactory)
        return CacheDataSource.Factory()
            .setCache(streamingCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
    }

    fun getDownloadCache() = downloadCache

    fun release() {
        try {
            streamingCache.release()
            downloadCache.release()
        } catch (e: Exception) {
            Timber.e(e, "Error releasing cache")
        }
    }
}

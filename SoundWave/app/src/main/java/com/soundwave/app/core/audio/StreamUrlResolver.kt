package com.soundwave.app.core.audio

import com.soundwave.app.data.remote.api.JioSaavnApi
import com.soundwave.app.data.remote.api.ProxyApi
import com.soundwave.app.data.remote.api.AudiusApi
import com.soundwave.app.core.cache.AudioCacheManager
import com.soundwave.app.domain.model.AudioQuality
import com.soundwave.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamUrlResolver @Inject constructor(
    private val jioSaavnApi: JioSaavnApi,
    private val proxyApi: ProxyApi,
    private val audiusApi: AudiusApi,
    private val audioCacheManager: AudioCacheManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val okHttpClient: OkHttpClient
) {
    // In-memory URL cache to avoid repeated API calls
    private val urlCache = HashMap<String, CachedUrl>()
    private val prefetchJobs = HashMap<String, Job>()
    private val resolverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    data class CachedUrl(
        val url: String,
        val expiresAt: Long,
        val quality: AudioQuality
    ) {
        val isExpired get() = System.currentTimeMillis() > expiresAt
    }

    suspend fun resolve(trackId: String): String? = withContext(Dispatchers.IO) {
        // 1. Check in-memory cache
        urlCache[trackId]?.takeIf { !it.isExpired }?.let { return@withContext it.url }

        // 2. Check disk cache
        audioCacheManager.getCachedTrackPath(trackId)?.let { return@withContext it }

        val prefs = userPreferencesRepository.userPreferences.first()
        val quality = prefs?.streamingQuality ?: AudioQuality.HIGH

        // 3. Try sources in priority order
        val sources = listOf(
            ::resolveFromJioSaavn,
            ::resolveFromProxy,
            ::resolveFromAudius
        )

        for (source in sources) {
            try {
                val url = source(trackId, quality)
                if (url != null && isUrlReachable(url)) {
                    cacheUrl(trackId, url, quality)
                    return@withContext url
                }
            } catch (e: Exception) {
                Timber.w(e, "Source failed for $trackId, trying next...")
            }
        }

        Timber.e("All sources exhausted for trackId: $trackId")
        null
    }

    suspend fun resolveFallback(trackId: String): String? = withContext(Dispatchers.IO) {
        urlCache.remove(trackId) // Force re-fetch
        resolve(trackId)
    }

    fun prefetch(trackId: String) {
        if (prefetchJobs[trackId]?.isActive == true) return
        prefetchJobs[trackId] = resolverScope.launch {
            try {
                resolve(trackId)
                Timber.d("Prefetched stream URL for $trackId")
            } catch (e: Exception) {
                Timber.w(e, "Prefetch failed for $trackId")
            }
        }
    }

    private suspend fun resolveFromJioSaavn(trackId: String, quality: AudioQuality): String? {
        return try {
            val response = jioSaavnApi.getSongDetails(trackId)
            val downloadUrls = response.data?.firstOrNull()?.downloadUrl ?: return null
            // Select appropriate quality URL
            when (quality) {
                AudioQuality.ULTRA, AudioQuality.HIGH ->
                    downloadUrls.find { it.quality == "320kbps" }?.url
                        ?: downloadUrls.find { it.quality == "160kbps" }?.url
                        ?: downloadUrls.lastOrNull()?.url
                AudioQuality.MEDIUM ->
                    downloadUrls.find { it.quality == "160kbps" }?.url
                        ?: downloadUrls.lastOrNull()?.url
                AudioQuality.LOW ->
                    downloadUrls.find { it.quality == "96kbps" }?.url
                        ?: downloadUrls.firstOrNull()?.url
            }
        } catch (e: Exception) {
            Timber.w(e, "JioSaavn resolution failed")
            null
        }
    }

    private suspend fun resolveFromProxy(trackId: String, quality: AudioQuality): String? {
        return try {
            val response = proxyApi.getStreamUrl(trackId, quality.kbps)
            response.streamUrl
        } catch (e: Exception) {
            Timber.w(e, "Proxy resolution failed")
            null
        }
    }

    private suspend fun resolveFromAudius(trackId: String, quality: AudioQuality): String? {
        return try {
            val hosts = audiusApi.getHosts()
            val host = hosts.data?.firstOrNull() ?: return null
            "$host/v1/tracks/$trackId/stream"
        } catch (e: Exception) {
            Timber.w(e, "Audius resolution failed")
            null
        }
    }

    private suspend fun isUrlReachable(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).head().build()
            val response = okHttpClient.newCall(request).execute()
            response.isSuccessful.also { response.close() }
        } catch (e: Exception) {
            false
        }
    }

    private fun cacheUrl(trackId: String, url: String, quality: AudioQuality) {
        val ttl = 3600_000L // 1 hour TTL for stream URLs
        urlCache[trackId] = CachedUrl(url, System.currentTimeMillis() + ttl, quality)
    }

    fun clearCache() = urlCache.clear()
}

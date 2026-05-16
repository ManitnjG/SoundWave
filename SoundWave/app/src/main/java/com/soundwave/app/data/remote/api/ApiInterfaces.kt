package com.soundwave.app.data.remote.api

import com.soundwave.app.data.remote.dto.*
import retrofit2.http.*

// ─── JioSaavn API ─────────────────────────────────────────────────────────────

interface JioSaavnApi {
    @GET(".")
    suspend fun search(
        @Query("__call") call: String = "search.getResults",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0",
        @Query("query") query: String,
        @Query("n") count: Int = 20,
        @Query("p") page: Int = 1
    ): JioSaavnSearchResponse

    @GET(".")
    suspend fun searchSongs(
        @Query("__call") call: String = "search.getSongs",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0",
        @Query("query") query: String,
        @Query("n") count: Int = 20,
        @Query("p") page: Int = 1
    ): JioSaavnSongsSearchResponse

    @GET(".")
    suspend fun searchAlbums(
        @Query("__call") call: String = "search.getAlbums",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0",
        @Query("query") query: String,
        @Query("n") count: Int = 20,
        @Query("p") page: Int = 1
    ): JioSaavnAlbumsSearchResponse

    @GET(".")
    suspend fun getSongDetails(
        @Query("pids") songId: String,
        @Query("__call") call: String = "song.getDetails",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0"
    ): JioSaavnSongDetailResponse

    @GET(".")
    suspend fun getAlbumDetails(
        @Query("albumid") albumId: String,
        @Query("__call") call: String = "content.getAlbumDetails",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0"
    ): JioSaavnAlbumDetailResponse

    @GET(".")
    suspend fun getArtistDetails(
        @Query("artistId") artistId: String,
        @Query("__call") call: String = "artist.getArtistPageDetails",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0",
        @Query("n_song") songCount: Int = 10,
        @Query("n_album") albumCount: Int = 10,
        @Query("page") page: Int = 0
    ): JioSaavnArtistDetailResponse

    @GET(".")
    suspend fun getTrending(
        @Query("__call") call: String = "content.getTrending",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0",
        @Query("entity_type") type: String = "song",
        @Query("entity_language") language: String = "hindi,english",
        @Query("n") count: Int = 20
    ): JioSaavnTrendingResponse

    @GET(".")
    suspend fun getFeaturedPlaylists(
        @Query("__call") call: String = "content.getFeaturedPlaylists",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0",
        @Query("n") count: Int = 20,
        @Query("p") page: Int = 1
    ): JioSaavnPlaylistResponse

    @GET(".")
    suspend fun getNewReleases(
        @Query("__call") call: String = "content.getAlbums",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0",
        @Query("n") count: Int = 20,
        @Query("p") page: Int = 1
    ): JioSaavnAlbumsSearchResponse

    @GET(".")
    suspend fun getCharts(
        @Query("__call") call: String = "content.getCharts",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0",
        @Query("n") count: Int = 20
    ): JioSaavnChartsResponse

    @GET(".")
    suspend fun getPlaylistDetails(
        @Query("listid") playlistId: String,
        @Query("__call") call: String = "playlist.getDetails",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0",
        @Query("n") count: Int = 50,
        @Query("p") page: Int = 1
    ): JioSaavnPlaylistDetailResponse

    @GET(".")
    suspend fun getSuggestions(
        @Query("pid") trackId: String,
        @Query("__call") call: String = "reco.getreco",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("api_version") version: Int = 4,
        @Query("ctx") ctx: String = "web6dot0"
    ): JioSaavnRecoResponse
}

// ─── Proxy API ────────────────────────────────────────────────────────────────

interface ProxyApi {
    @GET("/stream")
    suspend fun getStreamUrl(
        @Query("id") trackId: String,
        @Query("quality") qualityKbps: Int = 320
    ): ProxyStreamResponse

    @GET("/metadata")
    suspend fun getTrackMetadata(
        @Query("id") trackId: String,
        @Query("source") source: String = "spotify"
    ): ProxyMetadataResponse

    @GET("/health")
    suspend fun checkHealth(): ProxyHealthResponse
}

// ─── Lyrics API (lrclib.net) ──────────────────────────────────────────────────

interface LyricsApi {
    @GET("/api/get")
    suspend fun getLyrics(
        @Query("artist_name") artist: String,
        @Query("track_name") track: String,
        @Query("album_name") album: String = "",
        @Query("duration") durationSec: Int = 0
    ): LyricsResponse

    @GET("/api/search")
    suspend fun searchLyrics(
        @Query("track_name") track: String,
        @Query("artist_name") artist: String
    ): List<LyricsSearchResult>
}

// ─── Audius API ───────────────────────────────────────────────────────────────

interface AudiusApi {
    @GET("/v1/discovery-nodes")
    suspend fun getHosts(): AudiusHostsResponse

    @GET("/v1/tracks/trending")
    suspend fun getTrendingTracks(
        @Query("time") time: String = "week"
    ): AudiusTrendingResponse
}

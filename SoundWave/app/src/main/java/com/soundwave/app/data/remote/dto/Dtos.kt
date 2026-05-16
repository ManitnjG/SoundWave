package com.soundwave.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ─── JioSaavn DTOs ────────────────────────────────────────────────────────────

data class JioSaavnSongDto(
    val id: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val type: String? = null,
    val perma_url: String? = null,
    val image: String? = null,
    val music: String? = null,
    val song: String? = null,
    val album: String? = null,
    val album_id: String? = null,
    val primary_artists: String? = null,
    val primary_artists_id: String? = null,
    val duration: String? = null,
    val has_lyrics: String? = null,
    val lyrics_snippet: String? = null,
    val release_date: String? = null,
    val year: String? = null,
    val language: String? = null,
    val `320kbps`: String? = null,
    val explicitContent: Int? = null,
    val play_count: String? = null,
    val more_info: JioSaavnMoreInfo? = null,
    val downloadUrl: List<JioSaavnDownloadUrl>? = null
)

data class JioSaavnMoreInfo(
    val music: String? = null,
    val album_id: String? = null,
    val album: String? = null,
    val label: String? = null,
    val origin: String? = null,
    val is_dolby_content: Boolean? = null,
    val encrypted_media_url: String? = null,
    val encrypted_media_path: String? = null,
    val album_url: String? = null,
    val duration: String? = null,
    val rights: JioSaavnRights? = null,
    val kbps320: String? = null,
    val artistMap: JioSaavnArtistMap? = null,
    val release_date: String? = null,
    val vcode: String? = null,
    val trillerAvailable: Boolean? = null,
    val language: String? = null,
    val playback_rights: JioSaavnPlaybackRights? = null
)

data class JioSaavnDownloadUrl(
    val quality: String? = null,
    val url: String? = null
)

data class JioSaavnRights(
    val code: String? = null,
    val reason: String? = null,
    val cacheable: String? = null,
    val delete_cached_object: String? = null
)

data class JioSaavnArtistMap(
    val primary_artists: List<JioSaavnArtistDto>? = null,
    val featured_artists: List<JioSaavnArtistDto>? = null,
    val artists: List<JioSaavnArtistDto>? = null
)

data class JioSaavnPlaybackRights(
    val value: Boolean? = null,
    val reason: String? = null
)

data class JioSaavnArtistDto(
    val id: String? = null,
    val name: String? = null,
    val role: String? = null,
    val type: String? = null,
    val image: String? = null,
    val perma_url: String? = null
)

data class JioSaavnAlbumDto(
    val id: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val type: String? = null,
    val image: String? = null,
    val perma_url: String? = null,
    val year: String? = null,
    val song_count: String? = null,
    val primary_artists: String? = null,
    val more_info: JioSaavnAlbumMoreInfo? = null,
    val list: List<JioSaavnSongDto>? = null
)

data class JioSaavnAlbumMoreInfo(
    val artistMap: JioSaavnArtistMap? = null,
    val query: String? = null,
    val text: String? = null,
    val topc_hat: String? = null,
    val artist_map: JioSaavnArtistMap? = null
)

data class JioSaavnPlaylistDto(
    val id: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val type: String? = null,
    val image: String? = null,
    val perma_url: String? = null,
    val list: List<JioSaavnSongDto>? = null,
    val list_count: String? = null,
    val firstname: String? = null,
    val follower_count: String? = null,
    val more_info: JioSaavnPlaylistMoreInfo? = null
)

data class JioSaavnPlaylistMoreInfo(
    val uid: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val is_dolby_content: Boolean? = null,
    val sub_types: Any? = null,
    val video_available: Boolean? = null
)

// ─── Response Wrappers ────────────────────────────────────────────────────────

data class JioSaavnSearchResponse(
    val songs: JioSaavnDataWrapper<JioSaavnSongDto>? = null,
    val albums: JioSaavnDataWrapper<JioSaavnAlbumDto>? = null,
    val playlists: JioSaavnDataWrapper<JioSaavnPlaylistDto>? = null,
    val artists: JioSaavnDataWrapper<JioSaavnArtistDto>? = null
)

data class JioSaavnDataWrapper<T>(
    val data: JioSaavnData<T>? = null
)

data class JioSaavnData<T>(
    val results: List<T>? = null,
    val total: Int? = null,
    val start: Int? = null,
    val count: Int? = null
)

data class JioSaavnSongsSearchResponse(
    val results: List<JioSaavnSongDto>? = null,
    val total: Int? = null,
    val start: Int? = null,
    val count: Int? = null
)

data class JioSaavnAlbumsSearchResponse(
    val results: List<JioSaavnAlbumDto>? = null,
    val total: Int? = null,
    val start: Int? = null
)

data class JioSaavnSongDetailResponse(
    val songs: List<JioSaavnSongDto>? = null,
    val data: List<JioSaavnSongDto>? = null
)

data class JioSaavnAlbumDetailResponse(
    val id: String? = null,
    val title: String? = null,
    val songs: List<JioSaavnSongDto>? = null,
    val image: String? = null,
    val primary_artists: String? = null,
    val year: String? = null
)

data class JioSaavnArtistDetailResponse(
    val artistId: String? = null,
    val name: String? = null,
    val image: String? = null,
    val follower_count: String? = null,
    val isVerified: Boolean? = null,
    val dominantLanguage: String? = null,
    val dominantType: String? = null,
    val topSongs: List<JioSaavnSongDto>? = null,
    val topAlbums: List<JioSaavnAlbumDto>? = null,
    val similarArtists: List<JioSaavnArtistDto>? = null,
    val modules: Any? = null
)

data class JioSaavnTrendingResponse(
    val data: List<JioSaavnSongDto>? = null
)

data class JioSaavnPlaylistResponse(
    val data: JioSaavnData<JioSaavnPlaylistDto>? = null
)

data class JioSaavnPlaylistDetailResponse(
    val id: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val image: String? = null,
    val list: List<JioSaavnSongDto>? = null,
    val list_count: String? = null,
    val follower_count: String? = null,
    val firstname: String? = null
)

data class JioSaavnChartsResponse(
    val data: List<JioSaavnPlaylistDto>? = null
)

data class JioSaavnRecoResponse(
    val data: List<JioSaavnSongDto>? = null
)

// ─── Proxy DTOs ───────────────────────────────────────────────────────────────

data class ProxyStreamResponse(
    val streamUrl: String? = null,
    val quality: Int? = null,
    val expiresAt: Long? = null,
    val source: String? = null
)

data class ProxyMetadataResponse(
    val id: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val duration: Long? = null,
    val artUrl: String? = null,
    val spotifyId: String? = null
)

data class ProxyHealthResponse(
    val status: String? = null,
    val uptime: Long? = null,
    val version: String? = null
)

// ─── Lyrics DTOs ──────────────────────────────────────────────────────────────

data class LyricsResponse(
    val id: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val duration: Double? = null,
    val instrumental: Boolean? = null,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
)

data class LyricsSearchResult(
    val id: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val duration: Double? = null
)

// ─── Audius DTOs ──────────────────────────────────────────────────────────────

data class AudiusHostsResponse(
    val data: List<String>? = null
)

data class AudiusTrendingResponse(
    val data: List<AudiusTrackDto>? = null
)

data class AudiusTrackDto(
    val id: String? = null,
    val title: String? = null,
    val duration: Int? = null,
    val artwork: AudiusArtwork? = null,
    val user: AudiusUser? = null,
    val play_count: Int? = null
)

data class AudiusArtwork(
    @SerializedName("480x480") val medium: String? = null,
    @SerializedName("1000x1000") val large: String? = null
)

data class AudiusUser(
    val id: String? = null,
    val name: String? = null
)

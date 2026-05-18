package com.soundwave.app.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.soundwave.app.domain.model.*
import com.soundwave.app.presentation.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToPlaylist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToArtist: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 160.dp)
    ) {
        // Header
        item {
            HomeHeader(
                greeting = uiState.greeting,
                onSettingsClick = {}
            )
        }

        // Quick access grid (recently played)
        if (uiState.recentlyPlayed.isNotEmpty() || uiState.isLoading) {
            item {
                QuickAccessGrid(
                    tracks = uiState.recentlyPlayed.take(6),
                    isLoading = uiState.isLoading,
                    onTrackClick = { track ->
                        viewModel.playTrack(track, uiState.recentlyPlayed)
                        onNavigateToPlayer()
                    }
                )
            }
        }

        // Featured banner / top picks
        if (uiState.featuredBanner.isNotEmpty() || uiState.isLoading) {
            item {
                SectionHeader("Top Picks For You")
                FeaturedBanner(
                    tracks = uiState.featuredBanner,
                    isLoading = uiState.isLoading,
                    onTrackClick = { track ->
                        viewModel.playTrack(track, uiState.featuredBanner)
                        onNavigateToPlayer()
                    }
                )
            }
        }

        // Trending now
        if (uiState.trending.isNotEmpty() || uiState.isLoading) {
            item {
                SectionHeader("Trending Now")
                TrackRow(
                    tracks = uiState.trending,
                    isLoading = uiState.isLoading,
                    onTrackClick = { track ->
                        viewModel.playTrack(track, uiState.trending)
                        onNavigateToPlayer()
                    }
                )
            }
        }

        // Featured playlists
        if (uiState.featuredPlaylists.isNotEmpty() || uiState.isLoading) {
            item {
                SectionHeader("Featured Playlists")
                PlaylistRow(
                    playlists = uiState.featuredPlaylists,
                    isLoading = uiState.isLoading,
                    onPlaylistClick = onNavigateToPlaylist
                )
            }
        }

        // New releases
        if (uiState.newReleases.isNotEmpty() || uiState.isLoading) {
            item {
                SectionHeader("New Releases")
                AlbumRow(
                    albums = uiState.newReleases,
                    isLoading = uiState.isLoading,
                    onAlbumClick = onNavigateToAlbum
                )
            }
        }

        // Mood categories
        if (uiState.moodPlaylists.isNotEmpty()) {
            item {
                SectionHeader("Browse by Mood")
                MoodGrid(
                    categories = uiState.moodPlaylists,
                    onCategoryClick = { category ->
                        category.playlists.firstOrNull()?.let { onNavigateToPlaylist(it.id) }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(greeting: String, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "SoundWave",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Notifications, "Notifications", tint = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, "Settings", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun QuickAccessGrid(
    tracks: List<Track>,
    isLoading: Boolean,
    onTrackClick: (Track) -> Unit
) {
    val items = if (isLoading) List(6) { null } else tracks.map { it }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(items.size) { i ->
            val track = items[i]
            QuickAccessCard(
                track = track,
                isLoading = track == null,
                onClick = { track?.let(onTrackClick) }
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    track: Track?,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                ShimmerBox(modifier = Modifier.size(56.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(12.dp).padding(horizontal = 12.dp))
            } else {
                AsyncImage(
                    model = track?.albumArtUrl,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = track?.title ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String? = null, onActionClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun FeaturedBanner(tracks: List<Track>, isLoading: Boolean, onTrackClick: (Track) -> Unit) {
    val items = if (isLoading) List(5) { null } else tracks.take(5).map { it }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items.size) { i ->
            val track = items[i]
            FeaturedCard(track = track, isLoading = track == null, onClick = { track?.let(onTrackClick) })
        }
    }
}

@Composable
private fun FeaturedCard(track: Track?, isLoading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.width(200.dp).height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
    ) {
        if (isLoading) {
            ShimmerBox(Modifier.fillMaxSize())
        } else {
            AsyncImage(
                model = track?.albumArtUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f)))
                )
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
            ) {
                Text(track?.title ?: "", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(track?.artist ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun TrackRow(tracks: List<Track>, isLoading: Boolean, onTrackClick: (Track) -> Unit) {
    val items = if (isLoading) List(8) { null } else tracks.map { it }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items.size) { i ->
            val track = items[i]
            TrackCard(track = track, isLoading = track == null, onClick = { track?.let(onTrackClick) })
        }
    }
}

@Composable
fun TrackCard(track: Track?, isLoading: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(140.dp).clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(140.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (isLoading) ShimmerBox(Modifier.fillMaxSize())
            else AsyncImage(
                model = track?.albumArtUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        if (isLoading) {
            ShimmerBox(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(4.dp)))
            ShimmerBox(Modifier.fillMaxWidth(0.7f).height(10.dp).clip(RoundedCornerShape(4.dp)))
        } else {
            Text(track?.title ?: "", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track?.artist ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun PlaylistRow(playlists: List<Playlist>, isLoading: Boolean, onPlaylistClick: (String) -> Unit) {
    val items = if (isLoading) List(6) { null } else playlists.map { it }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items.size) { i ->
            val pl = items[i]
            PlaylistCard(playlist = pl, isLoading = pl == null, onClick = { pl?.let { onPlaylistClick(it.id) } })
        }
    }
}

@Composable
fun PlaylistCard(playlist: Playlist?, isLoading: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(160.dp).clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(160.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            if (isLoading) ShimmerBox(Modifier.fillMaxSize())
            else AsyncImage(model = playlist?.artUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        if (isLoading) {
            ShimmerBox(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(4.dp)))
        } else {
            Text(playlist?.name ?: "", style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun AlbumRow(albums: List<Album>, isLoading: Boolean, onAlbumClick: (String) -> Unit) {
    val items = if (isLoading) List(6) { null } else albums.map { it }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items.size) { i ->
            val album = items[i]
            Column(
                modifier = Modifier.width(140.dp).clickable { album?.let { onAlbumClick(it.id) } },
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(140.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    if (isLoading) ShimmerBox(Modifier.fillMaxSize())
                    else AsyncImage(model = album?.artUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                if (!isLoading) {
                    Text(album?.name ?: "", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(album?.artist ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else {
                    ShimmerBox(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(4.dp)))
                }
            }
        }
    }
}

@Composable
private fun MoodGrid(categories: List<MoodCategory>, onCategoryClick: (MoodCategory) -> Unit) {
    val moodColors = listOf(
        listOf(Color(0xFFFF6B6B), Color(0xFFFF8E8E)),
        listOf(Color(0xFF6BCB77), Color(0xFF4CAF50)),
        listOf(Color(0xFF4D96FF), Color(0xFF2979FF)),
        listOf(Color(0xFFFFD93D), Color(0xFFFFB300)),
        listOf(Color(0xFFFF9FF3), Color(0xFFE040FB)),
        listOf(Color(0xFF845EC2), Color(0xFF6A1B9A))
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories.size) { i ->
            val cat = categories[i]
            val colors = moodColors[i % moodColors.size]
            Box(
                modifier = Modifier.width(120.dp).height(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(colors))
                    .clickable { onCategoryClick(cat) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(cat.emoji, style = MaterialTheme.typography.headlineSmall)
                    Text(cat.name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }
        }
    }
}

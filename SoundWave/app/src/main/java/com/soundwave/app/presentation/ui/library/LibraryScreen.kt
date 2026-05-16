package com.soundwave.app.presentation.ui.library

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.soundwave.app.domain.model.*
import com.soundwave.app.presentation.ui.search.TrackListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToPlaylist: (String) -> Unit,
    onNavigateToPlayer: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Playlists", "Favorites", "Albums", "Artists")

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Your Library", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
            Row {
                IconButton(onClick = {}) { Icon(Icons.Filled.Search, null) }
                IconButton(onClick = { viewModel.createNewPlaylist() }) { Icon(Icons.Filled.Add, "New Playlist") }
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { i, tab ->
                Tab(selected = i == selectedTab, onClick = { selectedTab = i },
                    text = { Text(tab, style = MaterialTheme.typography.labelLarge) })
            }
        }

        when (selectedTab) {
            0 -> PlaylistsTab(uiState.playlists, uiState.favoritePlaylists, onPlaylistClick = onNavigateToPlaylist)
            1 -> FavoritesTab(uiState.favoriteTracks, onTrackClick = { t -> viewModel.playTrack(t, uiState.favoriteTracks); onNavigateToPlayer() })
            2 -> AlbumsTab(uiState.favoriteAlbums)
            3 -> ArtistsTab(uiState.followedArtists)
        }
    }
}

@Composable
private fun PlaylistsTab(playlists: List<Playlist>, favPlaylists: List<Playlist>, onPlaylistClick: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)) {
        items(playlists.size + favPlaylists.size) { i ->
            val playlist = if (i < playlists.size) playlists[i] else favPlaylists[i - playlists.size]
            ListItem(
                headlineContent = { Text(playlist.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text("${playlist.trackCount} songs • ${if (playlist.isUserCreated) "Your playlist" else playlist.ownerName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingContent = {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        if (playlist.artUrl.isNotEmpty()) AsyncImage(model = playlist.artUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Icon(Icons.Filled.MusicNote, null, modifier = Modifier.align(Alignment.Center), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                modifier = Modifier.clickable { onPlaylistClick(playlist.id) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}

@Composable
private fun FavoritesTab(tracks: List<Track>, onTrackClick: (Track) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
        if (tracks.isEmpty()) {
            item { Box(Modifier.fillMaxWidth().padding(64.dp), Alignment.Center) { Text("No favorites yet", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        } else {
            items(tracks.size) { i -> TrackListItem(tracks[i], onClick = { onTrackClick(tracks[i]) }) }
        }
    }
}

@Composable
private fun AlbumsTab(albums: List<Album>) {
    LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
        if (albums.isEmpty()) {
            item { Box(Modifier.fillMaxWidth().padding(64.dp), Alignment.Center) { Text("No saved albums", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }
}

@Composable
private fun ArtistsTab(artists: List<Artist>) {
    LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
        if (artists.isEmpty()) {
            item { Box(Modifier.fillMaxWidth().padding(64.dp), Alignment.Center) { Text("Follow artists to see them here", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }
}

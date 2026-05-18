package com.soundwave.app.presentation.ui.search

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.soundwave.app.domain.model.*
import com.soundwave.app.presentation.ui.components.ShimmerBox

@Composable
fun SearchScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToPlaylist: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Search bar
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(16.dp).focusRequester(focusRequester),
            placeholder = { Text("Search songs, artists, albums...") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = {
                AnimatedVisibility(uiState.query.isNotEmpty()) {
                    IconButton(onClick = viewModel::clearQuery) {
                        Icon(Icons.Filled.Clear, "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        when {
            uiState.query.isEmpty() -> {
                // Browse categories + search history
                SearchIdleContent(
                    history = uiState.searchHistory,
                    onHistoryClick = viewModel::onQueryChange,
                    onClearHistory = viewModel::clearHistory
                )
            }
            uiState.isLoading -> {
                SearchLoadingContent()
            }
            else -> {
                SearchResultsContent(
                    result = uiState.searchResult,
                    onTrackClick = { track ->
                        viewModel.playTrack(track, uiState.searchResult.tracks)
                        onNavigateToPlayer()
                    },
                    onAlbumClick = onNavigateToAlbum,
                    onArtistClick = onNavigateToArtist,
                    onPlaylistClick = onNavigateToPlaylist
                )
            }
        }
    }
}

@Composable
private fun SearchIdleContent(history: List<String>, onHistoryClick: (String) -> Unit, onClearHistory: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (history.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Recent Searches", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onClearHistory) { Text("Clear all", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium) }
                }
            }
            items(history.size) { i ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onHistoryClick(history[i]) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Text(history[i], style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Text("Browse Categories", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            GenreGrid()
        }
    }
}

@Composable
private fun GenreGrid() {
    val genres = listOf("Pop" to Color(0xFF1ED760), "Hip-Hop" to Color(0xFFFF6B6B), "Rock" to Color(0xFF4D96FF),
        "Electronic" to Color(0xFFFFD93D), "R&B" to Color(0xFFFF9FF3), "Jazz" to Color(0xFF6BCB77),
        "Classical" to Color(0xFF845EC2), "Bollywood" to Color(0xFFFF9800), "K-Pop" to Color(0xFFE91E63),
        "Indie" to Color(0xFF00BCD4))
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(360.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = false
    ) {
        items(genres.size) { i ->
            Box(
                modifier = Modifier.height(72.dp).clip(RoundedCornerShape(10.dp))
                    .background(genres[i].second.copy(0.85f)).clickable {},
                contentAlignment = Alignment.BottomStart
            ) {
                Text(genres[i].first, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
            }
        }
    }
}

@Composable
private fun SearchLoadingContent() {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(8) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ShimmerBox(Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerBox(Modifier.fillMaxWidth(0.7f).height(14.dp).clip(RoundedCornerShape(4.dp)))
                    ShimmerBox(Modifier.fillMaxWidth(0.45f).height(11.dp).clip(RoundedCornerShape(4.dp)))
                }
            }
        }
    }
}

@Composable
private fun SearchResultsContent(
    result: SearchResult,
    onTrackClick: (Track) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
        if (result.tracks.isNotEmpty()) {
            item { SectionTitle("Songs") }
            items(result.tracks.size.coerceAtMost(5)) { i ->
                TrackListItem(result.tracks[i], onClick = { onTrackClick(result.tracks[i]) })
            }
        }
        if (result.artists.isNotEmpty()) {
            item { SectionTitle("Artists") }
            item {
                LazyHorizontalRow(items = result.artists) { artist ->
                    Column(
                        modifier = Modifier.width(96.dp).clickable { onArtistClick(artist.id) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AsyncImage(model = artist.imageUrl, contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(androidx.compose.foundation.shape.CircleShape),
                            contentScale = ContentScale.Crop)
                        Text(artist.name, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
        if (result.albums.isNotEmpty()) {
            item { SectionTitle("Albums") }
            items(result.albums.size.coerceAtMost(5)) { i ->
                val album = result.albums[i]
                ListItem(
                    headlineContent = { Text(album.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supportingContent = { Text("Album • ${album.artist}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) },
                    leadingContent = { AsyncImage(model = album.artUrl, contentDescription = null, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop) },
                    modifier = Modifier.clickable { onAlbumClick(album.id) }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
}

@Composable
fun TrackListItem(track: Track, onClick: () -> Unit, trailingContent: @Composable (() -> Unit)? = null) {
    ListItem(
        headlineContent = { Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text("${track.artist} • ${track.durationFormatted}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = {
            AsyncImage(model = track.albumArtUrl, contentDescription = null,
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        },
        trailingContent = trailingContent ?: { IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, null, modifier = Modifier.size(20.dp)) } },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun <T> LazyHorizontalRow(items: List<T>, content: @Composable (T) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items.size) { i -> content(items[i]) }
    }
}

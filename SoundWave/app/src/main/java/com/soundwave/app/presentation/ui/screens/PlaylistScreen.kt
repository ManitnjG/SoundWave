package com.soundwave.app.presentation.ui.playlist

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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.soundwave.app.presentation.ui.search.TrackListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToArtist: (String) -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    LaunchedEffect(playlistId) { viewModel.loadPlaylist(playlistId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(32.dp)) } },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Filled.Favorite, null) }
                    IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 160.dp)
        ) {
            item {
                Box(Modifier.fillMaxWidth().height(280.dp)) {
                    AsyncImage(model = uiState.playlist?.artUrl, contentDescription = null, modifier = Modifier.fillMaxSize().blur(20.dp).alpha(0.5f), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))))
                    Column(Modifier.align(Alignment.BottomStart).padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AsyncImage(model = uiState.playlist?.artUrl, contentDescription = null, modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)).shadow(16.dp, RoundedCornerShape(12.dp)))
                    }
                }
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(uiState.playlist?.name ?: "", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
                    Text(uiState.playlist?.ownerName ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${uiState.playlist?.trackCount ?: 0} songs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
                        Button(onClick = { viewModel.playAll(); onNavigateToPlayer() }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.PlayArrow, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Play")
                        }
                        OutlinedButton(onClick = { viewModel.shuffle(); onNavigateToPlayer() }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Shuffle, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Shuffle")
                        }
                    }
                }
            }
            items(uiState.tracks.size) { i ->
                TrackListItem(uiState.tracks[i], onClick = { viewModel.playFromIndex(i); onNavigateToPlayer() })
            }
        }
    }
}

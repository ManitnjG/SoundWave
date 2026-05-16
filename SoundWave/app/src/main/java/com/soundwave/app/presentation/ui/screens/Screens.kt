package com.soundwave.app.presentation.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.soundwave.app.core.audio.MusicPlayerController
import com.soundwave.app.domain.model.*
import com.soundwave.app.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val playlists: List<Playlist> = emptyList(),
    val favoritePlaylists: List<Playlist> = emptyList(),
    val favoriteTracks: List<Track> = emptyList(),
    val favoriteAlbums: List<Album> = emptyList(),
    val followedArtists: List<Artist> = emptyList()
)

@UnstableApi
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playerController: MusicPlayerController
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                musicRepository.getUserPlaylists(),
                musicRepository.getFavoritePlaylists(),
                musicRepository.getFavoriteTracks()
            ) { playlists, favPl, favTracks ->
                LibraryUiState(playlists = playlists, favoritePlaylists = favPl, favoriteTracks = favTracks)
            }.collect { _uiState.value = it }
        }
    }

    fun playTrack(track: Track, queue: List<Track>) {
        playerController.playTrack(track, queue, queue.indexOf(track).coerceAtLeast(0))
    }

    fun createNewPlaylist() {
        viewModelScope.launch { musicRepository.createPlaylist("New Playlist ${System.currentTimeMillis() % 1000}", "") }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

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
                        AsyncImage(model = uiState.playlist?.artUrl, contentDescription = null, modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)).shadow(16.dp, RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
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

// ─────────────────────────────────────────────────────────────────────────────

package com.soundwave.app.presentation.ui.downloads

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soundwave.app.presentation.ui.search.TrackListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onNavigateToPlayer: () -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 160.dp)) {
            if (uiState.downloads.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(64.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Filled.Download, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("No downloads yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Downloaded songs play without internet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(uiState.downloads.size) { i ->
                    TrackListItem(uiState.downloads[i], onClick = { viewModel.play(uiState.downloads[i], uiState.downloads); onNavigateToPlayer() })
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

package com.soundwave.app.presentation.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soundwave.app.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {
            item { SettingsSectionHeader("Playback") }
            item {
                SettingsDropdown("Streaming Quality", prefs?.streamingQuality?.label ?: "High", AudioQuality.values().map { it.label }, AudioQuality.values().indexOf(prefs?.streamingQuality)) {
                    viewModel.updateStreamingQuality(AudioQuality.values()[it])
                }
            }
            item { SettingsToggle("Normalize Volume", prefs?.normalizationEnabled ?: true) { viewModel.updateNormalization(it) } }
            item { SettingsToggle("Skip Silence", prefs?.skipSilence ?: false) { viewModel.updateSkipSilence(it) } }
            item { SettingsSectionHeader("Appearance") }
            item {
                SettingsDropdown("Theme", prefs?.themeMode?.name ?: "DARK", ThemeMode.values().map { it.name }, ThemeMode.values().indexOf(prefs?.themeMode)) {
                    viewModel.updateThemeMode(ThemeMode.values()[it])
                }
            }
            item { SettingsToggle("Dynamic Color (Material You)", prefs?.dynamicColor ?: true) { viewModel.updateDynamicColor(it) } }
            item { SettingsSectionHeader("Downloads") }
            item { SettingsToggle("Download on Wi-Fi Only", prefs?.downloadOnWifiOnly ?: true) { viewModel.updateDownloadOnWifiOnly(it) } }
            item { SettingsSectionHeader("Lyrics") }
            item { SettingsToggle("Show Lyrics", prefs?.lyricsEnabled ?: true) { viewModel.updateLyricsEnabled(it) } }
            item { SettingsSectionHeader("Visualizer") }
            item { SettingsToggle("Audio Visualizer", prefs?.visualizerEnabled ?: true) { viewModel.updateVisualizerEnabled(it) } }
            item { SettingsSectionHeader("Content") }
            item { SettingsToggle("Show Explicit Content", prefs?.showExplicit ?: true) { viewModel.updateShowExplicit(it) } }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(title, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).padding(top = 8.dp),
        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
}

@Composable
private fun SettingsToggle(title: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onChanged) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun SettingsDropdown(title: String, current: String, options: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(current, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = {
            Box {
                IconButton(onClick = { expanded = true }) { Icon(Icons.Filled.ExpandMore, null) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEachIndexed { i, option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(i); expanded = false })
                    }
                }
            }
        },
        modifier = Modifier.clickable { expanded = true },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(onNavigateBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equalizer", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Equalizer", style = MaterialTheme.typography.titleMedium)
                Switch(checked = prefs?.equalizerEnabled ?: false, onCheckedChange = { viewModel.updateEqualizerEnabled(it) })
            }
            // Preset picker
            ScrollableTabRow(selectedTabIndex = EqualizerPreset.values().indexOf(prefs?.equalizerPreset ?: EqualizerPreset.FLAT), edgePadding = 0.dp, containerColor = Color.Transparent) {
                EqualizerPreset.values().forEachIndexed { i, preset ->
                    Tab(selected = prefs?.equalizerPreset == preset, onClick = { viewModel.updateEqualizerPreset(preset) },
                        text = { Text(preset.displayName, style = MaterialTheme.typography.labelSmall) })
                }
            }
            // Band sliders
            val bands = prefs?.equalizerBands ?: EqualizerPreset.FLAT.bands
            Row(Modifier.fillMaxWidth().height(200.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                bands.forEachIndexed { i, band ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("+${band.gainDb.toInt()}dB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Slider(value = (band.gainDb + 15f) / 30f, onValueChange = { v ->
                            val newBands = bands.toMutableList()
                            newBands[i] = band.copy(gainDb = v * 30f - 15f)
                            viewModel.updateEqualizerBands(newBands)
                        }, modifier = Modifier.height(160.dp), orientation = false)
                        Text("${if (band.centerFrequency >= 1000) "${band.centerFrequency/1000}k" else "${band.centerFrequency}"}Hz",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private val Boolean.not get() = !this

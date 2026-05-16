package com.soundwave.app.presentation.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.soundwave.app.domain.model.*
import com.soundwave.app.presentation.ui.components.ShimmerBox
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToLyrics: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // Album art rotation animation when playing
    val rotationAnim = rememberInfiniteTransition(label = "art_rotation")
    val artRotation by rotationAnim.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "rotation"
    )

    // Scale animation for album art
    val artScale by animateFloatAsState(
        targetValue = if (uiState.playbackState.isPlaying) 1f else 0.88f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
        label = "art_scale"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // Background blurred art
        uiState.currentTrack?.let { track ->
            AsyncImage(
                model = track.albumArtUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(80.dp).alpha(0.35f),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background.copy(0.5f),
                            MaterialTheme.colorScheme.background.copy(0.85f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().navigationBarsPadding().statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            PlayerTopBar(
                onBack = onNavigateBack,
                onQueue = onNavigateToQueue,
                onMore = {}
            )

            Spacer(Modifier.height(24.dp))

            // Album Art
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .graphicsLayer {
                        scaleX = artScale
                        scaleY = artScale
                        shadowElevation = 40.dp.toPx()
                        shape = CircleShape
                        clip = false
                    },
                contentAlignment = Alignment.Center
            ) {
                // Glow ring
                Box(
                    modifier = Modifier.size(316.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                AsyncImage(
                    model = uiState.currentTrack?.albumArtUrl,
                    contentDescription = "Album Art",
                    modifier = Modifier.size(300.dp).clip(CircleShape)
                        .graphicsLayer {
                            if (uiState.playbackState.isPlaying) rotationZ = artRotation
                        }
                        .shadow(24.dp, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(32.dp))

            // Track info + favorite
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = uiState.currentTrack?.title ?: "",
                        transitionSpec = {
                            slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                        },
                        label = "track_title"
                    ) { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    AnimatedContent(
                        targetState = uiState.currentTrack?.artist ?: "",
                        transitionSpec = {
                            slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                        },
                        label = "track_artist"
                    ) { artist ->
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleFavorite()
                    }
                ) {
                    Icon(
                        imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (uiState.isFavorite) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Waveform / Progress slider
            WaveformSeekbar(
                progress = uiState.playbackState.progress,
                bufferedProgress = uiState.playbackState.bufferedProgress,
                onSeek = viewModel::seekTo,
                currentPosition = uiState.currentPosition,
                duration = uiState.playbackState.durationMs,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Main controls
            PlayerControls(
                isPlaying = uiState.playbackState.isPlaying,
                isBuffering = uiState.playbackState.isBuffering,
                shuffleEnabled = uiState.shuffleEnabled,
                repeatMode = uiState.repeatMode,
                onPlayPause = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.togglePlayPause() },
                onSkipNext = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.skipToNext() },
                onSkipPrevious = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.skipToPrevious() },
                onShuffle = viewModel::toggleShuffle,
                onRepeat = viewModel::toggleRepeat,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Extra controls row
            ExtraControls(
                onLyrics = onNavigateToLyrics,
                onEqualizer = onNavigateToEqualizer,
                onAddToPlaylist = {},
                onShare = {}
            )
        }
    }
}

@Composable
private fun PlayerTopBar(onBack: () -> Unit, onQueue: () -> Unit, onMore: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.KeyboardArrowDown, "Back", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(32.dp))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PLAYING FROM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
            Text("Your Library", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
        }
        Row {
            IconButton(onClick = onQueue) {
                Icon(Icons.Filled.QueueMusic, "Queue", tint = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = onMore) {
                Icon(Icons.Filled.MoreVert, "More", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun WaveformSeekbar(
    progress: Float,
    bufferedProgress: Float,
    onSeek: (Float) -> Unit,
    currentPosition: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Slider(
            value = progress,
            onValueChange = onSeek,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            thumb = {
                Box(
                    modifier = Modifier.size(14.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground)
                        .shadow(4.dp, CircleShape)
                )
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatDuration(currentPosition), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatDuration(duration), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    isBuffering: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Shuffle
        IconButton(onClick = onShuffle) {
            Icon(
                Icons.Filled.Shuffle, "Shuffle",
                tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
        }

        // Previous
        IconButton(onClick = onSkipPrevious, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Filled.SkipPrevious, "Previous", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(40.dp))
        }

        // Play/Pause
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.onBackground)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center
        ) {
            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.background,
                    strokeWidth = 3.dp
                )
            } else {
                AnimatedContent(
                    targetState = isPlaying,
                    transitionSpec = { scaleIn(tween(150)) togetherWith scaleOut(tween(150)) },
                    label = "play_icon"
                ) { playing ->
                    Icon(
                        imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playing) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // Next
        IconButton(onClick = onSkipNext, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Filled.SkipNext, "Next", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(40.dp))
        }

        // Repeat
        IconButton(onClick = onRepeat) {
            Icon(
                imageVector = when (repeatMode) {
                    RepeatMode.ONE -> Icons.Filled.RepeatOne
                    else -> Icons.Filled.Repeat
                },
                contentDescription = "Repeat",
                tint = if (repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun ExtraControls(onLyrics: () -> Unit, onEqualizer: () -> Unit, onAddToPlaylist: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onLyrics) {
            Icon(Icons.Filled.Lyrics, "Lyrics", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onEqualizer) {
            Icon(Icons.Filled.Equalizer, "Equalizer", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onAddToPlaylist) {
            Icon(Icons.Filled.PlaylistAdd, "Add to Playlist", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Filled.Share, "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QueueScreen(onNavigateBack: () -> Unit, viewModel: PlayerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Queue") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.Close, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            itemsIndexed(uiState.queue) { index, track ->
                val isCurrent = index == uiState.queueIndex
                ListItem(
                    headlineContent = { Text(track.title, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal, color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground) },
                    supportingContent = { Text(track.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingContent = {
                        AsyncImage(model = track.albumArtUrl, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                    },
                    trailingContent = {
                        if (isCurrent) Icon(Icons.Filled.VolumeUp, null, tint = MaterialTheme.colorScheme.primary)
                        else IconButton(onClick = { viewModel.removeFromQueue(index) }) { Icon(Icons.Filled.Close, "Remove", modifier = Modifier.size(18.dp)) }
                    },
                    modifier = Modifier.clickable { viewModel.skipToQueueItem(index) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
            }
        }
    }
}

@Composable
fun LyricsScreen(onNavigateBack: () -> Unit, viewModel: PlayerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val currentLineIndex = uiState.currentLyricLine

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0) {
            listState.animateScrollToItem(maxOf(0, currentLineIndex - 3))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lyrics") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.KeyboardArrowDown, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            uiState.currentTrack?.let { track ->
                AsyncImage(model = track.albumArtUrl, contentDescription = null, modifier = Modifier.fillMaxSize().blur(60.dp).alpha(0.4f), contentScale = ContentScale.Crop)
            }
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.6f), Color.Black.copy(0.8f)))))

            if (uiState.lyrics == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (uiState.lyricsLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    else Text("No lyrics available", color = Color.White.copy(0.5f), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    itemsIndexed(uiState.lyrics.lines) { index, line ->
                        val isCurrent = index == currentLineIndex
                        val textAlpha by animateFloatAsState(
                            targetValue = if (isCurrent) 1f else 0.35f,
                            animationSpec = tween(300),
                            label = "lyric_alpha"
                        )
                        val textSize by animateFloatAsState(
                            targetValue = if (isCurrent) 26f else 20f,
                            animationSpec = spring(dampingRatio = 0.8f),
                            label = "lyric_size"
                        )
                        Text(
                            text = line.text,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = textSize.sp
                            ),
                            color = Color.White.copy(alpha = textAlpha),
                            modifier = Modifier.clickable {
                                if (uiState.lyrics.isSynced) viewModel.seekTo(line.timestampMs.toFloat() / (uiState.playbackState.durationMs))
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "$min:${sec.toString().padStart(2, '0')}"
}

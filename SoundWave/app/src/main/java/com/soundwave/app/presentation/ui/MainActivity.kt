package com.soundwave.app.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soundwave.app.domain.model.ThemeMode
import com.soundwave.app.presentation.navigation.*
import com.soundwave.app.presentation.ui.components.MiniPlayer
import com.soundwave.app.presentation.ui.theme.SoundWaveTheme
import com.soundwave.app.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Keep splash until preferences loaded
            splashScreen.setKeepOnScreenCondition { !uiState.isReady }

            SoundWaveTheme(
                darkTheme = uiState.themeMode != ThemeMode.LIGHT,
                amoledMode = uiState.themeMode == ThemeMode.AMOLED,
                dynamicColor = uiState.dynamicColor
            ) {
                SoundWaveApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundWaveApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val showBottomBar = currentDestination?.route !in listOf(
        Screen.Player.route, Screen.Queue.route, Screen.Lyrics.route, Screen.Equalizer.route
    )
    val showMiniPlayer = uiState.currentTrack != null &&
            currentDestination?.route != Screen.Player.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    AnimatedVisibility(
                        visible = showMiniPlayer,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        MiniPlayer(
                            track = uiState.currentTrack!!,
                            playbackState = uiState.playbackState,
                            onPlayPause = viewModel::togglePlayPause,
                            onSkipNext = viewModel::skipToNext,
                            onClick = { navController.navigate(Screen.Player.route) },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    SoundWaveBottomNav(
                        navController = navController,
                        currentDestination = currentDestination
                    )
                }
            }
        }
    ) { paddingValues ->
        SoundWaveNavGraph(navController = navController)
    }
}

data class BottomNavItemDef(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val navItems = listOf(
    BottomNavItemDef(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItemDef(Screen.Search.route, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    BottomNavItemDef(Screen.Library.route, "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    BottomNavItemDef(Screen.Downloads.route, "Downloads", Icons.Filled.Download, Icons.Outlined.Download)
)

@Composable
fun SoundWaveBottomNav(
    navController: androidx.navigation.NavController,
    currentDestination: androidx.navigation.NavDestination?
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        navItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}

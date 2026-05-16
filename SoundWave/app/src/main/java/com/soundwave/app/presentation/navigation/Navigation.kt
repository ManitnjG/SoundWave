package com.soundwave.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.*
import androidx.navigation.compose.*
import com.soundwave.app.presentation.ui.home.HomeScreen
import com.soundwave.app.presentation.ui.search.SearchScreen
import com.soundwave.app.presentation.ui.library.LibraryScreen
import com.soundwave.app.presentation.ui.player.PlayerScreen
import com.soundwave.app.presentation.ui.playlist.PlaylistScreen
import com.soundwave.app.presentation.ui.downloads.DownloadsScreen
import com.soundwave.app.presentation.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object Downloads : Screen("downloads")
    object Settings : Screen("settings")
    object Player : Screen("player")
    object Playlist : Screen("playlist/{playlistId}") {
        fun createRoute(id: String) = "playlist/$id"
    }
    object Album : Screen("album/{albumId}") {
        fun createRoute(id: String) = "album/$id"
    }
    object Artist : Screen("artist/{artistId}") {
        fun createRoute(id: String) = "artist/$id"
    }
    object Equalizer : Screen("equalizer")
    object Queue : Screen("queue")
    object Lyrics : Screen("lyrics")
}

sealed class BottomNavItem(val route: String, val label: String, val iconResId: Int) {
    object Home : BottomNavItem(Screen.Home.route, "Home", 0)
    object Search : BottomNavItem(Screen.Search.route, "Search", 0)
    object Library : BottomNavItem(Screen.Library.route, "Your Library", 0)
    object Downloads : BottomNavItem(Screen.Downloads.route, "Downloads", 0)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Search,
    BottomNavItem.Library,
    BottomNavItem.Downloads
)

// Smooth slide + fade transitions
private val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIn(
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        initialOffset = { IntOffset(it.width / 8, 0) }
    ) + fadeIn(animationSpec = tween(280))
}

private val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOut(
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        targetOffset = { IntOffset(-it.width / 8, 0) }
    ) + fadeOut(animationSpec = tween(200))
}

private val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIn(
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        initialOffset = { IntOffset(-it.width / 8, 0) }
    ) + fadeIn(animationSpec = tween(280))
}

private val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOut(
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        targetOffset = { IntOffset(it.width / 8, 0) }
    ) + fadeOut(animationSpec = tween(200))
}

@Composable
fun SoundWaveNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                onNavigateToPlaylist = { id -> navController.navigate(Screen.Playlist.createRoute(id)) },
                onNavigateToAlbum = { id -> navController.navigate(Screen.Album.createRoute(id)) },
                onNavigateToArtist = { id -> navController.navigate(Screen.Artist.createRoute(id)) }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                onNavigateToAlbum = { id -> navController.navigate(Screen.Album.createRoute(id)) },
                onNavigateToArtist = { id -> navController.navigate(Screen.Artist.createRoute(id)) },
                onNavigateToPlaylist = { id -> navController.navigate(Screen.Playlist.createRoute(id)) }
            )
        }
        composable(Screen.Library.route) {
            LibraryScreen(
                onNavigateToPlaylist = { id -> navController.navigate(Screen.Playlist.createRoute(id)) },
                onNavigateToPlayer = { navController.navigate(Screen.Player.route) }
            )
        }
        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onNavigateToPlayer = { navController.navigate(Screen.Player.route) }
            )
        }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.Player.route) {
            PlayerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQueue = { navController.navigate(Screen.Queue.route) },
                onNavigateToLyrics = { navController.navigate(Screen.Lyrics.route) },
                onNavigateToEqualizer = { navController.navigate(Screen.Equalizer.route) }
            )
        }
        composable(
            route = Screen.Playlist.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStack ->
            val playlistId = backStack.arguments?.getString("playlistId") ?: return@composable
            PlaylistScreen(
                playlistId = playlistId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                onNavigateToArtist = { id -> navController.navigate(Screen.Artist.createRoute(id)) }
            )
        }
        composable(Screen.Queue.route) {
            com.soundwave.app.presentation.ui.player.QueueScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Lyrics.route) {
            com.soundwave.app.presentation.ui.player.LyricsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Equalizer.route) {
            com.soundwave.app.presentation.ui.settings.EqualizerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.LetterPickMode
import com.example.ui.screens.CheatSheetScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.HomeSetupScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoundResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.EsmFamilTheme
import com.example.viewmodel.EsmFamilViewModel
import kotlinx.coroutines.launch

enum class MainTab(
    val route: String,
    val titleFa: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("home", "بازی", Icons.Rounded.Home, Icons.Outlined.Home),
    CHEAT_SHEET("cheat_sheet", "تقلب", Icons.Rounded.AutoStories, Icons.Outlined.AutoStories),
    PROFILE("profile", "پروفایل", Icons.Rounded.Person, Icons.Outlined.Person),
    GUIDE("guide", "راهنما", Icons.AutoMirrored.Rounded.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    SETTINGS("settings", "تنظیمات", Icons.Rounded.Settings, Icons.Outlined.Settings),
}

@Composable
fun EsmFamilApp(
    viewModel: EsmFamilViewModel,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val activeRound by viewModel.activeRound.collectAsStateWithLifecycle()
    val roundResult by viewModel.currentRoundResult.collectAsStateWithLifecycle()
    val matchSummary by viewModel.matchSummary.collectAsStateWithLifecycle()

    EsmFamilTheme(themeMode = settings.themeMode) {
        when {
            activeRound != null -> {
                val round = activeRound!!
                GamePlayScreen(
                    roundState = round,
                    soundEnabled = settings.soundEnabled,
                    onToggleSound = viewModel::toggleSound,
                    onAnswerChange = viewModel::updateAnswer,
                    onSubmitRound = viewModel::submitCurrentRound,
                    onExitGame = viewModel::abandonActiveGame,
                )
            }

            roundResult != null -> {
                val result = roundResult!!
                val totalRounds = if (settings.letterPickMode == LetterPickMode.MANUAL_CHOICE) {
                    1
                } else {
                    settings.roundsCount
                }
                RoundResultScreen(
                    roundResult = result,
                    totalRounds = totalRounds,
                    matchSummary = matchSummary,
                    onToggleValidity = viewModel::toggleCategoryValidity,
                    onNextRound = viewModel::startNextRound,
                    onPlayAgain = viewModel::startNewMatch,
                    onReturnHome = viewModel::abandonActiveGame,
                )
            }

            else -> {
                MainHubScaffold(
                    viewModel = viewModel,
                    settings = settings,
                    stats = stats,
                )
            }
        }
    }
}

@Composable
private fun MainHubScaffold(
    viewModel: EsmFamilViewModel,
    settings: com.example.model.GameSettings,
    stats: com.example.model.AppStats,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MainTab.HOME.route

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun navigateToTab(tab: MainTab) {
        viewModel.soundManager.playTap()
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar {
                        MainTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.titleFa,
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.titleFa,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_${tab.route}"),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding(),
            ) {
                if (useNavigationRail) {
                    NavigationRail {
                        MainTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.titleFa,
                                    )
                                },
                                label = { Text(tab.titleFa) },
                                modifier = Modifier.testTag("nav_rail_${tab.route}"),
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = MainTab.HOME.route,
                    ) {
                        composable(MainTab.HOME.route) {
                            HomeSetupScreen(
                                settings = settings,
                                stats = stats,
                                onToggleSound = viewModel::toggleSound,
                                onSetSoundEnabled = viewModel::setSoundEnabled,
                                onToggleLetter = viewModel::toggleLetterSelection,
                                onSelectAllLetters = viewModel::selectAllLetters,
                                onSelectPopularLetters = viewModel::selectPopularLetters,
                                onSetLetterPickMode = viewModel::setLetterPickMode,
                                onSetManualLetter = viewModel::setManualLetter,
                                onSetTimerSeconds = viewModel::setTimerSeconds,
                                onSetRoundsCount = viewModel::setRoundsCount,
                                onSetBotDifficulty = viewModel::setBotDifficulty,
                                onStartGame = viewModel::startNewMatch,
                                onOpenCheatSheet = { navigateToTab(MainTab.CHEAT_SHEET) },
                                onOpenGuide = { navigateToTab(MainTab.GUIDE) },
                                onOpenProfile = { navigateToTab(MainTab.PROFILE) },
                            )
                        }
                        composable(MainTab.CHEAT_SHEET.route) {
                            CheatSheetScreen(
                                onShowMessage = { msg ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                },
                                onPlayTapSound = { viewModel.soundManager.playTap() },
                            )
                        }
                        composable(MainTab.PROFILE.route) {
                            ProfileScreen(
                                stats = stats,
                                onUpdateProfile = viewModel::updateProfile,
                            )
                        }
                        composable(MainTab.GUIDE.route) {
                            GuideScreen()
                        }
                        composable(MainTab.SETTINGS.route) {
                            SettingsScreen(
                                settings = settings,
                                stats = stats,
                                onSetSoundEnabled = viewModel::setSoundEnabled,
                                onTestSound = { viewModel.soundManager.playVictoryChime() },
                                onSetThemeMode = viewModel::setThemeMode,
                                onSetTimerSeconds = viewModel::setTimerSeconds,
                                onSetBotDifficulty = viewModel::setBotDifficulty,
                                onResetStats = viewModel::resetStats,
                            )
                        }
                    }
                }
            }
        }
    }
}

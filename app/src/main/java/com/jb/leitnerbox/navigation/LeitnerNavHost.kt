package com.jb.leitnerbox.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.hilt.navigation.compose.hiltViewModel
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.feature.decks.ui.list.DeckListScreen
import com.jb.leitnerbox.feature.decks.ui.list.DeckListViewModel
import com.jb.leitnerbox.feature.decks.ui.edit.DeckEditScreen
import com.jb.leitnerbox.feature.decks.ui.edit.DeckEditViewModel
import com.jb.leitnerbox.feature.decks.ui.detail.DeckDetailScreen
import com.jb.leitnerbox.feature.decks.ui.detail.DeckDetailViewModel
import com.jb.leitnerbox.feature.decks.ui.detail.BoxDetailScreen
import com.jb.leitnerbox.feature.decks.ui.detail.BoxDetailViewModel
import com.jb.leitnerbox.feature.dashboard.ui.DashboardScreen
import com.jb.leitnerbox.feature.dashboard.ui.DashboardViewModel
import com.jb.leitnerbox.feature.settings.ui.SettingsViewModel
import com.jb.leitnerbox.feature.settings.ui.SettingsScreen
import com.jb.leitnerbox.feature.cards.ui.edit.CardEditScreen
import com.jb.leitnerbox.feature.cards.ui.edit.CardEditViewModel
import com.jb.leitnerbox.feature.cards.ui.edit.CardUpdateScreen
import com.jb.leitnerbox.feature.cards.ui.edit.CardUpdateViewModel
import com.jb.leitnerbox.feature.session.selection.SessionSelectionScreen
import com.jb.leitnerbox.feature.session.ui.SessionScreen
import com.jb.leitnerbox.feature.session.ui.Session2Screen
import com.jb.leitnerbox.feature.session.ui.extra.ExtraSessionScreen
import com.jb.leitnerbox.feature.session.ui.extra.ExtraSession2Screen
import com.jb.leitnerbox.feature.session.ui.result.SessionResultScreen
import com.jb.leitnerbox.feature.session.ui.result.SessionResultViewModel
import com.jb.leitnerbox.feature.stats.ui.StatsScreen
import com.jb.leitnerbox.feature.stats.ui.HistoryScreen
import com.jb.leitnerbox.feature.challenge.ui.ChallengeScreen
import com.jb.leitnerbox.feature.settings.ui.ExcludedDaysScreen
import com.jb.leitnerbox.feature.settings.ui.ThemeScreen
import com.jb.leitnerbox.feature.settings.ui.BackupScreen
import com.jb.leitnerbox.feature.settings.ui.CsvExportScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun LeitnerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var lastDeletedDeck by remember { mutableStateOf<Deck?>(null) }
    var lastDeletedCards by remember { mutableStateOf<List<Card>>(emptyList()) }

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToSessionSelection = {
                    navController.navigate(Screen.SessionSelection.route)
                },
                onNavigateToDecks = {
                    navController.navigate(Screen.Decks.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToChallenge = {
                    navController.navigate(Screen.Challenge.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Decks.route) {
            val viewModel: DeckListViewModel = hiltViewModel()
            DeckListScreen(
                viewModel = viewModel,
                onDeckClick = { deckId -> 
                    navController.navigate(Screen.DeckDetail.createRoute(deckId))
                },
                onAddDeckClick = { 
                    navController.navigate(Screen.DeckEdit.createRoute())
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onLaunchExtraSession = { deckId, mode ->
                    val route = if (mode == 2) {
                        Screen.ExtraSession2.createRoute(deckId)
                    } else {
                        Screen.ExtraSession.createRoute(deckId)
                    }
                    navController.navigate(route)
                },
                deletedDeck = lastDeletedDeck,
                onUndoDelete = { deck ->
                    viewModel.undoDelete(deck, lastDeletedCards)
                    lastDeletedDeck = null
                    lastDeletedCards = emptyList()
                },
                onSnackbarDismissed = {
                    viewModel.onDeleteConfirmed()
                    lastDeletedDeck = null
                    lastDeletedCards = emptyList()
                }
            )
        }
        composable(
            route = Screen.DeckEdit.route,
            arguments = listOf(
                navArgument("deckId") { 
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            val viewModel: DeckEditViewModel = hiltViewModel()
            DeckEditScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onDeckSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.DeckDetail.route,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) {
            val viewModel: DeckDetailViewModel = hiltViewModel()
            DeckDetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onAddCardClick = { deckId ->
                    navController.navigate(Screen.CardEdit.createRoute(deckId))
                },
                onEditDeckClick = { deckId ->
                    navController.navigate(Screen.DeckEdit.createRoute(deckId))
                },
                onBoxClick = { deckId, boxIndex ->
                    navController.navigate(Screen.BoxDetail.createRoute(deckId, boxIndex))
                },
                onDeckDeleted = { deck, cards ->
                    lastDeletedDeck = deck
                    lastDeletedCards = cards
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.CardEdit.route,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) {
            val viewModel: CardEditViewModel = hiltViewModel()
            CardEditScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onCardSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.BoxDetail.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("boxIndex") { type = NavType.IntType }
            )
        ) {
            val viewModel: BoxDetailViewModel = hiltViewModel()
            BoxDetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onEditCardClick = { cardId ->
                    navController.navigate(Screen.CardUpdate.createRoute(cardId))
                }
            )
        }
        composable(
            route = Screen.CardUpdate.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) {
            val viewModel: CardUpdateViewModel = hiltViewModel()
            CardUpdateScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onCardSaved = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToExcludedDays = {
                    navController.navigate(Screen.ExcludedDays.route)
                },
                onNavigateToTheme = {
                    navController.navigate(Screen.Theme.route)
                },
                onNavigateToBackup = {
                    navController.navigate(Screen.Backup.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onRestoreSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.SessionSelection.route) {
            SessionSelectionScreen(
                onStartSession = { mode ->
                    val route = if (mode == 2) Screen.Session2.route else Screen.Session.route
                    navController.navigate(route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Session.route) {
            SessionScreen(
                onSessionFinished = {
                    navController.navigate(Screen.SessionResult.route) {
                        popUpTo(Screen.SessionSelection.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Session2.route) {
            Session2Screen(
                onSessionFinished = {
                    navController.navigate(Screen.SessionResult.route) {
                        popUpTo(Screen.SessionSelection.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.SessionResult.route) {
            val viewModel: SessionResultViewModel = hiltViewModel()
            SessionResultScreen(
                viewModel = viewModel,
                onFinish = {
                    navController.navigate(viewModel.navigationTarget) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                }
            )
        }
        composable(Screen.Stats.route) {
            StatsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Challenge.route) {
            ChallengeScreen(
                onStartChallenge = { mode ->
                    val route = if (mode == 2) Screen.Session2.route else Screen.Session.route
                    navController.navigate(route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.ExcludedDays.route) {
            val settingsEntry = remember {
                navController.getBackStackEntry(Screen.Settings.route)
            }
            val settingsViewModel = hiltViewModel<SettingsViewModel>(settingsEntry)

            ExcludedDaysScreen(
                onBackClick = {
                    settingsViewModel.onReturnFromExcludedDays()
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Theme.route) {
            ThemeScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Backup.route) {
            BackupScreen(
                onNavigateToCsvExport = {
                    navController.navigate(Screen.CsvExport.route)
                },
                onBackClick = { navController.popBackStack() },
                onImportSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRestoreSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.CsvExport.route) {
            CsvExportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route     = Screen.ExtraSession.route,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) {
            ExtraSessionScreen(
                onSessionFinished = {
                    navController.navigate(Screen.SessionResult.route) {
                        popUpTo(Screen.ExtraSession.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route     = Screen.ExtraSession2.route,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) {
            ExtraSession2Screen(
                onSessionFinished = {
                    navController.navigate(Screen.SessionResult.route) {
                        popUpTo(Screen.ExtraSession2.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

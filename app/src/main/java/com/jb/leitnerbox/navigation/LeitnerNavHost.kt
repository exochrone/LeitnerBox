package com.jb.leitnerbox.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.hilt.navigation.compose.hiltViewModel
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
import com.jb.leitnerbox.feature.settings.ui.SettingsScreen
import com.jb.leitnerbox.feature.cards.ui.edit.CardEditScreen
import com.jb.leitnerbox.feature.cards.ui.edit.CardEditViewModel
import com.jb.leitnerbox.feature.cards.ui.edit.CardUpdateScreen
import com.jb.leitnerbox.feature.cards.ui.edit.CardUpdateViewModel
import com.jb.leitnerbox.feature.session.selection.SessionSelectionScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun LeitnerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var lastDeletedDeck by remember { mutableStateOf<Deck?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
        composable(Screen.Decks.route) {
            val viewModel: DeckListViewModel = hiltViewModel()
            DeckListScreen(
                viewModel = viewModel,
                onDeckClick = { deckId -> 
                    navController.navigate(Screen.DeckDetail.createRoute(deckId))
                },
                onAddDeckClick = { 
                    navController.navigate(Screen.DeckEdit.route)
                },
                deletedDeck = lastDeletedDeck,
                onUndoDelete = { deck ->
                    viewModel.undoDelete(deck)
                    lastDeletedDeck = null
                }
            )
        }
        composable(Screen.DeckEdit.route) {
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
                onBoxClick = { deckId, boxIndex ->
                    navController.navigate(Screen.BoxDetail.createRoute(deckId, boxIndex))
                },
                onDeckDeleted = { deck ->
                    lastDeletedDeck = deck
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
            SettingsScreen()
        }
        composable(Screen.SessionSelection.route) {
            SessionSelectionScreen(
                onStartSession = {
                    // TODO: US-03/US-05 Navigate to Session
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
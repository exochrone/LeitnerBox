package com.jb.leitnerbox.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Decks : Screen("decks")
    object Settings : Screen("settings")
    object DeckEdit : Screen("deck_edit")
    object CardEdit : Screen("card_edit/{deckId}") {
        fun createRoute(deckId: Long) = "card_edit/$deckId"
    }
    object DeckDetail : Screen("deck_detail/{deckId}") {
        fun createRoute(deckId: Long) = "deck_detail/$deckId"
    }
    object BoxDetail : Screen("box_detail/{deckId}/{boxIndex}") {
        fun createRoute(deckId: Long, boxIndex: Int) = "box_detail/$deckId/$boxIndex"
    }
    object CardUpdate : Screen("card_update/{cardId}") {
        fun createRoute(cardId: Long) = "card_update/$cardId"
    }
    object SessionSelection : Screen("session/selection")
}
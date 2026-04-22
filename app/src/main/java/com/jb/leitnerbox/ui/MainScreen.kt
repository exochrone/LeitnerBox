package com.jb.leitnerbox.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.jb.leitnerbox.R
import com.jb.leitnerbox.feature.decks.ui.list.DeckListScreen
import com.jb.leitnerbox.feature.decks.ui.list.DeckListViewModel
import com.jb.leitnerbox.navigation.LeitnerNavHost
import com.jb.leitnerbox.navigation.Screen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Dashboard to Icons.Default.Home,
        Screen.Decks to Icons.Default.List,
        Screen.Settings to Icons.Default.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { (screen, icon) ->
                    val label = when(screen) {
                        Screen.Dashboard -> stringResource(R.string.nav_dashboard)
                        Screen.Decks -> stringResource(R.string.nav_decks)
                        Screen.Settings -> stringResource(R.string.nav_settings)
                        else -> screen.route
                    }
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                    if (screen == Screen.Decks) {
                                        inclusive = true
                                    }
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LeitnerNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
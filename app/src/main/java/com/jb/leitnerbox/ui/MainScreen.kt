package com.jb.leitnerbox.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.jb.leitnerbox.navigation.LeitnerNavHost
import com.jb.leitnerbox.navigation.Screen

@Composable
fun MainScreen(startDestination: String? = null) {
    val navController = rememberNavController()

    LaunchedEffect(startDestination) {
        if (startDestination == Screen.SessionSelection.route) {
            navController.navigate(Screen.SessionSelection.route)
        }
    }

    Scaffold { innerPadding ->
        LeitnerNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

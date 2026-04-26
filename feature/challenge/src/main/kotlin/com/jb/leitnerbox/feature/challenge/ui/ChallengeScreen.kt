package com.jb.leitnerbox.feature.challenge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel = hiltViewModel(),
    onStartChallenge: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Challenge") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "Challenge des Maîtres",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Révisez vos ${uiState.masteredCount} cartes maîtrisées pour ne jamais les oublier !",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = { viewModel.startChallenge(onStartChallenge) },
                enabled = uiState.masteredCount > 0 && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("DÉMARRER")
            }
        }
    }
}

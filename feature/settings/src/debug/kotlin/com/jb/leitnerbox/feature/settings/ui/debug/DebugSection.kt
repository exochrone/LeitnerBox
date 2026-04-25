package com.jb.leitnerbox.feature.settings.ui.debug

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DebugSection(
    viewModel: DebugViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.message.collect { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
        }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

    Text(
        text = "🛠 DEBUG",
        style = MaterialTheme.typography.labelLarge,
        color = Color.Red,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    DebugSectionTitle("Jeux de données")

    DebugButton("➕ Noms très longs") { viewModel.seedLongNames() }
    DebugButton("➕ Circulation des boîtes (3 cartes)") { viewModel.seedBoxCirculation() }
    DebugButton("➕ Test de maîtrise (30 cartes)") { viewModel.seedMasteryTest() }
    DebugButton("🗑 Supprimer tous les [TEST]", isDestructive = true) {
        viewModel.cleanTestData()
    }

    DebugSectionTitle("Simulation du temps")

    Text(
        text = "Avance la nextReviewDate de toutes les cartes [TEST] dans le passé.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { viewModel.advanceOneDay() },
            modifier = Modifier.weight(1f)
        ) { Text("⏩ +1 jour") }

        Button(
            onClick = { viewModel.advanceSevenDays() },
            modifier = Modifier.weight(1f)
        ) { Text("⏩ +7 jours") }
    }

    SnackbarHost(hostState = snackbarHostState)
}

@Composable
private fun DebugSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun DebugButton(
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
        )
    }
}

package com.jb.leitnerbox.feature.session.ui.result

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jb.leitnerbox.core.ui.components.CelebrationOverlay
import com.jb.leitnerbox.core.ui.components.CelebrationType
import com.jb.leitnerbox.feature.session.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionResultScreen(
    viewModel: SessionResultViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val session = viewModel.session
    val successRate = viewModel.successRate
    var showCelebration by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (successRate >= 60) {
            showCelebration = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.session_result_title)) }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.session_result_congrats),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$successRate%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.session_result_success_rate),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                ResultItem(
                    icon = Icons.Default.Info,
                    label = stringResource(R.string.session_result_reviewed_cards),
                    value = "${session.cardCount}"
                )

                ResultItem(
                    icon = Icons.Default.CheckCircle,
                    label = stringResource(R.string.session_result_correct_answers),
                    value = "${session.successCount}"
                )

                ResultItem(
                    icon = Icons.Default.Star,
                    label = stringResource(R.string.session_result_newly_mastered),
                    value = "${session.masteredCount}"
                )

                ResultItem(
                    icon = Icons.Default.KeyboardArrowUp,
                    label = stringResource(R.string.session_result_advanced),
                    value = "${session.advancedCount}"
                )

                ResultItem(
                    icon = Icons.Default.KeyboardArrowDown,
                    label = stringResource(R.string.session_result_retreated),
                    value = "${session.retreatedCount}"
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.session_result_finish))
                }
            }
        }

        if (showCelebration) {
            CelebrationOverlay(
                type = CelebrationType.SESSION_SUCCESS,
                onFinished = { showCelebration = false }
            )
        }
    }
}

@Composable
private fun ResultItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

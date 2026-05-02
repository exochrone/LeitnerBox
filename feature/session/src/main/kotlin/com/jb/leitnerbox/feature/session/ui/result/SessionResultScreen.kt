package com.jb.leitnerbox.feature.session.ui.result

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.ui.components.CelebrationOverlay
import com.jb.leitnerbox.core.ui.components.CelebrationType
import com.jb.leitnerbox.feature.session.R
import java.time.Instant

@Composable
fun SessionResultScreen(
    viewModel: SessionResultViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val showCelebration by viewModel.showCelebration.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                onFinish()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler { onFinish() }

    SessionResultContent(
        session = viewModel.session,
        successRate = viewModel.successRate,
        showCelebration = showCelebration,
        onCelebrationFinished = viewModel::onCelebrationFinished,
        onFinish = onFinish
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SessionResultContent(
    session: Session,
    successRate: Int,
    showCelebration: Boolean,
    onCelebrationFinished: () -> Unit,
    onFinish: () -> Unit
) {
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
                val headlineText = if (successRate >= 60) {
                    stringResource(R.string.session_result_congrats)
                } else {
                    stringResource(R.string.session_result_title)
                }

                val headlineColor = if (successRate >= 60) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = headlineColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$successRate%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.session_result_success_rate),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
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
                    value = "${session.successCount}",
                    dimmed = session.successCount == 0
                )

                ResultItem(
                    icon = Icons.Default.Star,
                    label = stringResource(R.string.session_result_newly_mastered),
                    value = "${session.masteredCount}",
                    dimmed = session.masteredCount == 0
                )

                ResultItem(
                    icon = Icons.Default.KeyboardArrowUp,
                    label = stringResource(R.string.session_result_advanced),
                    value = "${session.advancedCount}",
                    dimmed = session.advancedCount == 0
                )

                ResultItem(
                    icon = Icons.Default.KeyboardArrowDown,
                    label = stringResource(R.string.session_result_retreated),
                    value = "${session.retreatedCount}",
                    dimmed = session.retreatedCount == 0
                )

                Spacer(modifier = Modifier.height(32.dp))

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
                onFinished = onCelebrationFinished
            )
        }
    }
}

@Composable
private fun ResultItem(
    icon: ImageVector,
    label: String,
    value: String,
    dimmed: Boolean = false
) {
    val color = if (dimmed) 
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    else 
        MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (dimmed) color else MaterialTheme.colorScheme.secondary
        )
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = color
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SessionResultContentPreview() {
    val mockSession = Session(
        id = 1,
        date = Instant.now(),
        deckIds = listOf(1L),
        cardCount = 10,
        successCount = 8,
        masteredCount = 2,
        advancedCount = 6,
        retreatedCount = 1
    )
    MaterialTheme {
        SessionResultContent(
            session = mockSession,
            successRate = 80,
            showCelebration = false,
            onCelebrationFinished = {},
            onFinish = {}
        )
    }
}

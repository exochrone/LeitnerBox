package com.jb.leitnerbox.feature.session.ui.extra

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jb.leitnerbox.core.ui.components.CelebrationOverlay
import com.jb.leitnerbox.core.ui.components.CelebrationType
import com.jb.leitnerbox.feature.session.R

@Composable
fun ExtraSessionResultScreen(
    viewModel: ExtraSessionResultViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
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

    ExtraSessionResultContent(
        cardCount      = viewModel.cardCount,
        successCount   = viewModel.successCount,
        successRate    = viewModel.successRate,
        shouldCelebrate = viewModel.shouldCelebrate,
        onFinish       = onFinish
    )
}

@Composable
internal fun ExtraSessionResultContent(
    cardCount: Int,
    successCount: Int,
    successRate: Int,
    shouldCelebrate: Boolean,
    onFinish: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text  = if (successRate >= 60)
                    stringResource(R.string.session_result_congrats)
                else
                    stringResource(R.string.session_result_neutral),
                style = MaterialTheme.typography.headlineSmall
            )

            // Résultats — sans cartes maîtrisées ni avancement/recul
            ResultItem(
                label = stringResource(R.string.extra_result_cards_reviewed),
                value = cardCount.toString()
            )
            ResultItem(
                label = stringResource(R.string.extra_result_correct_answers),
                value = successCount.toString()
            )
            ResultItem(
                label = stringResource(R.string.extra_result_success_rate),
                value = "$successRate %"
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick  = onFinish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.session_finish))
            }
        }

        if (shouldCelebrate) {
            CelebrationOverlay(
                type       = CelebrationType.SESSION_SUCCESS,
                onFinished = {}
            )
        }
    }
}

@Composable
private fun ResultItem(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

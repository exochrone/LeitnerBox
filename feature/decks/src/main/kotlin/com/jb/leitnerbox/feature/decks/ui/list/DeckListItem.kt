package com.jb.leitnerbox.feature.decks.ui.list

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.ui.theme.LeitnerBoxDark
import com.jb.leitnerbox.core.ui.theme.LeitnerTrophyGold
import com.jb.leitnerbox.core.ui.utils.LeitnerColorUtils
import com.jb.leitnerbox.feature.decks.R
import com.jb.leitnerbox.feature.decks.ui.list.model.DeckDisplayItem
import com.jb.leitnerbox.feature.decks.utils.DeckDateFormatter
import java.time.Instant

@Composable
internal fun DeckListItem(
    item: DeckDisplayItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Ligne 1 : Titre ──────────────────────────────────────────────
            DeckTitleRow(
                name           = item.deck.name,
                totalCardCount = item.totalCardCount
            )

            // ── Ligne 2 : Boîtes Leitner + badge maîtrise ────────────────────
            LeitnerBoxesRow(
                cardsPerBox   = item.cardsPerBox,
                masteredCount = item.masteredCount,
                boxCount      = item.deck.intervals.size
            )

            // ── Ligne 3 : Barre de progression ───────────────────────────────
            ProgressRow(progress = item.progress)

            // ── Ligne 4 : Footer date + bouton Lancer ────────────────────────
            FooterRow(nextReviewDate = item.nextReviewDate)
        }
    }
}

// ─── Ligne 1 ─────────────────────────────────────────────────────────────────

@Composable
private fun DeckTitleRow(name: String, totalCardCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector  = Icons.Default.Style,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text     = "$totalCardCount",
            style    = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text     = name,
            style    = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color    = LeitnerBoxDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─── Ligne 2 ─────────────────────────────────────────────────────────────────

@Composable
private fun LeitnerBoxesRow(
    cardsPerBox: Map<Int, Int>,
    masteredCount: Int,
    boxCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Boîtes : un carré par boîte, sans espace entre eux
        Row(modifier = Modifier.weight(1f)) {
            (1..boxCount).forEach { boxNumber ->
                val count = cardsPerBox[boxNumber] ?: 0
                val color = LeitnerColorUtils.boxColor(
                    boxIndex   = boxNumber - 1,
                    totalBoxes = boxCount
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)   // carré
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = count.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (boxNumber > boxCount / 2) Color.White
                                else Color.Black
                    )
                }
            }
        }

        // Séparateur visuel
        Spacer(Modifier.width(12.dp))

        // Badge maîtrise : trophée + nombre à droite
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector        = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint               = LeitnerTrophyGold,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text  = masteredCount.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── Ligne 3 ─────────────────────────────────────────────────────────────────

@Composable
private fun ProgressRow(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue  = progress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label        = "deckProgress"
    )
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LinearProgressIndicator(
            progress  = { animatedProgress },
            modifier  = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color     = LeitnerBoxDark,
            trackColor = LeitnerBoxDark.copy(alpha = 0.15f)
        )
        Text(
            text  = "${(animatedProgress * 100).toInt()} %",
            style = MaterialTheme.typography.labelSmall,
            color = LeitnerBoxDark,
            modifier = Modifier.widthIn(min = 36.dp),
            textAlign = TextAlign.End
        )
    }
}

// ─── Ligne 4 ─────────────────────────────────────────────────────────────────

@Composable
private fun FooterRow(nextReviewDate: Instant?) {
    val locale = LocalConfiguration.current.locales[0]
    val dateLabel = remember(nextReviewDate) {
        DeckDateFormatter.format(nextReviewDate, locale)
    }
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = stringResource(R.string.deck_next_session, dateLabel),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilledTonalButton(
            onClick  = { },
            enabled  = false,
            shape    = RoundedCornerShape(50)
        ) {
            Text(
                text  = stringResource(R.string.deck_launch_button),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

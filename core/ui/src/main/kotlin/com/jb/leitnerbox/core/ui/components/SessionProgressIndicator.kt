package com.jb.leitnerbox.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SessionProgressIndicator(
    current: Int,
    total: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val fraction by remember(current, total) {
        derivedStateOf { if (total == 0) 0f else current.toFloat() / total }
    }
    
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        label = "progressAnimation"
    )

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

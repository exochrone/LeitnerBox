package com.jb.leitnerbox.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.ui.theme.CardRectoBackground
import com.jb.leitnerbox.core.ui.theme.CardRectoContent
import com.jb.leitnerbox.core.ui.theme.CardVersoBackground

@Composable
fun FlipCard2(
    recto: String,
    verso: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
    rectoZoom: Float = 1.0f,
    versoZoom: Float = 1.0f
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "CardFlip2"
    )

    val isAtRecto = rotation <= 90f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 380.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { onFlip() },
        colors = CardDefaults.cardColors(
            containerColor = if (isAtRecto)
                CardRectoBackground
            else
                CardVersoBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isAtRecto) {
                CardFace2(
                    text = recto,
                    style = MaterialTheme.typography.titleMedium,
                    color = CardRectoContent,
                    modifier = Modifier.fillMaxSize(),
                    zoom = rectoZoom
                )
            } else {
                CardFace2(
                    text = verso,
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalContentColor.current,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    zoom = versoZoom
                )
            }
        }
    }
}

@Composable
private fun CardFace2(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    zoom: Float = 1.0f
) {
    // Note: verticalScroll est gardé au cas où le texte dépasse vraiment, 
    // mais le WebView est censé être non-interactif. 
    // Cependant, le WebView lui-même ne scrolle pas d'après la spec "transparente".
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AlwaysMathWebView(
            text = text,
            style = style,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            zoom = zoom
        )
    }
}

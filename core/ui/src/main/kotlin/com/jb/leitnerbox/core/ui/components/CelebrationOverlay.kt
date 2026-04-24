package com.jb.leitnerbox.core.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jb.leitnerbox.core.ui.R
import kotlinx.coroutines.delay

enum class CelebrationType { CARD_MASTERED, SESSION_SUCCESS }

@Composable
fun CelebrationOverlay(
    type: CelebrationType,
    onFinished: () -> Unit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            if (type == CelebrationType.CARD_MASTERED) R.raw.confetti_light
            else R.raw.confetti_full
        )
    )

    LottieAnimation(
        composition = composition,
        iterations = 1,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )

    LaunchedEffect(Unit) {
        delay(if (type == CelebrationType.CARD_MASTERED) 1500L else 2000L)
        onFinished()
    }
}

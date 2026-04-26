package com.jb.leitnerbox.core.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.jb.leitnerbox.core.ui.theme.LeitnerBoxDark

object LeitnerColorUtils {
    /**
     * Calcule la couleur de la boîte [boxIndex] (0-indexé) parmi [totalBoxes] boîtes.
     * Interpole linéairement entre une version claire et une version foncée [darkColor].
     * Cette formule s'adapte automatiquement à tout nombre de boîtes.
     */
    fun boxColor(
        boxIndex: Int,
        totalBoxes: Int,
        darkColor: Color = LeitnerBoxDark
    ): Color {
        if (totalBoxes <= 1) return darkColor
        val fraction = boxIndex.toFloat() / (totalBoxes - 1).toFloat()
        
        // Dériver une couleur claire depuis la foncée
        val lightColor = darkColor.copy(
            red = darkColor.red.coerceAtLeast(0.7f),
            green = darkColor.green.coerceAtLeast(0.5f),
            blue = darkColor.blue.coerceAtLeast(0.5f),
            alpha = 1f
        )
        
        return lerp(lightColor, darkColor, fraction)
    }
}

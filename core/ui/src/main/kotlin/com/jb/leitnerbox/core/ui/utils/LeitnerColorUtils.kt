package com.jb.leitnerbox.core.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.jb.leitnerbox.core.ui.theme.LeitnerBoxDark
import com.jb.leitnerbox.core.ui.theme.LeitnerBoxLight

object LeitnerColorUtils {
    /**
     * Calcule la couleur de la boîte [boxIndex] (0-indexé) parmi [totalBoxes] boîtes.
     * Interpole linéairement entre LeitnerBoxLight (boîte 0) et LeitnerBoxDark (dernière boîte).
     * Cette formule s'adapte automatiquement à tout nombre de boîtes.
     */
    fun boxColor(boxIndex: Int, totalBoxes: Int): Color {
        if (totalBoxes <= 1) return LeitnerBoxDark
        val fraction = boxIndex.toFloat() / (totalBoxes - 1).toFloat()
        return lerp(LeitnerBoxLight, LeitnerBoxDark, fraction)
    }
}

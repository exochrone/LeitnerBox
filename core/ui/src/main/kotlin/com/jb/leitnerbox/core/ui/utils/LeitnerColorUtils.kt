package com.jb.leitnerbox.core.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.jb.leitnerbox.core.ui.theme.LeitnerBoxDark

object LeitnerColorUtils {
    /**
     * Calcule la couleur de la boîte [boxIndex] (0-indexé) parmi [totalBoxes] boîtes.
     *
     * Le dégradé va de la teinte pastel (boîte 1) à la couleur pleine (dernière boîte) :
     * - lightColor = lerp(blanc, darkColor, 0.15f) → pastel de la même teinte
     * - Résultat   = lerp(lightColor, darkColor, fraction)
     *
     * Pour la couleur par défaut ("default"), passer defaultGrayDark comme darkColor.
     */
    fun boxColor(
        boxIndex: Int,
        totalBoxes: Int,
        darkColor: Color = LeitnerBoxDark
    ): Color {
        if (totalBoxes <= 1) return darkColor
        val fraction = boxIndex.toFloat() / (totalBoxes - 1).toFloat()
        val lightColor = lerp(Color.White, darkColor, 0.15f)
        return lerp(lightColor, darkColor, fraction)
    }
}

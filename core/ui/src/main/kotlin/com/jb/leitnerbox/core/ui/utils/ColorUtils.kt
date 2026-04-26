package com.jb.leitnerbox.core.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.ui.theme.DEFAULT_DECK_COLOR

object ColorUtils {
    /**
     * Convertit une string hex (#RRGGBB ou #AARRGGBB) en Color Compose.
     * Ne doit pas recevoir "default" — utiliser Deck.resolveColor() pour ça.
     * Retourne Color.Unspecified si la string est invalide.
     */
    fun fromHex(hex: String): Color = try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        Color.Unspecified
    }

    /**
     * Convertit une Color Compose en string hex #RRGGBB.
     */
    fun toHex(color: Color): String {
        val argb = color.toArgb()
        return "#%06X".format(argb and 0xFFFFFF)
    }
}

/**
 * Résout la couleur d'un deck :
 * - "default" → MaterialTheme.colorScheme.onSurface (adaptatif thème clair/sombre)
 * - "#RRGGBB"  → Color Compose correspondante
 */
@Composable
fun Deck.resolveColor(): Color = when (color) {
    DEFAULT_DECK_COLOR -> MaterialTheme.colorScheme.onSurface
    else -> ColorUtils.fromHex(color)
}

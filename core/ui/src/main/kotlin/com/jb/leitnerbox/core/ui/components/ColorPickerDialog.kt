package com.jb.leitnerbox.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.ui.R
import com.jb.leitnerbox.core.ui.theme.DEFAULT_DECK_COLOR
import com.jb.leitnerbox.core.ui.theme.DeckColorPalette
import com.jb.leitnerbox.core.ui.utils.ColorUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    currentColorHex: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.color_picker_title))
        },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Option "Par défaut" — cercle bicolore clair/sombre
                val isDefaultSelected = currentColorHex == DEFAULT_DECK_COLOR
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onColorSelected(DEFAULT_DECK_COLOR) }
                        .then(
                            if (isDefaultSelected) Modifier.border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(40.dp)) {
                        drawArc(Color.Black, 90f, 180f, useCenter = true)
                        drawArc(Color.White, 270f, 180f, useCenter = true)
                        drawCircle(
                            color = Color.LightGray,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }

                // 12 couleurs prédéfinies
                DeckColorPalette.forEach { color ->
                    val colorHex = ColorUtils.toHex(color)
                    val isSelected = currentColorHex == colorHex
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected(colorHex) }
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                ) else Modifier
                            )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

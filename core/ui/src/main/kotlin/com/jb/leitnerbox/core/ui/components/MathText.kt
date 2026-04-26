package com.jb.leitnerbox.core.ui.components

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jb.leitnerbox.core.domain.utils.LatexDetector

/**
 * Composable qui affiche du texte avec rendu LaTeX si nécessaire.
 * - Texte sans $ → Text Compose standard (léger)
 * - Texte avec $ → WebView KaTeX (rendu mathématique)
 */
@Composable
fun MathText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current
) {
    if (!LatexDetector.containsLatex(text)) {
        // Cas nominal : Text Compose standard, aucune WebView
        Text(
            text     = text,
            style    = style,
            color    = color,
            modifier = modifier
        )
        return
    }

    // Cas LaTeX : WebView KaTeX
    val context       = LocalContext.current
    val textColorHex  = "#%06X".format(color.toArgb() and 0xFFFFFF)
    val fontSizeSp    = style.fontSize.value.takeIf { !it.isNaN() && it > 0 } ?: 16f

    // Charger le template une seule fois par composition
    val template = remember {
        context.assets
            .open("katex/katex_template.html")
            .bufferedReader()
            .use { it.readText() }
    }

    // Détecter si c'est un bloc $$ ou inline $
    val trimmed = text.trim()
    val isDisplayMode = trimmed.startsWith("$$") && trimmed.endsWith("$$")
    val latexContent  = text
        .replace("$$", "")
        .replace("$", "")
        .trim()

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled      = true
                    allowFileAccess        = true
                    allowFileAccessFromFileURLs = true
                    setSupportZoom(false)
                    builtInZoomControls    = false
                    displayZoomControls    = false
                    loadWithOverviewMode   = true
                    useWideViewPort        = false
                }
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { webView ->
            val html = template
                .replace("{{FONT_SIZE}}",     fontSizeSp.toString())
                .replace("{{TEXT_COLOR}}",    textColorHex)
                .replace("{{LATEX_CONTENT}}", latexContent.replace("`", "\\`"))
                .replace("{{DISPLAY_MODE}}",  isDisplayMode.toString())

            webView.loadDataWithBaseURL(
                "file:///android_asset/katex/",
                html,
                "text/html",
                "UTF-8",
                null
            )
        },
        onRelease = { webView ->
            webView.destroy()
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
    )
}

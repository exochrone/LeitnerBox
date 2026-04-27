package com.jb.leitnerbox.core.ui.components

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jb.leitnerbox.core.domain.utils.LatexDetector

/**
 * Composable qui affiche du texte avec rendu LaTeX si nécessaire.
 * - Texte sans délimiteurs valides → Text Compose standard (léger)
 * - Texte avec $ ou $$ → WebView KaTeX via auto-render
 */
@Composable
fun MathText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current
) {
    if (!LatexDetector.containsLatex(text)) {
        Text(
            text     = text,
            style    = style,
            color    = color,
            textAlign = TextAlign.Center,
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current
    val textColorHex = "#%06X".format(color.toArgb() and 0xFFFFFF)
    val fontSizeSp = style.fontSize.value.takeIf { !it.isNaN() && it > 0 } ?: 16f

    // Stockage de la hauteur rapportée par la WebView (en pixels)
    var contentHeightPx by remember { mutableIntStateOf(0) }

    val template = remember {
        context.assets
            .open("katex/katex_template.html")
            .bufferedReader()
            .use { it.readText() }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    allowFileAccess = true
                }
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                
                // Interface pour recevoir la hauteur depuis JS
                addJavascriptInterface(
                    object : Any() {
                        @JavascriptInterface
                        fun reportHeight(height: Int) {
                            contentHeightPx = height
                        }
                    },
                    "Android"
                )
            }
        },
        update = { webView ->
            val escapedContent = text
                .replace("\\", "\\\\")
                .replace("`", "\\`")

            val html = template
                .replace("{{FONT_SIZE}}", fontSizeSp.toString())
                .replace("{{TEXT_COLOR}}", textColorHex)
                .replace("{{LATEX_CONTENT}}", escapedContent)

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
            .height(
                if (contentHeightPx > 20) {
                    contentHeightPx.dp
                } else {
                    80.dp
                }
            )
    )
}

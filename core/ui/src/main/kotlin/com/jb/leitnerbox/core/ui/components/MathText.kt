package com.jb.leitnerbox.core.ui.components

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jb.leitnerbox.core.domain.utils.LatexDetector

@Composable
fun MathText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign = TextAlign.Center,
    onRendered: () -> Unit = {}
) {
    if (!LatexDetector.containsLatex(text)) {
        LaunchedEffect(Unit) { onRendered() }
        Text(
            text = text,
            style = style,
            color = color,
            textAlign = textAlign,
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current
    val density = LocalDensity.current
    val textColorHex = "#%06X".format(color.toArgb() and 0xFFFFFF)
    val fontSizeSp = style.fontSize.value.takeIf { !it.isNaN() && it > 0 } ?: 16f

    var contentHeightPx by remember { mutableIntStateOf(0) }

    val template = remember {
        context.assets
            .open("katex/katex_template.html")
            .bufferedReader()
            .use { it.readText() }
    }

    val htmlTextAlign = when (textAlign) {
        TextAlign.Start -> "left"
        TextAlign.End -> "right"
        TextAlign.Justify -> "justify"
        else -> "center"
    }

    val html = remember(text, textColorHex, fontSizeSp, htmlTextAlign) {
        val escapedContent = text
            .replace("\\", "\\\\")
            .replace("`", "\\`")

        template
            .replace("{{FONT_SIZE}}", fontSizeSp.toString())
            .replace("{{TEXT_COLOR}}", textColorHex)
            .replace("{{TEXT_ALIGN}}", htmlTextAlign)
            .replace("{{LATEX_CONTENT}}", escapedContent)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                
                // Permettre le scroll horizontal
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false // On le cache mais il fonctionne
                
                addJavascriptInterface(
                    object : Any() {
                        @JavascriptInterface
                        fun reportHeight(height: Int) {
                            Handler(Looper.getMainLooper()).post {
                                if (contentHeightPx != height) {
                                    contentHeightPx = height
                                    onRendered()
                                }
                            }
                        }
                    },
                    "Android"
                )
            }
        },
        update = { webView ->
            // On évite de recharger la page si le HTML est identique (évite boucle infinie)
            if (webView.tag != html) {
                webView.loadDataWithBaseURL(
                    "file:///android_asset/katex/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
                webView.tag = html
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(
                if (contentHeightPx > 0) {
                    with(density) { contentHeightPx.toDp() }
                } else {
                    80.dp
                }
            )
    )
}

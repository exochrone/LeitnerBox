package com.jb.leitnerbox.core.ui.components

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.viewinterop.AndroidView
import com.jb.leitnerbox.core.domain.utils.LatexDetector

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MathText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign = TextAlign.Center,
    onRendered: () -> Unit = {},
    onClick: () -> Unit = {} // Déclenché via le clic HTML interne
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
    val textColorHex = String.format("#%06X", 0xFFFFFF and color.toArgb())
    val align = when (textAlign) {
        TextAlign.Left -> "left"
        TextAlign.Right -> "right"
        else -> "center"
    }

    val escapedContent = text
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("$", "\\$")

    // Ajout d'un écouteur 'onclick' en HTML qui appelle l'interface Android
    val html = remember(text, textColorHex, align) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <link rel="stylesheet" href="file:///android_asset/katex/katex.min.css">
            <script src="file:///android_asset/katex/katex.min.js"></script>
            <script src="file:///android_asset/katex/contrib/auto-render.min.js"></script>
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    color: $textColorHex;
                    text-align: $align;
                    font-family: sans-serif;
                    font-size: 18px;
                    background-color: transparent;
                    word-wrap: break-word;
                }
                .content {
                    padding: 8px;
                    min-height: 90%;
                }
            </style>
        </head>
        <body>
            <div class="content" id="math-content" onclick="Android.performClick()">${escapedContent}</div>
            <script>
                document.addEventListener("DOMContentLoaded", function() {
                    renderMathInElement(document.body, {
                        delimiters: [
                            {left: "$$", right: "$$", display: true},
                            {left: "$", right: "$", display: false},
                            {left: "\\(", right: "\\)", display: false},
                            {left: "\\[", right: "\\]", display: true}
                        ],
                        throwOnError: false
                    });
                    Android.onRendered();
                });
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                settings.domStorageEnabled = true
                
                // On laisse la WebView gérer ses barres de défilement de façon transparente
                isVerticalScrollBarEnabled = true
                isHorizontalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                // Interface de pont JS -> Native
                addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun performClick() {
                        Handler(Looper.getMainLooper()).post {
                            onClick() // Exécute l'action de retournement dans Compose
                        }
                    }
                    
                    @JavascriptInterface
                    fun onRendered() {
                        Handler(Looper.getMainLooper()).post {
                            onRendered()
                        }
                    }
                }, "Android")
            }
        },
        update = { webView ->
            if (webView.tag != html) {
                webView.loadDataWithBaseURL("file:///android_asset/katex/", html, "text/html", "UTF-8", null)
                webView.tag = html
            }
        },
        onRelease = { webView ->
            webView.removeJavascriptInterface("Android")
            webView.destroy()
        },
        // IMPORTANT : Utilise fillMaxSize pour occuper l'espace de la Box centrale sans perturber le swipe parent
        modifier = modifier.fillMaxSize()
    )
}

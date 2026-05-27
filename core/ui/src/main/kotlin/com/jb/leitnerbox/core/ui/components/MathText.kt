package com.jb.leitnerbox.core.ui.components

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
    onRendered: () -> Unit = {}
) {
    if (!LatexDetector.containsLatex(text)) {
        LaunchedEffect(text) { onRendered() }
        Text(
            text = text,
            style = style,
            color = color,
            textAlign = textAlign,
            modifier = modifier
        )
        return
    }

    val textColorHex = String.format("#%06X", 0xFFFFFF and color.toArgb())
    val align = when (textAlign) {
        TextAlign.Left -> "left"
        TextAlign.Right -> "right"
        else -> "center"
    }
    val fontSize = style.fontSize.value

    val escapedContent = text
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("$", "\\$")

    val html = remember(text, textColorHex, align, fontSize) {
        """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
        <link rel="stylesheet" href="katex.min.css">
        <script src="katex.min.js"></script>
        <script src="auto-render.min.js"></script>
        <style>
          * { margin: 0; padding: 0; box-sizing: border-box; }
          html, body {
            background: transparent;
            width: 100%;
            height: 100%;
          }
          body {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 8px;
          }
          #content {
            width: 100%;
            text-align: $align;
            font-family: -apple-system, sans-serif;
            font-size: ${fontSize}px;
            color: $textColorHex;
            line-height: 1.5;
            word-wrap: break-word;
          }
          .katex-display { margin: 0.3em 0; }
        </style>
        </head>
        <body>
          <div id="content"></div>
          <script>
            document.getElementById('content').textContent = `$escapedContent`;
            renderMathInElement(document.getElementById('content'), {
              delimiters: [
                {left: "$$", right: "$$", display: true},
                {left: "$",  right: "$",  display: false},
                {left: "\\(", right: "\\)", display: false},
                {left: "\\[", right: "\\]", display: true}
              ],
              throwOnError: false
            });
            if (window.Android) window.Android.onRendered();
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
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                isClickable = false
                isFocusable = false
                isFocusableInTouchMode = false
                addJavascriptInterface(
                    object : Any() {
                        @JavascriptInterface
                        fun onRendered() {
                            Handler(Looper.getMainLooper()).post { onRendered() }
                        }
                    },
                    "Android"
                )
            }
        },
        update = { webView ->
            if (webView.tag != html) {
                webView.loadDataWithBaseURL(
                    "file:///android_asset/katex/",
                    html, "text/html", "UTF-8", null
                )
                webView.tag = html
            }
        },
        onRelease = { webView ->
            webView.removeJavascriptInterface("Android")
            webView.destroy()
        },
        modifier = modifier.fillMaxWidth()
    )
}

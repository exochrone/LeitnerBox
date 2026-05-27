package com.jb.leitnerbox.core.ui.components

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
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

    val context = LocalContext.current
    val textColorHex = "#%06X".format(0xFFFFFF and color.toArgb())
    val fontSizeSp = style.fontSize.value.takeIf { !it.isNaN() && it > 0 } ?: 16f
    val htmlTextAlign = when (textAlign) {
        TextAlign.Start, TextAlign.Left -> "left"
        TextAlign.End, TextAlign.Right  -> "right"
        else                            -> "center"
    }

    var contentHeightDp by remember { mutableStateOf(80.dp) }

    val escapedContent = remember(text) {
        text
            .replace("\\", "\\\\")
            .replace("`", "\\`")
    }

    val html = remember(escapedContent, textColorHex, fontSizeSp, htmlTextAlign) {
        buildHtml(escapedContent, textColorHex, fontSizeSp, htmlTextAlign)
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
                isLongClickable = false
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

                addJavascriptInterface(
                    object : Any() {
                        @JavascriptInterface
                        fun onHeightMeasured(cssPx: Int) {
                            Handler(Looper.getMainLooper()).post {
                                if (cssPx > 5) {
                                    contentHeightDp = cssPx.dp
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
        modifier = modifier
            .fillMaxWidth()
            .height(contentHeightDp)
    )
}

private fun buildHtml(
    escapedContent: String,
    textColor: String,
    fontSize: Float,
    textAlign: String
): String = """
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
  }
  #content {
    width: 100%;
    text-align: $textAlign;
    font-family: -apple-system, sans-serif;
    font-size: ${fontSize}px;
    color: $textColor;
    line-height: 1.5;
    word-wrap: break-word;
  }
  .katex-display { margin: 0.3em 0; }
</style>
</head>
<body>
  <div id="content"></div>
  <script>
    var el = document.getElementById('content');
    el.textContent = `$escapedContent`;
    renderMathInElement(el, {
      delimiters: [
        {left: "$$", right: "$$", display: true},
        {left: "$",  right: "$",  display: false},
        {left: "\\(", right: "\\)", display: false},
        {left: "\\[", right: "\\]", display: true}
      ],
      throwOnError: false
    });

    function reportHeight() {
      var h = document.getElementById('content').offsetHeight;
      if (h > 5 && window.Android) {
        window.Android.onHeightMeasured(h);
      }
    }

    new ResizeObserver(function() { reportHeight(); })
      .observe(document.getElementById('content'));

    window.addEventListener('load', reportHeight);
    setTimeout(reportHeight, 400);
  </script>
</body>
</html>
""".trimIndent()

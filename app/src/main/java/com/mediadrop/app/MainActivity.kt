package com.mediadrop.app

import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webChromeClient = WebChromeClient()
        web.webViewClient = WebViewClient()
        web.addJavascriptInterface(AndroidBridge(), "MediaDropAndroid")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun openUrl(url: String) {
            runOnUiThread {
                try {
                    val uri = Uri.parse(url)
                    val host = uri.host?.lowercase() ?: ""
                    val allowed = host == "youtube.com" || host.endsWith(".youtube.com") || host == "youtu.be" ||
                        host == "facebook.com" || host.endsWith(".facebook.com") || host == "fb.watch" ||
                        host == "instagram.com" || host.endsWith(".instagram.com")
                    if (!allowed || (uri.scheme != "https" && uri.scheme != "http")) {
                        Toast.makeText(this@MainActivity, "Enlace no reconocido. Usa un enlace de YouTube, Facebook o Instagram.", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (_: Exception) {
                    Toast.makeText(this@MainActivity, "No se pudo abrir el enlace.", Toast.LENGTH_LONG).show()
                }
            }
        }

        @JavascriptInterface
        fun showMessage(message: String) {
            runOnUiThread { Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show() }
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}

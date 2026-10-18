
package com.mediadrop.app

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var pendingUrl: String? = null

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
        fun downloadFile(url: String) {
            runOnUiThread {
                startDownload(url)
            }
        }

        @JavascriptInterface
        fun openUrl(url: String) {
            runOnUiThread {
                try {
                    val uri = Uri.parse(url)
                    val host = uri.host?.lowercase() ?: ""
                    val allowed =
                        host == "youtube.com" || host.endsWith(".youtube.com") ||
                        host == "youtu.be" ||
                        host == "facebook.com" || host.endsWith(".facebook.com") ||
                        host == "fb.watch" ||
                        host == "instagram.com" || host.endsWith(".instagram.com")

                    if (!allowed || uri.scheme != "https") {
                        showToast("Enlace no reconocido o no seguro.")
                        return@runOnUiThread
                    }

                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (_: Exception) {
                    showToast("No se pudo abrir el enlace.")
                }
            }
        }

        @JavascriptInterface
        fun showMessage(message: String) {
            runOnUiThread {
                showToast(message.take(200))
            }
        }
    }

    private fun startDownload(rawUrl: String) {
        try {
            val uri = Uri.parse(rawUrl.trim())
            val scheme = uri.scheme?.lowercase()
            val host = uri.host?.lowercase() ?: ""

            if (scheme != "https" || host.isBlank()) {
                showToast("Usa un enlace HTTPS directo al archivo.")
                return
            }

            // Los enlaces de publicaciones no son archivos directos.
            val socialHost = host == "youtube.com" ||
                host.endsWith(".youtube.com") || host == "youtu.be" ||
                host == "facebook.com" || host.endsWith(".facebook.com") ||
                host == "fb.watch" || host == "instagram.com" ||
                host.endsWith(".instagram.com")

            if (socialHost) {
                showToast("Usa un enlace de descarga autorizado, no el enlace de la publicación.")
                return
            }

            val path = uri.path.orEmpty()
            val extension = path.substringAfterLast('.', "")
                .lowercase()

            val mime = when (extension) {
                "mp4" -> "video/mp4"
                "m4v" -> "video/x-m4v"
                "webm" -> "video/webm"
                "mov" -> "video/quicktime"
                "mp3" -> "audio/mpeg"
                "m4a" -> "audio/mp4"
                "aac" -> "audio/aac"
                "wav" -> "audio/wav"
                "ogg" -> "audio/ogg"
                else -> null
            }

            if (mime == null) {
                showToast("El enlace debe terminar en un formato de audio o vídeo compatible.")
                return
            }

            val filename = (uri.lastPathSegment ?: "mediadrop.$extension")
                .substringAfterLast('/')
                .replace(Regex("[^A-Za-z0-9._-]"), "_")
                .take(120)
                .ifBlank { "mediadrop.$extension" }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                pendingUrl = rawUrl
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST
                )
                return
            }

            val request = DownloadManager.Request(uri)
                .setTitle(filename)
                .setDescription("Descargando con MediaDrop")
                .setMimeType(mime)
                .setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    filename
                )

            val manager = getSystemService(Context.DOWNLOAD_SERVICE)
                as DownloadManager
            manager.enqueue(request)

            showToast("Descarga iniciada. Revisa Descargas.")
        } catch (_: Exception) {
            showToast("No se pudo iniciar la descarga. Revisa el enlace.")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            val url = pendingUrl
            pendingUrl = null

            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                url != null
            ) {
                startDownload(url)
            } else {
                showToast("Se necesita permiso para guardar el archivo.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST = 1607
    }
}

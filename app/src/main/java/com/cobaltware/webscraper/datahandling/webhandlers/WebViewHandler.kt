package com.cobaltware.webscraper.datahandling.webhandlers

import android.annotation.SuppressLint
import android.os.SystemClock
import android.webkit.WebView
import android.webkit.WebViewClient


class WebViewHandler @SuppressLint("SetJavaScriptEnabled") constructor(
    private val browser: WebView,
) {
    lateinit var html: String
    var ready = false

    init {
        browser.settings.javaScriptEnabled = true
        browser.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // Get HTML and display it
                evaluateHTML()
            }
        }
    }

    fun loadUrl(url: String) {
        ready = false
        browser.loadUrl(url)
    }

    private fun evaluateHTML() {
        browser.evaluateJavascript(
            "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();"
        ) { data ->
            // Rudimentary cloudflare bypass?
            if (data.lowercase().contains("checking your browser before accessing")) {
                SystemClock.sleep(1000)
                evaluateHTML()
                return@evaluateJavascript
            }
            html = data
            ready = true
        }
    }
}
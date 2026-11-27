package com.smsshield

import android.app.Activity
import android.app.role.RoleManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONArray

class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var scanner: SmsScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        scanner = SmsScanner(this)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = WebViewClient()
        web.addJavascriptInterface(Bridge(), "Android")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    inner class Bridge {
        @JavascriptInterface
        fun getMessages(): String = scanner.getMessages().toString()

        @JavascriptInterface
        fun scanExistingMessages() {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Scanning SMS inbox…", Toast.LENGTH_SHORT).show()
            }
            Thread {
                scanner.scanExistingMessages()
                runOnUiThread {
                    web.evaluateJavascript("loadMessages()", null)
                    Toast.makeText(this@MainActivity, "Scan complete", Toast.LENGTH_SHORT).show()
                }
            }.start()
        }

        @JavascriptInterface
        fun deleteMessages(idsJson: String): String {
            return try {
                val arr = JSONArray(idsJson)
                val ids = (0 until arr.length()).map { arr.getLong(it) }
                val count = scanner.deleteSelected(ids)
                runOnUiThread { web.evaluateJavascript("loadMessages()", null) }
                "{\"deleted\":$count}"
            } catch (e: Exception) {
                "{\"error\":\"Deletion failed\"}"
            }
        }

        @JavascriptInterface
        fun requestSmsRole() {
            runOnUiThread {
                val rm = getSystemService(RoleManager::class.java)
                if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_SMS)) {
                    if (!rm.isRoleHeld(RoleManager.ROLE_SMS)) {
                        startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_SMS), 42)
                    } else {
                        Toast.makeText(this@MainActivity, "Already the default SMS app", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "SMS role unavailable", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

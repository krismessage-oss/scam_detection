package com.smsshield

import android.content.ContentUris
import android.content.Context
import android.provider.Telephony
import com.chaquo.python.Python
import org.json.JSONArray
import org.json.JSONObject

class SmsScanner(private val context: Context) {
    private val prefs = context.getSharedPreferences("classifications", Context.MODE_PRIVATE)

    private fun classify(body: String): JSONObject {
        val result = Python.getInstance().getModule("detector")
            .callAttr("analyze_json", body).toString()
        return JSONObject(result)
    }

    fun scanExistingMessages() {
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY),
            null, null, "${Telephony.Sms.DATE} DESC"
        ) ?: return

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val sender = it.getString(1) ?: "Unknown sender"
                val body = it.getString(2) ?: ""
                val result = try { classify(body) } catch (_: Exception) { JSONObject() }
                val record = JSONObject().apply {
                    put("sender", sender)
                    put("body", body)
                    put("category", result.optString("category", "Review"))
                    put("confidence", result.optInt("confidence", 0))
                    put("reasons", result.optJSONArray("reasons") ?: JSONArray())
                }
                prefs.edit().putString(id.toString(), record.toString()).apply()
            }
        }
    }

    fun getMessages(): JSONArray {
        val output = JSONArray()
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY),
            null, null, "${Telephony.Sms.DATE} DESC"
        ) ?: return output

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val saved = prefs.getString(id.toString(), null)
                val item = if (saved != null) JSONObject(saved) else JSONObject().apply {
                    put("sender", it.getString(1) ?: "Unknown sender")
                    put("body", it.getString(2) ?: "")
                    put("category", "Unclassified")
                    put("confidence", 0)
                    put("reasons", JSONArray())
                }
                item.put("id", id)
                output.put(item)
            }
        }
        return output
    }

    fun deleteSelected(ids: List<Long>): Int {
        if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) return 0
        var deleted = 0
        for (id in ids.distinct()) {
            val saved = prefs.getString(id.toString(), null) ?: continue
            val category = JSONObject(saved).optString("category")
            if (category != "Spam" && category != "Scam") continue
            val uri = ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, id)
            val count = context.contentResolver.delete(uri, null, null)
            if (count > 0) {
                prefs.edit().remove(id.toString()).apply()
                deleted += count
            }
        }
        return deleted
    }
}

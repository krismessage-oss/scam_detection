package com.smsshield

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        Log.i("SMSShield", "SMS_DELIVER received ${parts.size} segment(s)")
        // Incoming-message persistence, notifications, and full default-SMS-app
        // behavior are deliberately not enabled in this test starter.
    }
}

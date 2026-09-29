package com.example.greetingcard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MyBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION = "com.example.MY_ACTION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION) {
            Toast.makeText(context, "Broadcast received!", Toast.LENGTH_SHORT).show()
        }
    }
}
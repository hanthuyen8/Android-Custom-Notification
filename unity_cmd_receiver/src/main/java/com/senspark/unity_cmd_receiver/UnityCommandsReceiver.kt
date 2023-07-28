package com.senspark.unity_cmd_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.json.JSONObject

class UnityCommandsReceiver(
    private val listener: String? = null,
    private val method: String? = null
) :
    BroadcastReceiver() {

    private val _initialized: Boolean = !listener.isNullOrEmpty() && !method.isNullOrEmpty()
    private val _cmdFormat = "com.senspark.unity_cmd_receiver.ACTION_COMMANDS"
    private val _unityClass = "com.unity3d.player.UnityPlayer"
    private val _unityMethod = "UnitySendMessage"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!_initialized) {
            return
        }
        if (intent != null) {
            if (!intent.action.equals(_cmdFormat)) {
                return
            }
            val cmd = intent.getStringExtra("cmd")
            val data = intent.getStringExtra("data")
            if (cmd.isNullOrEmpty()) {
                return;
            }
            sendMessageToUnity(cmd, data)
        }
    }

    private fun sendMessageToUnity(cmd: String, data: String?) {
        if (!_initialized) {
            return
        }
        try {
            val unityPlayerClass = Class.forName(_unityClass)
            val sender = unityPlayerClass.getMethod(
                _unityMethod,
                String::class.java,
                String::class.java,
                String::class.java
            )
            val jsonObject = JSONObject();
            jsonObject.put("cmd", cmd)
            jsonObject.put("data", data)
            sender.invoke(null, listener, method, jsonObject.toString())
        } catch (e: Exception) {
            val msg = "Error when send message to Unity: ${e.message}"
            Log.e("Senspark", msg)
        }
    }
}
package com.senspark.unity_cmd_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONObject

class UnityCmdReceiver : BroadcastReceiver() {
    private val kActionName = "com.senspark.ACTION_CMD"
    private val kUnityClass = "com.unity3d.player.UnityPlayer"
    private val kUnityMethod = "UnitySendMessage"
    private val kCmd = "cmd"
    private val kData = "data"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (!intent.action.equals(kActionName)) {
                return
            }
            val cmd = intent.getStringExtra(kCmd)
            val data = intent.getStringExtra(kData)
            if (cmd.isNullOrEmpty()) {
                return
            }
            UnityCmdSenderInfo.getInstance().logger.log("Received Cmd $cmd $data")
            sendMessageToUnity(cmd, data)
        }
    }

    private fun sendMessageToUnity(cmd: String, data: String?) {
        val senderInfo = UnityCmdSenderInfo.getInstance()
        try {
            if (!senderInfo.initialized) {
                return
            }

            val unityPlayerClass = Class.forName(kUnityClass)
            val sender = unityPlayerClass.getMethod(
                kUnityMethod,
                String::class.java,
                String::class.java,
                String::class.java
            )
            val jsonObject = JSONObject()
            jsonObject.put(kCmd, cmd)
            jsonObject.put(kData, data)
            senderInfo.logger.log("Send Cmd to Unity $cmd $data")
            sender.invoke(null, senderInfo.listener, senderInfo.method, jsonObject.toString())
        } catch (e: Exception) {
            senderInfo.logger.error("Error when send Cmd to Unity: ${e.message}")
        }
    }
}
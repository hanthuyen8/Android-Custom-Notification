package com.senspark.unity_cmd_receiver

import android.util.Log

class Logger(private val enableLog: Boolean) {
    fun log(message: String) {
        if (enableLog) {
            Log.d("Unity", "[Senspark][Android] $message")
        }
    }

    fun error(message: String) {
        if (enableLog) {
            Log.e("Unity", "[Senspark][Android] $message")
        }
    }
}
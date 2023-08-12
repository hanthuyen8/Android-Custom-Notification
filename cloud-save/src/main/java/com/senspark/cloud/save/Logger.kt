package com.senspark.cloud.save

import android.util.Log

class Logger(private val enableLog: Boolean) {
    fun log(message: String) {
        if (enableLog) {
            Log.d("Senspark", "[Senspark][Android] $message")
        }
    }

    fun error(message: String) {
        if (enableLog) {
            Log.e("Senspark", "[Senspark][Android] $message")
        }
    }
}
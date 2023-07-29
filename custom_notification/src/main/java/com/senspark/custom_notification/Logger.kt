package com.senspark.custom_notification

import android.util.Log

class Logger(private val useLog: Boolean) {
    fun log(message: String) {
        if (useLog) {
            Log.d("Unity", message)
        }
    }
}
package com.senspark.unity_cmd_receiver

class UnityCmdSenderInfo(
    val listener: String,
    val method: String,
    enableLog: Boolean
) {
    val initialized: Boolean = listener.isNotEmpty() && method.isNotEmpty()
    val logger: Logger = Logger(enableLog)

    init {
        if (initialized) {
            logger.log("Initialized Unity Commands")
            instance = this
        }
    }

    companion object {
        @Volatile
        private var instance: UnityCmdSenderInfo? = null
        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: UnityCmdSenderInfo("", "", false).also { instance = it }
            }
    }
}
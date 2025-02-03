package com.example.womensafetyapp.utils

object VolumeButtonStateManger {
    private var lastPressTime = 0L
    private var pressCount = 0
    private const val PRESS_TIMEOUT = 1000L // 1 second timeout

    @Synchronized
    fun recordButtonPress(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPressTime > PRESS_TIMEOUT) {
            pressCount = 1
        } else {
            pressCount++
        }
        lastPressTime = currentTime

        if (pressCount >= 3) {
            pressCount = 0
            return true
        }
        return false
    }
}
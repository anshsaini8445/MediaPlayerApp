package com.app.mediaplayer

import java.util.Locale

object PrecisionTimeFormatter {

    // Time in Milliseconds to Precise Format (HH:MM:SS.mmm)
    fun formatWithMillis(timeMs: Long): String {
        if (timeMs < 0) return "00:00:00.000"
        
        val totalSeconds = timeMs / 1000
        val millis = timeMs % 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d.%03d", minutes, seconds, millis)
        }
    }
}

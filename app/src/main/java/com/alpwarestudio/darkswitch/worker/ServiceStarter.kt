package com.alpwarestudio.darkswitch.worker

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/**
 * Helper responsible for starting and stopping {@link DarkApplyService}.
 *
 * This abstraction centralizes foreground service lifecycle handling
 * to keep callers decoupled from service startup details.
 */
object ServiceStarter {

    /**
     * Starts {@link DarkApplyService} as a foreground service.
     *
     * Foreground start is required to comply with modern Android background
     * execution limits.
     */
    fun start(context: Context) {
        val i = Intent(context, DarkApplyService::class.java)
        // Use ContextCompat to ensure compatibility across API levels.
        ContextCompat.startForegroundService(context, i)
    }

    /**
     * Requests {@link DarkApplyService} to stop.
     */
    fun stop(context: Context) {
        val i = Intent(context, DarkApplyService::class.java)
        context.stopService(i)
    }
}
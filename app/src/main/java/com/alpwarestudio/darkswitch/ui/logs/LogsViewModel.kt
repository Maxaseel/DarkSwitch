package com.alpwarestudio.darkswitch.ui.logs

import androidx.lifecycle.ViewModel
import com.alpwarestudio.darkswitch.core.log.DsLog
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel exposing runtime logs to the UI layer.
 *
 * This ViewModel acts as a thin wrapper around {@link DsLog}, forwarding
 * the in-memory log stream and clear action to composables.
 */
class LogsViewModel : ViewModel() {
    // Hot stream of in-memory log lines for UI observation.
    val lines: StateFlow<List<String>> = DsLog.lines
    // Clears all currently collected log entries.
    fun clear() = DsLog.clear()
}
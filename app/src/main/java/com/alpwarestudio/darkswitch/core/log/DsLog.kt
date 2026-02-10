package com.alpwarestudio.darkswitch.core.log

import android.annotation.SuppressLint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import androidx.annotation.StringRes

/**
 * Lightweight in-memory logging utility for runtime diagnostics.
 *
 * Logs are kept only in memory and exposed as a {@link StateFlow} for UI observation
 * (e.g., debug screens). This logger is not intended for persistent storage.
 */
object DsLog {
    // Timestamp formatter used for log lines (HH:mm:ss).
    @SuppressLint("ConstantLocale")
    private val df = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // Backing state holding recent log lines in reverse-chronological order.
    private val _lines = MutableStateFlow<List<String>>(emptyList())
    // Public read-only stream for observing log updates.
    val lines: StateFlow<List<String>> = _lines

    /**
     * Appends a new log entry with a timestamp.
     *
     * The log buffer is capped to a fixed size to avoid unbounded memory growth.
     */
    fun add(msg: String) {
        val line = "${df.format(Date())}  $msg"
        val current = _lines.value
        // Prepend newest entry and trim to the most recent 200 lines.
        val next = (listOf(line) + current).take(200)
        _lines.value = next
    }

    /**
     * Appends a new log entry using a string resource with optional format arguments.
     *
     * This enables proper localization for log messages.
     */
    fun addString(context: Context, @StringRes resId: Int, vararg args: Any?) {
        val msg = if (args.isNotEmpty()) {
            context.getString(resId, *args)
        } else {
            context.getString(resId)
        }
        add(msg)
    }

    /**
     * Clears all in-memory log entries.
     */
    fun clear() {
        _lines.value = emptyList()
    }
}
package com.alpwarestudio.darkswitch.worker

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alpwarestudio.darkswitch.core.force.CompatMode
import com.alpwarestudio.darkswitch.core.force.ShizukuCompatForceDarkController
import com.alpwarestudio.darkswitch.core.foreground.ForegroundBus
import com.alpwarestudio.darkswitch.core.log.DsLog
import com.alpwarestudio.darkswitch.R
import com.alpwarestudio.darkswitch.data.prefs.SettingsStore
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Foreground lifecycle-aware service responsible for applying force-dark behavior
 * based on the current foreground application and user settings.
 *
 * The service observes:
 * - persisted settings from {@link SettingsStore}
 * - foreground app changes emitted by {@link ForegroundBus}
 *
 * When conditions change, it applies the selected {@link CompatMode}
 * via {@link ShizukuCompatForceDarkController}.
 */
class DarkApplyService : LifecycleService() {

    // Access to persisted user settings.
    private lateinit var store: SettingsStore

    // Controller responsible for applying force-dark via Shizuku.
    private lateinit var controller: ShizukuCompatForceDarkController

    // Cache of the last applied enable/disable state to avoid redundant operations.
    private var lastAppliedEnabled: Boolean? = null

    // Cache of the last applied compat mode to avoid unnecessary re-application.
    private var lastAppliedMode: CompatMode? = null

    override fun onCreate() {
        super.onCreate()

        store = SettingsStore(applicationContext)
        controller = ShizukuCompatForceDarkController(applicationContext)

        // Prepare and start the service in the foreground to ensure long-running execution.
        ServiceNotification.ensureChannel(this)
        startForeground(1, ServiceNotification.build(this))
        DsLog.addString(this, R.string.ds_log_service_started)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    store.settingsModelFlow,
                    ForegroundBus.pkg,
                ) { settings, fgPkg ->
                    settings to fgPkg
                }.collect { (settings, fgPkg) ->
                    // Short-circuit when the global master switch is disabled.
                    if (!settings.globalEnabled) return@collect

                    // Foreground package may be temporarily unavailable during startup.
                    if (fgPkg.isBlank()) {
                        DsLog.addString(this@DarkApplyService, R.string.ds_log_foreground_waiting_first_package)
                        return@collect
                    }

                    // Shizuku binder or permission is not ready; cannot apply changes yet.
                    if (!controller.isAvailable()) {
                        DsLog.addString(this@DarkApplyService, R.string.ds_log_shizuku_not_ready)
                        return@collect
                    }

                    // Small debounce to allow the foreground app to fully settle.
                    delay(200)

                    // Determine whether force-dark should be enabled for the current foreground app.
                    val shouldEnable = settings.enabledPackages.contains(fgPkg)
                    val mode = settings.compatMode

                    // Skip if the desired state is already applied.
                    if (lastAppliedEnabled == shouldEnable && lastAppliedMode == mode) return@collect

                    // Apply the selected compat strategy for the foreground app.
                    val ok = controller.apply(mode, shouldEnable)

                    // Some strategies require the target app to be restarted for changes to take effect.
                    if (mode.requiresRestartHint) {
                        DsLog.addString(this@DarkApplyService, R.string.ds_log_restart_hint)
                    }

                    val stateStr = if (shouldEnable) {
                        getString(R.string.ds_state_on)
                    } else {
                        getString(R.string.ds_state_off)
                    }

                    val resultStr = if (ok) {
                        getString(R.string.ds_result_ok)
                    } else {
                        getString(R.string.ds_result_fail)
                    }

                    DsLog.addString(
                        this@DarkApplyService,
                        R.string.ds_log_apply_result,
                        mode.name,
                        fgPkg,
                        stateStr,
                        resultStr
                    )

                    lastAppliedEnabled = shouldEnable
                    lastAppliedMode = mode
                }
            }
        }
    }

    /**
     * Called when the service is being destroyed.
     *
     * Used to emit a final log entry for diagnostics.
     */
    override fun onDestroy() {
        DsLog.addString(this, R.string.ds_log_service_stopped)
        super.onDestroy()
    }
}
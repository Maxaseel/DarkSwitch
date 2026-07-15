package com.alpwarestudio.darkswitch.worker

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alpwarestudio.darkswitch.R
import com.alpwarestudio.darkswitch.core.compatibility.CompatibilityRepository
import com.alpwarestudio.darkswitch.core.force.ShizukuCompatForceDarkController
import com.alpwarestudio.darkswitch.core.foreground.ForegroundBus
import com.alpwarestudio.darkswitch.core.log.DsLog
import com.alpwarestudio.darkswitch.data.prefs.SettingsStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class DarkApplyService : LifecycleService() {
    private lateinit var store: SettingsStore
    private lateinit var controller: ShizukuCompatForceDarkController
    private lateinit var compatibility: CompatibilityRepository
    private var lastPackage: String? = null
    private var lastEnabled: Boolean? = null

    override fun onCreate() {
        super.onCreate()
        store = SettingsStore(applicationContext)
        controller = ShizukuCompatForceDarkController(applicationContext)
        compatibility = CompatibilityRepository(applicationContext)
        ServiceNotification.ensureChannel(this)
        startForeground(1, ServiceNotification.build(this))
        DsLog.addString(this, R.string.ds_log_service_started)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(store.settingsModelFlow, ForegroundBus.pkg) { settings, pkg -> settings to pkg }
                    .collect { (settings, pkg) ->
                        if (!settings.globalEnabled || pkg.isBlank()) return@collect
                        if (!controller.isAvailable()) {
                            DsLog.addString(this@DarkApplyService, R.string.ds_log_shizuku_not_ready)
                            return@collect
                        }
                        val shouldEnable = pkg in settings.enabledPackages
                        if (pkg == lastPackage && shouldEnable == lastEnabled) return@collect
                        delay(180.milliseconds)
                        val dbMode = compatibility.get(pkg).recommendedMode
                        val preferred = settings.learnedModes[pkg] ?: dbMode
                        val result = controller.applyWithFallback(settings.compatMode, shouldEnable, preferred)
                        if (result.success && shouldEnable && result.appliedMode != null) {
                            store.rememberMode(pkg, result.appliedMode)
                        }
                        DsLog.addString(
                            this@DarkApplyService,
                            R.string.ds_log_apply_result,
                            result.appliedMode?.name ?: result.attemptedModes.joinToString("→") { it.name },
                            pkg,
                            if (shouldEnable) getString(R.string.ds_state_on) else getString(R.string.ds_state_off),
                            if (result.success) getString(R.string.ds_result_ok) else getString(R.string.ds_result_fail)
                        )
                        lastPackage = pkg
                        lastEnabled = shouldEnable
                    }
            }
        }
    }

    override fun onDestroy() {
        DsLog.addString(this, R.string.ds_log_service_stopped)
        super.onDestroy()
    }
}

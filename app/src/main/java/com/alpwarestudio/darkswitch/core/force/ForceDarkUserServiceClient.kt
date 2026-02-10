package com.alpwarestudio.darkswitch.core.force

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.alpwarestudio.darkswitch.BuildConfig
import com.alpwarestudio.darkswitch.core.log.DsLog
import com.alpwarestudio.darkswitch.R
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rikka.shizuku.Shizuku

/**
 * Client-side helper for connecting to and interacting with {@link ForceDarkUserService}
 * via Shizuku's user service mechanism.
 *
 * This class is responsible for:
 * - binding/unbinding the user service
 * - serializing connection attempts
 * - exposing safe suspend APIs for IPC calls
 */
class ForceDarkUserServiceClient(private val context: Context) {

    // Ensures that service connection attempts are serialized.
    private val mutex = Mutex()
    private var service: IForceDarkService? = null
    // Represents an in-flight connection attempt shared by concurrent callers.
    private var connecting: CompletableDeferred<IForceDarkService?>? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val s = IForceDarkService.Stub.asInterface(binder)
            if (s == null) DsLog.addString(context, R.string.ds_log_user_service_connected_binder_null)
            service = s
            // Resume all awaiting callers once the binder becomes available.
            connecting?.complete(s)
            connecting = null
            DsLog.addString(context, R.string.ds_log_user_service_connected)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Clear cached binder reference; a new bind is required.
            service = null
            DsLog.addString(context, R.string.ds_log_user_service_disconnected)
        }
    }

    /**
     * Lazily constructed arguments describing how the user service should be started.
     */
    private val userServiceArgs: Shizuku.UserServiceArgs by lazy {
        val cn = ComponentName(
            BuildConfig.APPLICATION_ID,
            ForceDarkUserService::class.java.name
        )

        Shizuku.UserServiceArgs(cn)
            .tag("force_dark_service")
            .version(1)
            .daemon(false)
            .processNameSuffix("darkswitch_user")
            .debuggable(BuildConfig.DEBUG)
    }

    /**
     * Ensures that the user service is bound and ready.
     *
     * Concurrent callers will share the same binding attempt.
     */
    suspend fun ensureConnected(): IForceDarkService? = mutex.withLock {
        service?.let { return it }

        connecting?.let { return it.await() }

        if (!Shizuku.pingBinder()) {
            DsLog.addString(context, R.string.ds_log_shizuku_binder_not_alive)
            return null
        }

        val deferred = CompletableDeferred<IForceDarkService?>()
        connecting = deferred

        return try {
            Shizuku.bindUserService(userServiceArgs, connection)
            deferred.await()
        } catch (t: Throwable) {
            DsLog.addString(
                context,
                R.string.ds_log_bind_user_service_failed,
                t.javaClass.name,
                t.message ?: ""
            )
            t.printStackTrace()
            connecting = null
            null
        }
    }

    /**
     * Unbinds from the user service and clears local state.
     */
    fun disconnect() {
        try {
            Shizuku.unbindUserService(userServiceArgs, connection, true)
        } catch (_: Throwable) {
        }
        service = null
    }

    /**
     * Requests the user service to toggle the global force-dark flag.
     */
    suspend fun setGlobalForceDark(enabled: Boolean): Boolean {
        val s = ensureConnected() ?: return false
        return try {
            s.setGlobalForceDark(enabled)
        } catch (t: Throwable) {
            DsLog.addString(
                context,
                R.string.ds_log_user_service_call_failed,
                t.javaClass.simpleName
            )
            false
        }
    }

    /**
     * Executes a shell command inside the user service process.
     */
    suspend fun runCommand(vararg cmd: String): Int {
        val s = ensureConnected() ?: return -1
        return try {
            s.runCommand(cmd)
        } catch (_: Throwable) {
            -2
        }
    }

    /**
     * Requests the user service to terminate itself and then disconnects locally.
     */
    suspend fun destroyService() {
        val s = ensureConnected() ?: return
        try {
            s.destroy()
        } catch (_: Throwable) {
        }
        disconnect()
    }
}
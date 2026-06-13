package com.crw.myteacher.data.remote

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Globalny manager sesji — emituje zdarzenie gdy sesja wygaśnie (401/403).
 * MainActivity nasłuchuje i przekierowuje na ekran logowania.
 */
object SessionManager {

    private const val TAG = "SessionManager"

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    /**
     * Gdy true, interceptor nie emituje sessionExpired (np. podczas walidacji sesji na splash
     * lub ładowania dashboardu bezpośrednio po logowaniu).
     */
    @Volatile
    var suppressExpiredEvent: Boolean = false

    fun notifySessionExpired() {
        if (!suppressExpiredEvent) {
            Log.w(TAG, "🔴 SESSION EXPIRED EVENT EMITTED — redirecting to Login")
            _sessionExpired.tryEmit(Unit)
        } else {
            Log.d(TAG, "🟡 Session expired suppressed (suppressExpiredEvent=true)")
        }
    }
}

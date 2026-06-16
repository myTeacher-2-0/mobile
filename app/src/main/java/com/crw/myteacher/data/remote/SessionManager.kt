package com.crw.myteacher.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionManager {

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    @Volatile
    var suppressExpiredEvent: Boolean = false

    fun notifySessionExpired() {
        if (!suppressExpiredEvent) {
            _sessionExpired.tryEmit(Unit)
        }
    }
}

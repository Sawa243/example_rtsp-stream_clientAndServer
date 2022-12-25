package com.sawacorp.displaysharepro.feature.connectToBroadcast.storages

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ActiveStreamStorage {

    private val _activeStream: MutableSharedFlow<Boolean> = MutableSharedFlow(1)
    val activeStream = _activeStream.asSharedFlow()

    suspend fun setActiveStream(activeStream: Boolean) {
        _activeStream.emit(activeStream)
    }

}
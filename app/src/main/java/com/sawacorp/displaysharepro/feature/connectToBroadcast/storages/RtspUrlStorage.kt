package com.sawacorp.displaysharepro.feature.connectToBroadcast.storages

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RtspUrlStorage {

    private val _urlSessionFlow: MutableSharedFlow<String> = MutableSharedFlow(1)
    val urlSessionFlow = _urlSessionFlow.asSharedFlow()

    suspend fun setSessionUrl(sessionUrl: String) {
        _urlSessionFlow.emit(sessionUrl)
    }

}
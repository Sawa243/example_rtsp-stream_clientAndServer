package com.sawacorp.displaysharepro.feature.connectToBroadcast.useCase

import com.sawacorp.displaysharepro.feature.connectToBroadcast.repository.ConnectRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class ConnectUseCase @Inject constructor(
    private val repository: ConnectRepository
) {
    fun serverStart(
        connectionCode: String, coroutineScope: CoroutineScope,
        onRtspUrl: (String) -> Unit
    ) = repository.startServer(connectionCode, coroutineScope, onRtspUrl)

    fun serverStop() {
        repository.serverStop()
    }
}
package com.sawacorp.displaysharepro.feature.connectToBroadcast.useCase

import com.sawacorp.displaysharepro.feature.connectToBroadcast.repository.ConnectRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class ConnectUseCase @Inject constructor(
    private val repository: ConnectRepository
) {

    var onRtspUrl: (String) -> Unit = {}
    var stopStream: () -> Unit = {}

    fun serverStart(
        connectionCode: String
    ) = repository.startServer(connectionCode, onRtspUrl, stopStream)

    fun serverStop() {
        repository.serverStop()
    }

    val urlSession: SharedFlow<String> = repository.urlSession

    suspend fun setUrlInStorage(url: String) {
        repository.setUrlInStorage(url)
    }

    val activeStream: SharedFlow<Boolean> = repository.activeStream

    suspend fun setActiveStream(activeStream: Boolean) {
        repository.setActiveStream(activeStream)
    }
}
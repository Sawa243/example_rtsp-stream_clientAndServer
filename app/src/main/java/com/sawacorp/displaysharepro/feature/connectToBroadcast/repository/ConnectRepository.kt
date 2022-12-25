package com.sawacorp.displaysharepro.feature.connectToBroadcast.repository

import android.content.Context
import com.safframework.server.converter.gson.GsonConverter
import com.safframework.server.core.AndroidServer
import com.sawacorp.displaysharepro.feature.connectToBroadcast.api.startHttpServer
import com.sawacorp.displaysharepro.feature.connectToBroadcast.database.AppDatabase
import com.sawacorp.displaysharepro.feature.connectToBroadcast.storages.ActiveStreamStorage
import com.sawacorp.displaysharepro.feature.connectToBroadcast.storages.RtspUrlStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Singleton

@Singleton
class ConnectRepository(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val rtspUrlStorage: RtspUrlStorage,
    private val activeStreamStorage: ActiveStreamStorage
) {

    private val clientDao = database.clientDao()
    private val server = AndroidServer.Builder {
        converter {
            GsonConverter()
        }
        port {
            port
        }
    }.build()

    val urlSession: SharedFlow<String> = rtspUrlStorage.urlSessionFlow

    suspend fun setUrlInStorage(url: String) {
        rtspUrlStorage.setSessionUrl(url)
    }

    val activeStream: SharedFlow<Boolean> = activeStreamStorage.activeStream

    suspend fun setActiveStream(activeStream: Boolean) {
        activeStreamStorage.setActiveStream(activeStream)
    }

    fun startServer(
        connectionCode: String,
        onRtspUrl: (String) -> Unit,
        stopStream: () -> Unit
    ) {
        startHttpServer(
            context,
            server,
            connectionCode,
            clientDao,
            onRtspUrl,
            stopStream
        )
    }

    fun serverStop() {
        server.close()
    }

}
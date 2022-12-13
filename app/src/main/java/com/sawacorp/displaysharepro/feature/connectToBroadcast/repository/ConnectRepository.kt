package com.sawacorp.displaysharepro.feature.connectToBroadcast.repository

import android.content.Context
import com.safframework.server.converter.gson.GsonConverter
import com.safframework.server.core.AndroidServer
import com.sawacorp.displaysharepro.feature.connectToBroadcast.api.startHttpServer
import com.sawacorp.displaysharepro.feature.connectToBroadcast.database.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Singleton
class ConnectRepository(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
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

    fun startServer(
        connectionCode: String, coroutineScope: CoroutineScope,
        onRtspUrl: (String) -> Unit,
        stopStream: () -> Unit
    ) {
        startHttpServer(
            context,
            server,
            connectionCode,
            clientDao,
            coroutineScope,
            onRtspUrl,
            stopStream
        )
    }

    fun serverStop() {
        server.close()
    }

}
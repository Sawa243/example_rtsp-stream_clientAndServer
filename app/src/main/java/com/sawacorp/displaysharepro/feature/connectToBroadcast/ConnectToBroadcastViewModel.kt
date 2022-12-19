package com.sawacorp.displaysharepro.feature.connectToBroadcast

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexvas.rtsp.widget.RtspSurfaceView
import com.sawacorp.displaysharepro.feature.connectToBroadcast.useCase.ConnectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ConnectToBroadcastViewModel @Inject constructor(
    private val connectUseCase: ConnectUseCase
) : ViewModel() {

    private var connectionWork: Boolean = false
    val port = 8080
    val connectionCode: MutableStateFlow<List<Int>> = MutableStateFlow(listOf())
    val rtspString: MutableStateFlow<String> = MutableStateFlow("")
    val stopStream: MutableStateFlow<Boolean> = MutableStateFlow(true)

    init {
        val amplitudes = IntArray(6) { Random.nextInt(10 - 0) + 0 }.asList()
        viewModelScope.launch {
            connectionCode.emit(amplitudes)
        }
    }

    fun startHttpServer(code: String) {
        if (!connectionWork) {
            connectUseCase.serverStart(code, viewModelScope, { rtspUrl ->
                viewModelScope.launch {
                    rtspString.emit(rtspUrl)
                }
            }, {
                viewModelScope.launch {
                    stopStream.emit(true)
                }
            })
            connectionWork = true
        }
    }

    fun initSurfaceView(streamVideo: RtspSurfaceView, rtsp: String) {
        val uri = Uri.parse(rtsp)
        streamVideo.apply {
            init(uri, "", "")
            setStatusListener(getRtspStatusListener())
            start(requestVideo = true, requestAudio = true)
        }
        viewModelScope.launch {
            stopStream.emit(false)
        }
    }

    private fun getRtspStatusListener(): RtspSurfaceView.RtspStatusListener {
        return object : RtspSurfaceView.RtspStatusListener {
            override fun onRtspFirstFrameRendered() {
                viewModelScope.launch {
                    stopStream.emit(false)
                }
            }

            override fun onRtspStatusConnected() {
                viewModelScope.launch {
                    stopStream.emit(false)
                }
            }

            override fun onRtspStatusConnecting() {

            }

            override fun onRtspStatusDisconnected() {
                viewModelScope.launch {
                    stopStream.emit(true)
                }
            }

            override fun onRtspStatusFailed(message: String?) {
                viewModelScope.launch {
                    stopStream.emit(true)
                }
            }

            override fun onRtspStatusFailedUnauthorized() {
                viewModelScope.launch {
                    stopStream.emit(true)
                }
            }

        }
    }

    override fun onCleared() {
        super.onCleared()
        connectionWork = false
        connectUseCase.serverStop()
    }

}
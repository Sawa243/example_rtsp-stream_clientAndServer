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

    init {
        val amplitudes = IntArray(6) { Random.nextInt(10 - 0) + 0 }.asList()
        viewModelScope.launch {
            connectionCode.emit(amplitudes)
        }
    }

    fun startHttpServer(code: String) {
        if (!connectionWork) {
            connectUseCase.serverStart(code, viewModelScope) { rtspUrl ->
                viewModelScope.launch {
                    rtspString.emit(rtspUrl)
                }
            }
            connectionWork = true
        }
    }

    fun initSurfaceView(streamVideo: RtspSurfaceView, rtsp: String) {
        val uri = Uri.parse(rtsp)
        streamVideo.init(uri, "", "")
        streamVideo.start(requestVideo = true, requestAudio = false)
    }

    override fun onCleared() {
        super.onCleared()
        connectionWork = false
        connectUseCase.serverStop()
    }

}
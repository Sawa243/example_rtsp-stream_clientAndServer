package com.sawacorp.displaysharepro.feature.connectToBroadcast

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.sawacorp.displaysharepro.feature.connectToBroadcast.useCase.ConnectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectToBroadcastViewModel @Inject constructor(
    private val connectUseCase: ConnectUseCase
) : ViewModel() {

    val port = 8080
    var code = ""
    val rtspString: MutableStateFlow<String> = MutableStateFlow("")
    val activeStream: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var player: ExoPlayer? = null

    init {
        viewModelScope.launch {
            connectUseCase.urlSession.onEach { rtspUrl ->
                rtspString.value = rtspUrl
            }.launchIn(this)
            connectUseCase.activeStream.onEach { isActive ->
                activeStream.value = isActive
            }.launchIn(this)
        }
    }

    fun initExoPlayerView(streamVideo: StyledPlayerView, rtsp: String) {
        val uri = Uri.parse(rtsp)

        if (player == null) {
            player = ExoPlayer.Builder(streamVideo.context).build()
        }

        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setLiveConfiguration(
                MediaItem.LiveConfiguration.Builder().setMaxOffsetMs(2000).setTargetOffsetMs(1000)
                    .build()
            )
        val mediaSource =
            RtspMediaSource.Factory().setTimeoutMs(2000).createMediaSource(mediaItem.build())
        player?.let {
            streamVideo.player = it
            it.setMediaSource(mediaSource)
            it.prepare()
            it.addListener(getPlayerListener())
            it.playWhenReady = true
            it.play()
        }

        viewModelScope.launch {
            activeStream.emit(true)
        }
    }

    fun stopPlayer() {
        player?.let {
            it.stop()
            it.release()
        }
    }

    private fun getPlayerListener() = object : Player.Listener { //Todo можно добавить методов
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, playbackState)
            if (playbackState == Player.STATE_ENDED) {
                viewModelScope.launch {
                    activeStream.emit(false)
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) { //TODO надо тестить
            super.onIsPlayingChanged(isPlaying)
            /*if (!isPlaying) {
                viewModelScope.launch {
                    stopStream.emit(true)
                }
            }*/
        }
    }

    override fun onCleared() {
        super.onCleared()
        player = null
        connectUseCase.serverStop()
    }

}
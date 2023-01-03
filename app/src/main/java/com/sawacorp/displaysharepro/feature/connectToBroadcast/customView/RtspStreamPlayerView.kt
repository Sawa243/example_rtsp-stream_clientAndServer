package com.sawacorp.displaysharepro.feature.connectToBroadcast.customView

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.sawacorp.displaysharepro.R
import com.sawacorp.displaysharepro.databinding.LayoutRtspStreamPlayerBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A draggable viewResizeable which uses [SimpleExoPlayerView] to render video playback over main
 * application UI.
 */
class RtspStreamPlayerView : FrameLayout {

    private var _binding: LayoutRtspStreamPlayerBinding? = null
    private val binding get() = _binding!!

    private var playerViewSize: Int = 0
    private var sizeChangeable: Boolean = true
    private var playerType: Int = 0
    private var player: ExoPlayer? = null
    private val _activeStream: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val activeStream: StateFlow<Boolean> = _activeStream.asStateFlow()

    /**
     * Tracks if viewResizeable is fullscreen.
     */
    private var fullscreenOn: Boolean = false
    val isDraggable: Boolean
        get() {
            return !fullscreenOn
        }

    private var onClosed: () -> Unit = {}

    constructor(ctx: Context) : super(ctx) {
        _binding = LayoutRtspStreamPlayerBinding.inflate(LayoutInflater.from(ctx), this, true)
        initView()
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        setAttributes(attrs)
        _binding = LayoutRtspStreamPlayerBinding.inflate(LayoutInflater.from(ctx), this, true)
        initView()
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int) : super(ctx, attrs, defStyle) {
        setAttributes(attrs)
        _binding = LayoutRtspStreamPlayerBinding.inflate(LayoutInflater.from(ctx), this, true)
        initView()
    }

    private fun setAttributes(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RtspStreamPlayerView,
            0, 0
        ).apply {
            try {
                playerViewSize = getInteger(R.styleable.RtspStreamPlayerView_ov_player_size, 0)
                playerType = getInteger(R.styleable.RtspStreamPlayerView_ov_size_changeable, 0)
                sizeChangeable =
                    getBoolean(R.styleable.RtspStreamPlayerView_ov_size_changeable, true)
            } finally {
                recycle()
            }
        }
    }

    private fun initView() {

        if (!sizeChangeable) {
            binding.imageResize.visibility = View.GONE
            binding.imageClosePip.visibility = View.GONE
            binding.touchView.visibility = View.GONE
        }

        setListeners()
    }

    fun getFrameRoot() = binding.rootView
    fun getImageResizeButton() = binding.imageResize
    fun getTouchView() = binding.touchView
    fun getPlayerView() = binding.playerView

    private fun setListeners() {
        binding.imageClosePip.setOnClickListener {
            onClosed.invoke()
        }
    }

    fun setOnEventActionListener(
        onClosed: () -> Unit
    ) {
        this.onClosed = onClosed
    }

    fun initExoPlayerView(rtsp: String) {
        val uri = Uri.parse(rtsp)

        if (player == null) {
            player = ExoPlayer.Builder(getPlayerView().context).build()
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
            getPlayerView().player = it
            it.setMediaSource(mediaSource)
            it.prepare()
            it.addListener(getPlayerListener())
            it.playWhenReady = true
            it.play()
        }

        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            _activeStream.emit(true)
        }
    }

    fun stopPlayer() {
        if (player?.isPlaying == true) player?.let {
            it.stop()
            it.release()
        }
    }

    private fun getPlayerListener() = object : Player.Listener { //Todo можно добавить методов
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, playbackState)
            if (playbackState == Player.STATE_ENDED) {
                findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                    _activeStream.emit(false)
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) { //TODO надо тестить
            super.onIsPlayingChanged(isPlaying)
            /*if (!isPlaying) {
                findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                    stopStream.emit(true)
                }
            }*/
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        player = null
        _binding = null
    }
}
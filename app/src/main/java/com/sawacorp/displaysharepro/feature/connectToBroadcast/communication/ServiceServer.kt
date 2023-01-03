package com.sawacorp.displaysharepro.feature.connectToBroadcast.communication

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.sawacorp.displaysharepro.R
import com.sawacorp.displaysharepro.feature.connectToBroadcast.communication.MyReceiver.Companion.OPEN_APP
import com.sawacorp.displaysharepro.feature.connectToBroadcast.communication.MyReceiver.Companion.STOP_SERVICE
import com.sawacorp.displaysharepro.feature.connectToBroadcast.customView.RtspStreamPlayerView
import com.sawacorp.displaysharepro.feature.connectToBroadcast.useCase.ConnectUseCase
import com.sawacorp.displaysharepro.toDp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

@AndroidEntryPoint
class ServiceServer : Service(), CoroutineScope, View.OnTouchListener {

    @Inject
    lateinit var useCase: ConnectUseCase

    private lateinit var view: RtspStreamPlayerView
    private lateinit var windowManager: WindowManager
    private lateinit var paramsWM: WindowManager.LayoutParams
    private var code = ""
    private var rtspUrlNew = ""
    private var pipModeActive: Boolean = false
    private var screenSize: Point = Point(0, 0)
    private var oldDraggableRawEventY: Float = 0f
    private var oldDraggableRawEventX: Float = 0f
    private var oldResizeRawEventY: Float = 0f
    private var oldResizeRawEventX: Float = 0f
    private var minimumWindowSize: Point = Point(200.toDp(), 200.toDp())
    private var maximumWindowSize: Point = Point(0, 0)

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        Log.i(TAG, "RTP service create")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH)
        notificationManager?.createNotificationChannel(channel)

        pipMode()

        val amplitudes = IntArray(6) { Random.nextInt(10 - 0) + 0 }.asList()
        code = amplitudes.joinToString("")

        keepAliveTrick()

        controlServer()
    }

    private val job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Executors.newFixedThreadPool(100).asCoroutineDispatcher()

    private fun controlServer() {

        useCase.stopStream = {
            CoroutineScope(coroutineContext).launch(Dispatchers.Main) {
                useCase.setActiveStream(false)
            }
        }
        useCase.onRtspUrl = { rtspUrl ->
            if (rtspUrlNew != rtspUrl) {
                rtspUrlNew = rtspUrl
                if (pipModeActive) {
                    if (rtspUrlNew.isNotEmpty()) view.initExoPlayerView(rtspUrlNew)
                }
            }

            CoroutineScope(coroutineContext).launch(Dispatchers.Main) {
                useCase.setUrlInStorage(rtspUrl)
            }
            sendBroadcast(Intent(OPEN_APP))
        }
        CoroutineScope(coroutineContext).launch(Dispatchers.Main) {
            useCase.activeStream.onEach { activeStream ->
                if (pipModeActive) {
                    if (!activeStream) view.stopPlayer()
                }
            }.launchIn(this)
            view.activeStream.onEach { activeStream ->
                useCase.setActiveStream(activeStream)
            }.launchIn(this)
        }
        useCase.serverStart(code)

    }

    private fun keepAliveTrick() {
        val remoteViewMini = RemoteViews(packageName, R.layout.layout_service_mini)
        remoteViewMini.setTextViewText(R.id.txt_code, code)
        val remoteViewBig = RemoteViews(packageName, R.layout.layout_service_big)
        remoteViewBig.apply {
            setTextViewText(R.id.txt_code, code)
            setOnClickPendingIntent(
                R.id.btn_start_activity, PendingIntent.getBroadcast(
                    this@ServiceServer, 0, Intent(OPEN_APP),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            setOnClickPendingIntent(
                R.id.btn_stop_service, PendingIntent.getBroadcast(
                    this@ServiceServer, 0, Intent(STOP_SERVICE),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            setOnClickPendingIntent(
                R.id.btn_pip_mode, PendingIntent.getForegroundService(
                    this@ServiceServer, 0, Intent(START_PIP_MODE),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSilent(true)
            .setOngoing(false)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContent(remoteViewMini)
            .setCustomBigContentView(remoteViewBig)
            .build()
        startForeground(243, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun pipMode() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        display.getSize(screenSize)

        maximumWindowSize = Point(screenSize.x, screenSize.y)

        paramsWM =
            WindowManager.LayoutParams(
                200.toDp(),
                200.toDp(),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )

        paramsWM.apply {
            gravity = Gravity.START or Gravity.TOP
            x = 0
            y = 0
        }
        view = RtspStreamPlayerView(this)

        view.setOnEventActionListener(onClosed = {
            val intent = Intent(this, ServiceServer::class.java)
            intent.action = STOP_PIP_MODE
            startService(intent)
        })
        view.getTouchView().setOnTouchListener(this)
        view.getImageResizeButton().setOnTouchListener { v, event ->

            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val changeDistanceY = (event.rawY - oldResizeRawEventY)
                    oldResizeRawEventY = event.rawY
                    val height = if (paramsWM.height > 0) {
                        paramsWM.height
                    } else {
                        view.measuredHeight
                    }
                    var heightResize: Float = height + changeDistanceY

                    if (heightResize < minimumWindowSize.y) {
                        heightResize = minimumWindowSize.y.toFloat()
                    }
                    if (maximumWindowSize.y < heightResize) {
                        heightResize = maximumWindowSize.y.toFloat()
                    }
                    if (screenSize.y < heightResize) {
                        heightResize = screenSize.y.toFloat()
                    }

                    val changeDistanceX = (event.rawX - oldResizeRawEventX)
                    oldResizeRawEventX = event.rawX
                    val width = if (paramsWM.height > 0) {
                        paramsWM.width
                    } else {
                        view.measuredWidth
                    }
                    var widthResize: Float = width + changeDistanceX

                    if (widthResize < minimumWindowSize.x) {
                        widthResize = minimumWindowSize.x.toFloat()
                    }
                    if (maximumWindowSize.x < widthResize) {
                        widthResize = maximumWindowSize.x.toFloat()
                    }
                    if (screenSize.x < widthResize) {
                        widthResize = screenSize.x.toFloat()
                    }

                    paramsWM.height = heightResize.toInt()
                    paramsWM.width = widthResize.toInt()
                    val viewParams = view.getFrameRoot().layoutParams
                    viewParams.height = heightResize.toInt()
                    viewParams.width = widthResize.toInt()
                    view.getFrameRoot().layoutParams = viewParams

                    windowManager.updateViewLayout(view, paramsWM)
                }
                MotionEvent.ACTION_UP -> {
                    oldResizeRawEventY = event.rawY
                    oldResizeRawEventX = event.rawX
                }
                MotionEvent.ACTION_DOWN -> {
                    oldResizeRawEventY = event.rawY
                    oldResizeRawEventX = event.rawX
                }
                else -> {
                    oldResizeRawEventY = event.rawY
                    oldResizeRawEventX = event.rawX
                }
            }
            return@setOnTouchListener true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        INSTANCE = this
        Log.i(TAG, "RTP service started")
        when (intent?.action) {
            START_PIP_MODE -> {
                windowManager.addView(view, paramsWM)
                pipModeActive = true
                if (rtspUrlNew.isNotEmpty()) view.initExoPlayerView(rtspUrlNew)
            }
            STOP_PIP_MODE -> {
                view.stopPlayer()
                windowManager.removeView(view)
                pipModeActive = false
            }
        }
        return START_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!view.isDraggable) {
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {

                val changeDistanceY = (event.rawY - oldDraggableRawEventY)
                oldDraggableRawEventY = event.rawY
                var yPos: Float = paramsWM.y + changeDistanceY

                if (yPos < 0) {
                    yPos = 0f
                }
                if (screenSize.y < yPos + view.height) {
                    yPos = (screenSize.y - view.height).toFloat()
                }

                val changeDistanceX = (event.rawX - oldDraggableRawEventX)
                oldDraggableRawEventX = event.rawX
                var xPos: Float = paramsWM.x + changeDistanceX

                if (xPos < 0) {
                    xPos = 0f
                }
                if (screenSize.x < xPos + view.width) {
                    xPos = (screenSize.x - view.width).toFloat()
                }

                paramsWM.y = yPos.toInt()
                paramsWM.x = xPos.toInt()
                windowManager.updateViewLayout(view, paramsWM)
            }
            MotionEvent.ACTION_UP -> {
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
            MotionEvent.ACTION_DOWN -> {
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
            else -> {
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
        }

        view.onTouchEvent(event)
        return true
    }

    private var notificationManager: NotificationManager? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "RTP service destroy")
        if (pipModeActive) {
            windowManager.removeView(view)
        }
        INSTANCE = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun showNotification(text: String) {
        val notification = NotificationCompat.Builder(baseContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("RTP Service Stream")
            .setContentText(text)
            .setOngoing(false)
            .build()
        notificationManager?.notify(notifyId, notification)
    }

    companion object {
        private const val TAG = "Service"
        private const val channelId = "rtpServiceChannel"
        const val notifyId = 243000
        var INSTANCE: ServiceServer? = null
        const val START_PIP_MODE =
            "ru.nextouch.screenshareserver.communication.ServiceServer.startPipMode"
        const val STOP_PIP_MODE =
            "ru.nextouch.screenshareserver.communication.ServiceServer.stopPipMode"
    }
}
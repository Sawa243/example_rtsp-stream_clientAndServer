package com.sawacorp.displaysharepro.feature.connectToBroadcast.communication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.sawacorp.displaysharepro.R
import com.sawacorp.displaysharepro.feature.connectToBroadcast.communication.MyReceiver.Companion.OPEN_APP
import com.sawacorp.displaysharepro.feature.connectToBroadcast.communication.MyReceiver.Companion.STOP_SERVICE
import com.sawacorp.displaysharepro.feature.connectToBroadcast.useCase.ConnectUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

@AndroidEntryPoint
class ServiceServer : Service(), CoroutineScope {

    @Inject
    lateinit var useCase: ConnectUseCase
    private var code = ""

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        Log.i(TAG, "RTP service create")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH)
        notificationManager?.createNotificationChannel(channel)

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
            CoroutineScope(coroutineContext).launch(Dispatchers.Main) {
                useCase.setUrlInStorage(rtspUrl)
            }
            sendBroadcast(Intent(OPEN_APP))
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        INSTANCE = this
        Log.i(TAG, "RTP service started")

        return START_STICKY
    }

    companion object {
        private const val TAG = "Service"
        private const val channelId = "rtpServiceChannel"
        const val notifyId = 243000
        var INSTANCE: ServiceServer? = null
    }

    private var notificationManager: NotificationManager? = null

    private fun showNotification(text: String) {
        val notification = NotificationCompat.Builder(baseContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("RTP Service Stream")
            .setContentText(text)
            .setOngoing(false)
            .build()
        notificationManager?.notify(notifyId, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "RTP service destroy")
        INSTANCE = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}
package com.sawacorp.displaysharepro.feature.createScreenBroadcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.pedro.rtspserver.RtspServerDisplay
import com.sawacorp.displaysharepro.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DisplayService : Service() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        Log.i(TAG, "RTP Display service create")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH)
        notificationManager?.createNotificationChannel(channel)
        keepAliveTrick()
    }

    private fun keepAliveTrick() {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSilent(true)
            .setOngoing(false)
            .build()
        startForeground(243, notification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        INSTANCE = this
        Log.i(TAG, "RTP Display service started")
        rtspServerDisplay = RtspServerDisplay(baseContext, true, connectCheckerRtp, 1935)
        rtspServerDisplay?.glInterface?.setForceRender(true)
        return START_STICKY
    }

    companion object {
        private const val TAG = "DisplayService"
        private const val channelId = "rtpDisplayStreamChannel"
        const val notifyId = 243000
        var INSTANCE: DisplayService? = null
    }

    private var notificationManager: NotificationManager? = null
    private var rtspServerDisplay: RtspServerDisplay? = null

    fun sendIntent(): Intent? {
        return rtspServerDisplay?.sendIntent()
    }

    fun isStreaming(): Boolean {
        return rtspServerDisplay?.isStreaming ?: false
    }

    fun isRecording(): Boolean {
        return rtspServerDisplay?.isRecording ?: false
    }

    fun stopStream() {
        if (rtspServerDisplay?.isStreaming == true) {
            rtspServerDisplay?.stopStream()
            notificationManager?.cancel(notifyId)
        }
    }

    private val connectCheckerRtp = object : ConnectCheckerRtsp {

        override fun onAuthErrorRtsp() {
            showNotification("Stream auth error")
        }

        override fun onAuthSuccessRtsp() {
            showNotification("Stream auth success")
        }

        override fun onConnectionFailedRtsp(reason: String) {
            showNotification("Stream connection failed")
            Log.i(TAG, "RTP service destroy")
        }

        override fun onConnectionStartedRtsp(rtspUrl: String) {
            showNotification("Stream connection started")
        }

        override fun onConnectionSuccessRtsp() {
            showNotification("Stream started")
            Log.i(TAG, "RTP service destroy")
        }

        override fun onDisconnectRtsp() {
            showNotification("Stream stopped")
        }

        override fun onNewBitrateRtsp(bitrate: Long) {
            Log.i(TAG, "Stream bitrate: $bitrate")
            //showNotification("Stream bitrate: $bitrate")
        }
    }

    private fun showNotification(text: String) {
        val notification = NotificationCompat.Builder(baseContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("RTP Display Stream")
            .setContentText(text)
            .setOngoing(false)
            .build()
        notificationManager?.notify(notifyId, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "RTP Display service destroy")
        stopStream()
        INSTANCE = null
    }

    fun setIntentResult(resultCode: Int, data: Intent) {
        stopStream()
        if (rtspServerDisplay == null) {
            rtspServerDisplay = RtspServerDisplay(baseContext, true, connectCheckerRtp, 1935)
        }
        rtspServerDisplay?.setIntentResult(resultCode, data)
        rtspServerDisplay?.glInterface?.setForceRender(true)
    }

    fun startStream(callback: (String) -> Unit) {
        if (rtspServerDisplay?.isStreaming != true) {
            val customSettingsVideo =
                rtspServerDisplay?.prepareVideo()//TODO тут кастомные настройки видео
            val customSettingsAudio =
                rtspServerDisplay?.prepareAudio() //TODO тут кастомные настройки звука
            if (customSettingsVideo == true && customSettingsAudio == true) {
                rtspServerDisplay?.startStream()
                callback(rtspServerDisplay?.getEndPointConnection() ?: "")
            } else {
                if (rtspServerDisplay?.prepareVideo() == true && rtspServerDisplay?.prepareAudio() == true) {
                    rtspServerDisplay?.startStream()
                    callback(rtspServerDisplay?.getEndPointConnection() ?: "")
                }
            }
        } else {
            showNotification("You are already streaming :(")
        }
    }

}
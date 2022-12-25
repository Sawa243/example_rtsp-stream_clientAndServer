package com.sawacorp.displaysharepro.feature.connectToBroadcast.communication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sawacorp.displaysharepro.App
import com.sawacorp.displaysharepro.MainActivity

class MyReceiver : BroadcastReceiver() {

    companion object {
        const val OPEN_APP =
            "com.sawacorp.displaysharepro.feature.connectToBroadcast.communication.MyReceiver.start"
        const val STOP_SERVICE =
            "com.sawacorp.displaysharepro.feature.connectToBroadcast.communication.MyReceiver.stopService"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            OPEN_APP -> {
                if (!(context.applicationContext as App).isAppForeground()) {
                    val dialogIntent = Intent(context.applicationContext, MainActivity::class.java)
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    (context.applicationContext as App).startActivity(dialogIntent)
                }
            }
            STOP_SERVICE -> {
                val service: ServiceServer? = ServiceServer.INSTANCE
                if (service != null) {
                    context.applicationContext.stopService(
                        Intent(
                            context,
                            ServiceServer::class.java
                        )
                    )
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                val service: ServiceServer? = ServiceServer.INSTANCE
                if (service == null) {
                    context.applicationContext.startService(
                        Intent(
                            context,
                            ServiceServer::class.java
                        )
                    )
                }
            }
        }

    }
}
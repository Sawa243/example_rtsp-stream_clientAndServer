package com.sawacorp.displaysharepro

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.sawacorp.displaysharepro.databinding.ActivityMainBinding
import com.sawacorp.displaysharepro.feature.connectToBroadcast.communication.MyReceiver
import com.sawacorp.displaysharepro.feature.connectToBroadcast.communication.MyReceiver.Companion.OPEN_APP
import com.sawacorp.displaysharepro.feature.connectToBroadcast.communication.MyReceiver.Companion.STOP_SERVICE
import com.sawacorp.displaysharepro.feature.connectToBroadcast.communication.ServiceServer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val params = window.attributes
        params.flags = params.flags or FLAG_KEEP_SCREEN_ON
        window.attributes = params

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val intentFilter = IntentFilter()
        intentFilter.addAction(OPEN_APP)
        intentFilter.addAction(STOP_SERVICE)
        registerReceiver(MyReceiver(), intentFilter)

        val service: ServiceServer? = ServiceServer.INSTANCE
        if (service == null) {
            startService(Intent(this, ServiceServer::class.java))
        }

        if (!Settings.canDrawOverlays(this)) { //TODO
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        /*val textView = TextView(this).apply {
            text = "window"
            textSize = 18f
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.TRANSPARENT)
        }
        val parent = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT, PixelFormat.TRANSLUCENT )
        parent.type = WindowManager.LayoutParams.TYPE_APPLICATION
        parent.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        parent.gravity = Gravity.TOP or Gravity.RIGHT
        parent.x = 0
        parent.y = 100
        windowManager.addView(textView, parent)*/
    }

}
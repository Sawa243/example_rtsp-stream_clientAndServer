package com.sawacorp.displaysharepro.feature.createScreenBroadcast

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.sawacorp.displaysharepro.databinding.FragmentCreateScreenBroadcastBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateScreenBroadcast : Fragment(), ConnectCheckerRtsp {

    companion object {
        private val TAG = "CreateScreenBroadcast"
        private val REQUEST_CODE_STREAM = 1002
        private val REQUEST_CODE = "REQUEST_CODE"
        private val PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private val viewModel: CreateScreenBroadcastViewModel by viewModels()
    private var _binding: FragmentCreateScreenBroadcastBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateScreenBroadcastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.myIp
                .flowWithLifecycle(
                    viewLifecycleOwner.lifecycle,
                    Lifecycle.State.STARTED
                ).onEach {
                    binding.myIp.text = "Мой IP: $it"
                }
                .launchIn(this)

            viewModel.myAccessToken
                .flowWithLifecycle(
                    viewLifecycleOwner.lifecycle,
                    Lifecycle.State.STARTED
                ).onEach {
                    if (it.isNotEmpty()) {
                        binding.shareScreenButton.isVisible = true
                        binding.myToken.text = "Мой токен: $it"
                    }
                }.launchIn(this)

        }

        binding.connectButton.setOnClickListener {
            val code = binding.inputCode.text.toString()
            viewModel.connect(code)
        }

        binding.shareScreenButton.setOnClickListener {
            startScreenShare()
        }
        val displayService: DisplayService? = DisplayService.INSTANCE
        if (displayService == null) {
            requireActivity().startService(Intent(requireContext(), DisplayService::class.java))
        }
        binding.shareScreenButton.text =
            if (displayService != null && displayService.isStreaming()) "Остановить трансляцию" else "Поделиться экраном"

        viewModel.getListDevice()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val displayService: DisplayService? = DisplayService.INSTANCE
        if (displayService != null && !displayService.isStreaming() && !displayService.isRecording()) {
            requireActivity().stopService(Intent(requireContext(), DisplayService::class.java))
        }
        _binding = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_STREAM && data != null) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val displayService: DisplayService? = DisplayService.INSTANCE
                if (displayService != null) {
                    displayService.setIntentResult(resultCode, data)
                    displayService.startStream(
                        viewModel.widthConnectDevice,
                        viewModel.heightConnectDevice
                    ) {
                        viewModel.startScreenShare(it)
                    }
                    binding.shareScreenButton.text = "Остановить трансляцию"
                }
            }
        } else {
            Log.e(TAG, "get capture permission fail!")
        }
    }

    /*private val startForFile: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val requestCode = result.data?.getIntExtra(REQUEST_CODE, 0)
            if (requestCode == REQUEST_CODE_STREAM && result.data != null) {
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val displayService: DisplayService? = DisplayService.INSTANCE
                    if (displayService != null) {
                        displayService.setIntentResult(result.resultCode, result.data!!)
                        displayService.startStream(
                            viewModel.widthConnectDevice,
                            viewModel.heightConnectDevice
                        ) {
                            viewModel.startScreenShare(it)
                        }
                        binding.shareScreenButton.text = "Остановить трансляцию"
                    }
                }
            } else {
                Log.e(TAG, "get capture permission fail!")
            }
        }*/

    private fun startScreenShare() {
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, 1)
        } else {
            startStopStream()
        }
    }

    private fun startStopStream() {
        val displayService: DisplayService? = DisplayService.INSTANCE
        if (displayService != null) {
            if (!displayService.isStreaming()) {
                startActivityForResult(displayService.sendIntent()!!, REQUEST_CODE_STREAM)
                /*val intent = displayService.sendIntent()!!
                intent.putExtra(REQUEST_CODE, REQUEST_CODE_STREAM)
                startForFile.launch(intent)*/
            } else {
                displayService.stopStream()
                viewModel.stopStream()
                binding.shareScreenButton.text = "Поделиться экраном"
            }
        }
    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    override fun onAuthErrorRtsp() {
        viewModel.viewModelScope.launch {
            Toast.makeText(requireContext(), "Auth error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthSuccessRtsp() {
        viewModel.viewModelScope.launch {
            Toast.makeText(requireContext(), "Auth success", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionFailedRtsp(reason: String) {
        viewModel.viewModelScope.launch {
            Toast.makeText(requireContext(), "Connection failed. $reason", Toast.LENGTH_SHORT)
                .show()
            DisplayService.INSTANCE?.stopStream()
            viewModel.stopStream()
            binding.shareScreenButton.text = "Поделиться экраном"
        }
    }

    override fun onConnectionStartedRtsp(rtspUrl: String) {
        viewModel.viewModelScope.launch {
            Toast.makeText(requireContext(), "Connection started. $rtspUrl", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onConnectionSuccessRtsp() {
        viewModel.viewModelScope.launch {
            Toast.makeText(requireContext(), "Connection success", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDisconnectRtsp() {
        viewModel.viewModelScope.launch {
            Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNewBitrateRtsp(bitrate: Long) {
        viewModel.viewModelScope.launch {
            Toast.makeText(requireContext(), "Bitrate: $bitrate", Toast.LENGTH_SHORT).show()
        }
    }

}
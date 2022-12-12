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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.sawacorp.displaysharepro.databinding.FragmentCreateScreenBroadcastBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateScreenBroadcast : Fragment(), ConnectCheckerRtsp {

    companion object {
        private val TAG = "CreateScreenBroadcast"
        private val REQUEST_CODE_STREAM = 1002
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

            viewModel.myIp.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect {
                    binding.myIp.text = "My IP: $it"
                }

            viewModel.myAccessToken.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect {
                if (it.isNotEmpty()) {
                    binding.shareScreenButton.isEnabled = true
                }
                binding.myToken.text = "My token: $it"
            }

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
            if (displayService != null && displayService.isStreaming()) "Stop share screen" else "Start share screen"

    }

    override fun onDestroyView() {
        super.onDestroyView()
        val displayService: DisplayService? = DisplayService.INSTANCE
        if (displayService != null && !displayService.isStreaming() && !displayService.isRecording()) {
            requireActivity().stopService(Intent(requireContext(), DisplayService::class.java))
        }
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_STREAM && data != null) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val displayService: DisplayService? = DisplayService.INSTANCE
                if (displayService != null) {
                    displayService.setIntentResult(resultCode, data)
                    displayService.startStream {
                        viewModel.startScreenShare(it)
                    }
                }
            }
        } else {
            Log.e(TAG, "get capture permission fail!")
        }
    }

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
                binding.shareScreenButton.text = "Stop share screen"
            } else {
                displayService.stopStream()
                binding.shareScreenButton.text = "Start share screen"
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
            binding.shareScreenButton.text = "Start share screen"
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
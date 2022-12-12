package com.sawacorp.displaysharepro.feature.connectToBroadcast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.sawacorp.displaysharepro.databinding.FragmentConnectToBroadcastBinding
import com.sawacorp.displaysharepro.getMyIpV4Ip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConnectToBroadcast : Fragment() {

    companion object {

    }

    private val viewModel: ConnectToBroadcastViewModel by viewModels()
    private var _binding: FragmentConnectToBroadcastBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConnectToBroadcastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun initData() {
        binding.myIp.text = "Server IP：${getMyIpV4Ip()}:${viewModel.port}"

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.connectionCode.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect {
                    val code = it.joinToString("")
                    binding.connectionCode.text = "Connection code: $code"
                    viewModel.startHttpServer(code)
                }

            viewModel.rtspString.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { rtsp ->
                Toast.makeText(requireContext(), rtsp, Toast.LENGTH_LONG).show()
                viewModel.initSurfaceView(binding.streamVideo, rtsp)
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
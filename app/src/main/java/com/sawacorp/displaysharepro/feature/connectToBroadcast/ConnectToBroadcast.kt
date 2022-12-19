package com.sawacorp.displaysharepro.feature.connectToBroadcast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.sawacorp.displaysharepro.databinding.FragmentConnectToBroadcastBinding
import com.sawacorp.displaysharepro.getMyIpV4Ip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        binding.myIp.text = "Сервер IP：${getMyIpV4Ip()}:${viewModel.port}"

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.connectionCode.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).onEach {
                val code = it.joinToString("")
                binding.connectionCode.text = "Код для соединения: $code"
                viewModel.startHttpServer(code)
            }.launchIn(this)

            viewModel.rtspString
                .flowWithLifecycle(
                    viewLifecycleOwner.lifecycle,
                    Lifecycle.State.STARTED
                )
                .onEach { rtsp ->
                    if (rtsp.isNotEmpty()) {
                        viewModel.initSurfaceView(binding.streamVideo, rtsp)
                    }
                }
                .launchIn(this)
            viewModel.stopStream
                .flowWithLifecycle(
                    viewLifecycleOwner.lifecycle,
                    Lifecycle.State.STARTED
                )
                .onEach { stop ->
                    binding.apply {
                        if (stop) { //TODO когда на той стороне нажали остановить трансляцию или проблема с соединением
                                streamVideo.stop()
                                myIp.visibility = View.VISIBLE
                                connectionCode.visibility = View.VISIBLE
                        } else {
                            myIp.visibility = View.GONE
                            connectionCode.visibility = View.GONE
                        }
                    }
                }
                .launchIn(this)

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
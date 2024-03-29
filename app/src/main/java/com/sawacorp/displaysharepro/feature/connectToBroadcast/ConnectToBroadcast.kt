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

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.rtspString
                .flowWithLifecycle(
                    viewLifecycleOwner.lifecycle,
                    Lifecycle.State.STARTED
                )
                .onEach { rtsp ->
                    if (rtsp.isNotEmpty()) {
                        Toast.makeText(requireContext(), rtsp, Toast.LENGTH_LONG).show()
                        binding.rtspPlayerView.initExoPlayerView(rtsp)
                    }
                }
                .launchIn(this)
            viewModel.activeStream
                .flowWithLifecycle(
                    viewLifecycleOwner.lifecycle,
                    Lifecycle.State.STARTED
                )
                .onEach { active ->
                    binding.apply {
                        if (!active) { //TODO когда на той стороне нажали остановить трансляцию или проблема с соединением
                            binding.rtspPlayerView.stopPlayer()
                        }
                    }
                }
                .launchIn(this)
            binding.rtspPlayerView.activeStream
                .flowWithLifecycle(
                    viewLifecycleOwner.lifecycle,
                    Lifecycle.State.STARTED
                )
                .onEach { active ->
                    viewModel.setActiveStream(active)
                }
                .launchIn(this)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
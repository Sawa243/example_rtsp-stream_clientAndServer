package com.sawacorp.displaysharepro.feature.connectToBroadcast

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sawacorp.displaysharepro.databinding.FragmentConnectToBroadcastBinding
import dagger.hilt.android.AndroidEntryPoint
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

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
        binding.myIp.text = "Server IP：$localIPAddress:${viewModel.port}"
        viewModel.connectionCode.observe(this.viewLifecycleOwner) {
            val code = it.joinToString("")

            binding.connectionCode.text = "Connection code: $code"
            viewModel.startHttpServer(code)
        }

        viewModel.rtspString.observe(this.viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            val uri = Uri.parse(it)
            val svVideo = binding.streamVideo
            svVideo.init(uri, "", "")
            svVideo.start(requestVideo = true, requestAudio = false)
        }
    }

    private val localIPAddress: String by lazy {

        val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val intf: NetworkInterface = en.nextElement()
            val enumIpAddr: Enumeration<InetAddress> = intf.inetAddresses
            while (enumIpAddr.hasMoreElements()) {
                val inetAddress: InetAddress = enumIpAddr.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    return@lazy inetAddress.hostAddress?.toString() ?: ""
                }
            }
        }
        "null"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
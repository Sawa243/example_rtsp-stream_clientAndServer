package com.sawacorp.displaysharepro.feature.startPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sawacorp.displaysharepro.R
import com.sawacorp.displaysharepro.databinding.FragmentStartBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartFragment : Fragment() {

    companion object {

    }

    private val viewModel: StartViewModel by viewModels()
    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCreateShared.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_createScreenBroadcast)
        }
        binding.btnConnectShared.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_connectToBroadcast)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
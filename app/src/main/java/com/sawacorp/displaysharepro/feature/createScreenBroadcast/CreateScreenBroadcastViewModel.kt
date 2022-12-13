package com.sawacorp.displaysharepro.feature.createScreenBroadcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.sawacorp.displaysharepro.deleteSymbolsAfterLastDot
import com.sawacorp.displaysharepro.feature.createScreenBroadcast.api.getRequest
import com.sawacorp.displaysharepro.feature.createScreenBroadcast.api.postRequest
import com.sawacorp.displaysharepro.feature.createScreenBroadcast.entity.ConnectResponse
import com.sawacorp.displaysharepro.getMyDeviceName
import com.sawacorp.displaysharepro.getMyIpV4Ip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateScreenBroadcastViewModel @Inject constructor() : ViewModel() {

    val myIp: MutableStateFlow<String> = MutableStateFlow(getMyIpV4Ip())
    val myAccessToken: MutableStateFlow<String> = MutableStateFlow("")
    val mapDevice = mutableMapOf<String, String>()
    private var myAccessUrl = ""
    private var myAccessCode = ""

    fun getListDevice() {                            //TODO: get list device from server
        val mask = deleteSymbolsAfterLastDot(myIp.value)
        for (i in 1..255) {
            val url = "http://$mask.$i:8080/getName"
            viewModelScope.launch {
                val response = getRequest(url) ?: ""
                if (response.isNotEmpty()) {
                    mapDevice["http://$mask.$i:8080"] = response
                }
            }
        }
    }

    fun connect(code: String) {
        val mask = deleteSymbolsAfterLastDot(myIp.value)
        for (i in 1..255) {
            val url = "http://$mask.$i:8080/connect"
            val requestJson =
                "{\"connectionCode\": \"$code\", \"device\": \"${getMyDeviceName()}\"}"
            viewModelScope.launch {
                val response = postRequest(url, requestJson) ?: ""
                if (response.isNotEmpty()) {
                    myAccessUrl = "http://$mask.$i:8080"
                    myAccessCode = code
                    try {
                        Gson()
                            .fromJson(response, ConnectResponse::class.java)?.let { client ->
                                if (client.token?.isNotEmpty() == true) {
                                    myAccessToken.value = client.token
                                }
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun startScreenShare(rtspUrl: String) {
        val url = "$myAccessUrl/stream"
        val requestJson =
            "{\"rtsp\": \"$rtspUrl\"}"
        viewModelScope.launch {
            val response = postRequest(url, requestJson, myAccessToken.value) ?: ""
            if (response.isNotEmpty()) {
                try {

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stopStream() {
        val url = "$myAccessUrl/stopStream"
        viewModelScope.launch {
            val requestJson =
                "{\"connectionCode\": \"$myAccessCode\", \"device\": \"${getMyDeviceName()}\"}"
            val response = postRequest(url, requestJson) ?: ""
            if (response.isNotEmpty()) {
                try {

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}
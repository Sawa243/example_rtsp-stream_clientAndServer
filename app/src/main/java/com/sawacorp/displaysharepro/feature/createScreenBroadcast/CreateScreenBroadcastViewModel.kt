package com.sawacorp.displaysharepro.feature.createScreenBroadcast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.sawacorp.displaysharepro.deleteSymbolsAfterLastDot
import com.sawacorp.displaysharepro.feature.createScreenBroadcast.api.postRequest
import com.sawacorp.displaysharepro.feature.createScreenBroadcast.entity.ConnectResponse
import com.sawacorp.displaysharepro.getMyDeviceName
import com.sawacorp.displaysharepro.getMyIpV4Ip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateScreenBroadcastViewModel @Inject constructor(): ViewModel() {

    val myIp = MutableLiveData<String>()
    val myAccessToken = MutableLiveData<String>()
    private var myAccessUrl = ""

    init {
        myIp.postValue(getMyIpV4Ip())
    }

    fun connect(code: String) {
        myIp.value?.let { ip ->
            val mask = deleteSymbolsAfterLastDot(ip)
            for (i in 1..255) {
                val url = "http://$mask.$i:8080/connect"
                val requestJson =
                    "{\"connectionCode\": \"$code\", \"device\": \"${getMyDeviceName()}\"}"
                viewModelScope.launch {
                    val response = postRequest(url, requestJson) ?: ""
                    if (response.isNotEmpty()) {
                        myAccessUrl = "http://$mask.$i:8080"
                        try {
                            Gson()
                                .fromJson(response, ConnectResponse::class.java)?.let { client ->
                                    if (client.token?.isNotEmpty() == true) {
                                        myAccessToken.postValue(client.token ?: "")
                                    }
                                }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
            val response = postRequest(url, requestJson, myAccessToken.value ?: "") ?: ""
            if (response.isNotEmpty()) {
                try {

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}
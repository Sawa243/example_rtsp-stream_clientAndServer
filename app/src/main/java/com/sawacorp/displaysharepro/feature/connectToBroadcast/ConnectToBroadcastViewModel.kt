package com.sawacorp.displaysharepro.feature.connectToBroadcast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawacorp.displaysharepro.feature.connectToBroadcast.useCase.ConnectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ConnectToBroadcastViewModel @Inject constructor(
    private val connectUseCase: ConnectUseCase
) : ViewModel() {

    val port = 8080

    val connectionCode = MutableLiveData<List<Int>>()
    val rtspString = MutableLiveData<String>()

    init {
        val amplitudes = IntArray(6) { Random.nextInt(10 - 0) + 0 }.asList()
        connectionCode.postValue(amplitudes)
    }

    fun startHttpServer(code: String) {
        connectUseCase.serverStart(code, viewModelScope) { rtspUrl ->
            rtspString.postValue(rtspUrl)
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectUseCase.serverStop()
    }

}
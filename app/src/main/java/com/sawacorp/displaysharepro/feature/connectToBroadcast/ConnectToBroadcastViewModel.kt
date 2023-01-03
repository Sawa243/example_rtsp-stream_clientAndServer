package com.sawacorp.displaysharepro.feature.connectToBroadcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawacorp.displaysharepro.feature.connectToBroadcast.useCase.ConnectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectToBroadcastViewModel @Inject constructor(
    private val connectUseCase: ConnectUseCase
) : ViewModel() {

    private val _rtspString: MutableStateFlow<String> = MutableStateFlow("")
    val rtspString: StateFlow<String> = _rtspString.asStateFlow()
    private val _activeStream: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val activeStream: StateFlow<Boolean> = _activeStream.asStateFlow()

    init {
        viewModelScope.launch {
            connectUseCase.urlSession.onEach { rtspUrl ->
                _rtspString.value = rtspUrl
            }.launchIn(this)
            connectUseCase.activeStream.onEach { isActive ->
                _activeStream.value = isActive
            }.launchIn(this)
        }
    }

    fun setActiveStream(isActive: Boolean) {
        viewModelScope.launch {
            connectUseCase.setActiveStream(isActive)
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectUseCase.serverStop()
    }

}
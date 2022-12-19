package com.sawacorp.displaysharepro.feature.createScreenBroadcast.entity

data class ConnectResponse(
    val code: Int,
    val isSuccess: Boolean,
    val token: String?,
    val width: Int?,
    val height: Int?,
)
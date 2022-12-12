package com.sawacorp.displaysharepro.feature.createScreenBroadcast.api

import okhttp3.*
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun postRequest(url: String, json: String, token: String = ""): String? =
    suspendCoroutine { continuation ->

        val client = OkHttpClient()
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
        val request = Request.Builder()
            .url(url)
            .post(body)

        if (token.isNotEmpty()) {
            request.addHeader("token", token)
        }

        client.newCall(request.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resume("") // or null
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    continuation.resume(response.body()?.string()) // resume calling coroutine
                }
            }
        })
    }
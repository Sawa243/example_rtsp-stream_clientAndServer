package com.sawacorp.displaysharepro.feature.connectToBroadcast.api

import android.content.Context
import com.google.gson.Gson
import com.safframework.server.core.AndroidServer
import com.safframework.server.core.http.Request
import com.safframework.server.core.http.Response
import com.safframework.server.core.http.filter.HttpFilter
import com.safframework.server.core.log.LogManager
import com.sawacorp.displaysharepro.feature.connectToBroadcast.database.Client
import com.sawacorp.displaysharepro.feature.connectToBroadcast.database.ClientDao
import com.sawacorp.displaysharepro.feature.connectToBroadcast.entity.ConnectRequest
import com.sawacorp.displaysharepro.feature.connectToBroadcast.entity.RTSPRequest
import com.sawacorp.displaysharepro.getMyDeviceName
import com.sawacorp.displaysharepro.getScreenHeight
import com.sawacorp.displaysharepro.getScreenWidth
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun startHttpServer(
    context: Context,
    androidServer: AndroidServer,
    connectionCode: String,
    clientDao: ClientDao,
    viewModelScope: CoroutineScope,
    onRtspUrl: (String) -> Unit,
    stopStream: () -> Unit
) {

    androidServer
        .get("/getName") { _, response: Response ->
            response.setBodyText(getMyDeviceName())
        }
        .post("/connect") { request, response: Response ->
            createClient(request, clientDao, connectionCode, response, viewModelScope)
        }
        .post("/stream") { request, response: Response ->
            val token = request.header("token")
            val requestObject = Gson().fromJson(request.content(), RTSPRequest::class.java)

            val rtspUrl = requestObject.rtsp
            if (token != null && rtspUrl != null) {
                val client = clientDao.getByToken(token)
                if (client != null) {
                    onRtspUrl(rtspUrl)
                    response.setBodyText("ok")
                } else {
                    response.setBodyText("invalid token")
                }
            } else {
                response.setBodyText("invalid params")
            }
        }
        .post("/stopStream") { request, response: Response ->
            stopStream(request, connectionCode, response, stopStream)
        }
        .get("/testHTML") { _, response: Response ->
            response.html(context, "testHTML")
        }
        .filter("/connect/*", object : HttpFilter {
            override fun before(request: Request): Boolean {
                LogManager.d("HttpService", "before....")
                return true
            }

            override fun after(request: Request, response: Response) {
                LogManager.d("HttpService", "after....")
            }

        })
        .start()
}

fun jsonWithErrorCode(code: Int, message: String): String {
    return "{\"code\": $code, \"isSuccess\": false, \"message\": \"$message\"}"
}

fun jsonWithSuccessToken(token: String, width: Int, height: Int): String {
    return "{\"code\": 200, \"isSuccess\": true, \"token\": \"$token\", \"width\": \"$width\", \"height\": \"$height\"}"
}

fun createClient(
    request: Request,
    clientDao: ClientDao,
    code: String,
    response: Response,
    viewModelScope: CoroutineScope
): Response {
    val requestBody = request.content()
    val requestObject = Gson().fromJson(requestBody, ConnectRequest::class.java)
    return if (requestObject.connectionCode != code) {
        response.setStatus(400)
        response.setBodyText(jsonWithErrorCode(400, "Wrong connection code"))
    } else {
        val compactJws = Jwts.builder().claim("device", requestObject.device)
            .signWith(SignatureAlgorithm.HS256, "secret".toByteArray())
            .compact()

        val client = Client(device = requestObject.device, token = compactJws)
        viewModelScope.launch { clientDao.insert(client) }
        response.setStatus(200)
        response.setBodyText(jsonWithSuccessToken(compactJws, getScreenWidth(), getScreenHeight()))
    }
}

fun stopStream(
    request: Request,
    code: String,
    response: Response,
    stopStream: () -> Unit
): Response {
    val requestBody = request.content()
    val requestObject = Gson().fromJson(requestBody, ConnectRequest::class.java)
    return if (requestObject.connectionCode != code) {
        response.setStatus(400)
        response.setBodyText("error")
    } else {
        stopStream()
        response.setStatus(200)
        response.setBodyText("ok")
    }
}

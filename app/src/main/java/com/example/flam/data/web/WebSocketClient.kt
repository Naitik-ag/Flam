package com.example.flam.data.web

import android.util.Log
import okhttp3.*

class WSClient(url: String) {

    private val client = OkHttpClient()
    private val request = Request.Builder()
        .url(url.replace("ws://", "http://"))
        .build()

    private var webSocket: WebSocket? = null

    fun connect() {
        Log.d("WS_CLIENT", "OkHttp WebSocket connecting...")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WS_CLIENT", "CONNECTED to WS (via OkHttp)!")
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                Log.e("WS_CLIENT", "WS FAILURE: ${t.message}")
            }

            override fun onClosed(
                webSocket: WebSocket,
                code: Int,
                reason: String
            ) {
                Log.w("WS_CLIENT", "WS CLOSED: $reason")
            }
        })
    }

    fun sendBase64(base64: String) {
        Log.d("WS_CLIENT", "Sending Base64 length=${base64.length}")
        webSocket?.send(base64)
    }
}

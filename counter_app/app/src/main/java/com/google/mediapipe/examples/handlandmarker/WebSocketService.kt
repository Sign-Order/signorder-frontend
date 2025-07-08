package com.google.mediapipe.examples.handlandmarker

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WebSocketService {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val storeCode = "5fjVwE8z"
    private val clientType = "counter_app"

    // private val URL = "${BuildConfig.WS_URL}?store_code=$storeCode&client_type=$clientType&api-key=${BuildConfig.WS_API_KEY}"

    // 로컬 서버 테스트
    private val URL = "ws://10.0.2.2:8001/ws?store_code=$storeCode&client_type=$clientType&api-key=${BuildConfig.WS_API_KEY}"

    var isConnected = false
    var signUrls: List<String> = emptyList()

    // 외부에서 수신 반응 설정 가능
    var onSignUrlsReceived: ((List<String>) -> Unit)? = null
    var onSignOrderReceived: ((Int) -> Unit)? = null  // num 값 기반 요청 대응용

    fun connect() {
        if (isConnected) return

        val request = Request.Builder().url(URL).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                isConnected = true
                println("WebSocket 연결됨")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                println("메시지 수신: $text")
                handleMessage(text)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                println("WebSocket 오류: ${t.message}")
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                isConnected = false
                println("WebSocket 종료: $reason")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, null)
        isConnected = false
    }

    private fun handleMessage(message: String) {
        try {
            val json = JSONObject(message)
            val type = json.getString("type")
            val data = json.getJSONObject("data")

            when (type) {
                "signMessage" -> {
                    if (data.has("sign_urls")) {
                        val urlsArray: JSONArray = data.getJSONArray("sign_urls")
                        signUrls = (0 until urlsArray.length()).map { i ->
                            urlsArray.getString(i)
                        }
                        println("🎥 수어 영상 URLs: \n${signUrls.joinToString("\n")}")
                        onSignUrlsReceived?.invoke(signUrls)
                    } else if (data.has("title") && data.has("num")) {
                        val title = data.getString("title")
                        val num = data.getInt("num")
                        println("🧾 수신: title=$title, num=$num")
                        onSignOrderReceived?.invoke(num)
                    }
                }

                "orderMessage" -> {
                    val num = data.getInt("num")
                    val msg = data.getString("message")
                    val createdAt = data.getString("created_at")
                    println("💬 [주문 메시지] $num: $msg ($createdAt)")
                    // 여기에 메시지 UI 반영 추가 가능
                }

                else -> {
                    println("알 수 없는 type: $type")
                }
            }
        } catch (e: Exception) {
            println("메시지 파싱 실패: ${e.message}")
        }
    }
}

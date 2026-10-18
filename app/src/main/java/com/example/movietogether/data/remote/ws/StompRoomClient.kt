package com.example.movietogether.data.remote.ws

import com.example.movietogether.data.remote.dto.ChatMessageDto
import com.example.movietogether.data.remote.dto.PlaybackStateDto
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class StompRoomClient(
    private val onChatMessage: (ChatMessageDto) -> Unit = {},
    private val onPlaybackState: (PlaybackStateDto) -> Unit = {},
    private val onConnected: () -> Unit = {},
    private val onError: (String) -> Unit = {}
) {
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private var roomId: String? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    fun connect(wsUrl: String, roomId: String) {
        this.roomId = roomId
        val request = Request.Builder().url(wsUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send("CONNECT\naccept-version:1.2\nheart-beat:0,0\n\n\u0000")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                when {
                    text.startsWith("CONNECTED") -> {
                        webSocket.send(
                            "SUBSCRIBE\nid:sub-chat\ndestination:/topic/room/$roomId/chat\n\n\u0000"
                        )
                        webSocket.send(
                            "SUBSCRIBE\nid:sub-sync\ndestination:/topic/room/$roomId/sync\n\n\u0000"
                        )
                        onConnected()
                    }

                    text.startsWith("MESSAGE") -> {
                        val destination = text.lines()
                            .firstOrNull { it.startsWith("destination:") }
                            ?.removePrefix("destination:")
                            ?.trim()
                            .orEmpty()

                        val body = text.substringAfter("\n\n")
                            .trim('\u0000', ' ', '\n', '\r')

                        try {
                            when {
                                destination.contains("/chat") -> {
                                    onChatMessage(gson.fromJson(body, ChatMessageDto::class.java))
                                }
                                destination.contains("/sync") -> {
                                    onPlaybackState(gson.fromJson(body, PlaybackStateDto::class.java))
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }

                    text.startsWith("ERROR") -> onError(text)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onError(t.message ?: "WebSocket error")
            }
        })
    }

    fun sendChat(username: String, message: String) {
        val id = roomId ?: return
        val payload = gson.toJson(
            ChatMessageDto(roomId = id, username = username, message = message)
        )
        sendFrame("/app/room/$id/chat", payload)
    }

    fun sendPlayback(state: PlaybackStateDto) {
        val id = roomId ?: return
        sendFrame("/app/room/$id/sync", gson.toJson(state.copy(roomId = id)))
    }

    private fun sendFrame(destination: String, payload: String) {
        val frame =
            "SEND\n" +
                    "destination:$destination\n" +
                    "content-type:application/json\n\n" +
                    payload +
                    "\u0000"
        webSocket?.send(frame)
    }

    fun disconnect() {
        webSocket?.close(1000, "bye")
        webSocket = null
    }
}
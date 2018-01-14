package me.glatteis.unichat.chat

import org.java_websocket.WebSocket

data class User(val room: ChatRoom, val username: String, val webSocket: WebSocket)
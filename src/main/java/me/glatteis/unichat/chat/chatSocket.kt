package me.glatteis.unichat.chat

import me.glatteis.unichat.chatRooms
import me.glatteis.unichat.gson
import org.eclipse.jetty.server.session.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import spark.Spark


object ChatSocket {
    fun initSocket() {
        println("Igniting chatsocket...")
        Spark.webSocket("chatsocket", ChatRoomWebSocket::class.java)
    }
}

@WebSocket
class ChatRoomWebSocket {
    @OnWebSocketConnect
    fun onConnect(session: Session) {
        println("Someone connected")
    }

    @OnWebSocketMessage
    fun onMessage(messageAsString: String, session: Session) {
        val message = gson.toJsonTree(messageAsString).asJsonObject
        val roomString = message.get("room").asString
        chatRooms[roomString]?.onMessage(message, session)
    }


}
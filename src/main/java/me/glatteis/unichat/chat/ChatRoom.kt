package me.glatteis.unichat.chat

import me.glatteis.unichat.data.Room
import me.glatteis.unichat.users.User
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import spark.Spark
import org.eclipse.jetty.server.session.Session as JSession

/**
 * Created by Linus on 21.12.2017!
 */
class ChatRoom(val id: String, val room: Room) {

    val onlineUsers = ArrayList<User>()

    val socketUrl = "roomsocket/" + id

    init {
        Spark.webSocket("roomsocket/" + id, ChatRoomWebSocket::class.java)
    }

    @WebSocket
    class ChatRoomWebSocket {
        @OnWebSocketConnect
        fun onConnect(session: JSession) {

        }

        @OnWebSocketMessage
        fun onMessage() {

        }


    }

}


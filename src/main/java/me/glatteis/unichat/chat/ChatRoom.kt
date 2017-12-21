package me.glatteis.unichat.chat

import me.glatteis.unichat.data.Room
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import spark.Session
import spark.Spark

/**
 * Created by Linus on 21.12.2017!
 */
class ChatRoom(val id: String, val room: Room) {

    val socketUrl = "roomsocket/" + id

    init {
        Spark.webSocket("roomsocket/" + id, ChatRoomWebSocket::class.java)
    }


    @WebSocket
    class ChatRoomWebSocket {
        @OnWebSocketConnect
        fun onConnect(session: Session) {

        }


    }

}


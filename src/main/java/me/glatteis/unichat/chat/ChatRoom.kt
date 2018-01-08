package me.glatteis.unichat.chat

import me.glatteis.unichat.data.Room
import me.glatteis.unichat.gson
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

    val onlineUsers = HashSet<User>()

    val socketUrl = "roomsocket/" + id



    @WebSocket
    class ChatRoomWebSocket {
        @OnWebSocketConnect
        fun onConnect(session: JSession) {
            println("Someone connected")
        }

        @OnWebSocketMessage
        fun onMessage(messageAsString: String, session: JSession) {
            val message = gson.toJsonTree(messageAsString).asJsonObject
            when (message.get("type").asString) {
                "login" -> {
                    val nickname = message.get("nickname").asString

                }
            }
        }


    }

}


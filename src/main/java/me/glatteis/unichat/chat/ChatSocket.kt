package me.glatteis.unichat.chat

import com.google.gson.JsonParser
import me.glatteis.unichat.chatRooms
import me.glatteis.unichat.gson
import me.glatteis.unichat.jsonMap
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket

@WebSocket
object ChatSocket {

    private val socketsToRooms = HashMap<Session, User>()

    @OnWebSocketConnect
    fun connected(session: Session) = println("session connected")

    @OnWebSocketClose
    fun closed(session: Session, statusCode: Int, reason: String?) {
        socketsToRooms.remove(session)
    }

    @OnWebSocketMessage
    fun message(session: Session, messageAsString: String) {
        println(messageAsString)
        val jsonParser = JsonParser()
        val message = jsonParser.parse(messageAsString).asJsonObject
        if (message["type"].asString == "login") {
            // If user is trying to login, login
            val roomString = message.get("room")?.asString
            val username = message.get("username")?.asString
            val chatRoom = chatRooms[roomString]
            when {
                chatRoom == null -> session.remote.sendString(gson.jsonMap(
                        "type" to "error",
                        "reason" to "This room does not exist"
                ))
                username == null -> session.remote.sendString(gson.jsonMap(
                        "type" to "error",
                        "reason" to "You have not specified a username"
                ))
                else -> {
                    val user = User(chatRoom, username, session)
                    socketsToRooms[session] = user
                    chatRoom.onlineUsers.add(user)
                    chatRoom.onMessage(message, user)
                }
            }
        } else {
            // Else our room should already exist. Send that message to the room
            val user = socketsToRooms[session]
            if (user == null) {
                session.remote.sendString(gson.jsonMap(
                        "type" to "error",
                        "reason" to "you_are_not_logged_in"
                ))
                return
            }
            user.room.onMessage(message, user)
        }
    }
}
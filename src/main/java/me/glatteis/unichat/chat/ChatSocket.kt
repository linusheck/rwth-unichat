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
        // If the user exists, tell the room that they have left
        val user = socketsToRooms[session] ?: return
        user.room.onLogout(user)
        // Remove the users from the socket to room lookup table
        socketsToRooms.remove(session)
    }

    @OnWebSocketMessage
    fun message(session: Session, messageAsString: String) {
        val jsonParser = JsonParser()
        val message = jsonParser.parse(messageAsString).asJsonObject
        if (message["type"].asString == "login") {
            // The user is trying to login
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

                    // If the user desires to have an identity, search for their identity or create a new one
                    val publicKey = if (message.has("user-id-secret")) {
                        UserDatabase.getPublicId(message["user-id-secret"].asString)
                    } else {
                        "anonymous:$username"
                    }

                    val user = User(chatRoom, username, publicKey, session)
                    // Add user to lookup table
                    socketsToRooms[session] = user
                    // Tell the ChatRoom that someone has logged in
                    chatRoom.onLogin(user)
                    // Add user to ChatRoom online user list
                    chatRoom.onlineUsers.add(user)
                }
            }
        } else {
            // Else our user should already exist and wants to send a message to their room
            val user = socketsToRooms[session]
            if (user == null) {
                session.remote.sendString(gson.jsonMap(
                        "type" to "error",
                        "reason" to "You are not logged in"
                ))
                return
            }
            user.room.onMessage(message, user)
        }
    }
}
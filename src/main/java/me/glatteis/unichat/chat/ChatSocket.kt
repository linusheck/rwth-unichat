package me.glatteis.unichat.chat

import com.google.gson.JsonParser
import me.glatteis.unichat.chatRooms
import me.glatteis.unichat.error
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket

@WebSocket
object ChatSocket {

    private val socketsToRooms = HashMap<Session, User>()

    @OnWebSocketConnect
    fun connected(session: Session) {

    }

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
                chatRoom == null -> session.error("This room does not exist")
                username == null -> session.error("You have not specified a username")
                username.length > 32 -> session.error("Your username is too long")
                else -> {
                    // If the user desires to have an identity, search for their identity or create a new one
                    val publicId = if (message.has("user-id-secret")) {
                        val privateId = message["user-id-secret"]
                        UserIdentification.getPublicId(privateId.asString)
                    } else {
                        "anonymous:$username"
                    }
                    chatRoom.removeClosed()
                    if (chatRoom.onlineUsers.any { it.publicId == publicId || it.username == username }) {
                        session.error("A user with your ID or your nickname is already logged in")
                        return
                    }

                    val user = User(chatRoom, username, publicId, session)
                    // Add user to lookup table
                    socketsToRooms[session] = user
                    // Add user to ChatRoom online user list
                    chatRoom.onlineUsers.add(user)
                    // Tell the ChatRoom that someone has logged in
                    chatRoom.onLogin(user)
                }
            }
        } else {
            // Else our user should already exist and wants to send a message to their room
            val user = socketsToRooms[session]
            if (user == null) {
                session.error("You are not logged in")
                return
            }
            user.room.onMessage(message, user)
        }
    }
}
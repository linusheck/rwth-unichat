package me.glatteis.unichat.chat

import com.google.gson.JsonParser
import me.glatteis.unichat.*
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import java.security.InvalidKeyException
import java.util.*

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
        when {
            message["type"].asString == "login" -> {
                // The user is trying to login
                val roomString = message.get("room")?.asString
                val username = message.get("username")?.asString
                val chatRoom = chatRooms[roomString]
                when {
                    chatRoom == null -> session.error("This room does not exist", ErrorCode.ROOM_DOES_NOT_EXIST)
                    username == null -> session.error("You have not specified a username", ErrorCode.USERNAME_EMPTY)
                    username.isBlank() -> session.error("Your username is blank", ErrorCode.USERNAME_BLANK)
                    username.length > 32 -> session.error("Your username is too long", ErrorCode.USERNAME_TOO_LONG)
                    else -> {
                        val publicId = if (message.has("user-id") && message.has("challenge-response")) {
                            val publicKey = message["user-id"].asString
                            val challengeResponse = message["challenge-response"].asString
                            if (UserIdentification.verifyChallenge(publicKey, challengeResponse)) {
                                publicKey.toString()
                            } else {
                                session.error("Your challenge response is not valid", ErrorCode.INVALID_CHALLENGE_RESPONSE)
                                return
                            }
                        } else {
                            "anonymous:$username"
                        }

                        chatRoom.removeClosed()
                        if (chatRoom.onlineUsers.any { it.publicId == publicId || it.username == username }) {
                            session.error("A user with your ID or your nickname is already logged in", ErrorCode.DUPLICATE_USER)
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
            }
            message["type"].asString == "challenge" -> {
                if (!message.has("user-id")) {
                    session.error("Missing user-id", ErrorCode.USER_ID_EMPTY)
                    return
                }
                val publicKey = message["user-id"].asString
                println("Challenge: $publicKey")
                val challenge: String
                try {
                    challenge = UserIdentification.createChallenge(publicKey)
                } catch (e: InvalidKeyException) {
                    e.printStackTrace()
                    session.error(e.message ?: "Invalid key", ErrorCode.INVALID_KEY)
                    return
                }
                println("Returning $challenge")
                session.remote.sendString(gson.jsonMap(
                        "type" to "challenge",
                        "challenge" to challenge
                ))
            }
            message["type"].asString == "get-users" -> {
                val user = socketsToRooms[session]
                if (user == null) {
                    session.error("You are not logged in", ErrorCode.NOT_LOGGED_IN)
                    return
                }
                val room = user.room.id
                val chatRoom = chatRooms[room]
                if (chatRoom == null) {
                    session.error("This room does not exist", ErrorCode.ROOM_DOES_NOT_EXIST)
                    return
                }
                val onlineUsers = chatRoom.onlineUsersAsJson()
                session.remote.sendString(gson.jsonMap(
                        "type" to "info-users",
                        "users" to onlineUsers
                ))
            }
            else -> {
                // Else our user should already exist and wants to send a message to their room
                val user = socketsToRooms[session]
                if (user == null) {
                    session.error("You are not logged in", ErrorCode.NOT_LOGGED_IN)
                    return
                }
                user.room.onMessage(message, user)
            }
        }
    }
}

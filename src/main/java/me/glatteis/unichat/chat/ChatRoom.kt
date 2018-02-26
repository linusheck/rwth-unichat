package me.glatteis.unichat.chat

import com.google.gson.JsonObject
import me.glatteis.unichat.data.Room
import me.glatteis.unichat.gson
import me.glatteis.unichat.jsonMap

/**
 * Created by Linus on 21.12.2017!
 */
class ChatRoom(val id: String, val room: Room) {

    val onlineUsers = HashSet<User>()

    // Removes online users that have closed the connection
    fun HashSet<User>.removeClosed(): HashSet<User> {
        this.retainAll {
            it.isOpen()
        }
        return this
    }

    fun sendToAll(message: String) {
        for (u in onlineUsers.removeClosed()) {
            u.webSocket.remote.sendString(message)
        }
    }

    fun onMessage(message: JsonObject, user: User) {
        when (message.get("type").asString) {
            "login" -> {
                val nickname = message.get("username").asString
                sendToAll(gson.jsonMap(
                        "type" to "login-info",
                        "user" to nickname
                ))
                println("$nickname connected to room ${room.id}")
            }
            "message" -> {
                sendToAll(gson.jsonMap(
                        "type" to "message",
                        "username" to user.username,
                        "message" to message.get("message")
                ))
            }
        }
    }
}
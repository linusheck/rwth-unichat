package me.glatteis.unichat.chat

import com.google.gson.JsonObject
import me.glatteis.unichat.data.Room
import me.glatteis.unichat.gson
import me.glatteis.unichat.jsonMap
import me.glatteis.unichat.now

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

    // Called by the ChatSocket when someone logs out
    fun onLogout(user: User) {
        sendToAll(gson.jsonMap(
                "type" to "info-logout",
                "username" to user.username
        ))
    }

    // Send a message to all users in this room
    fun sendToAll(message: String) {
        for (u in onlineUsers.removeClosed()) {
            u.webSocket.remote.sendString(message)
        }
    }

    // Gets called by the ChatSocket when a WebSocket messages comes in
    fun onMessage(message: JsonObject, user: User) {
        when (message.get("type").asString) {
            "login" -> {
                val username = message.get("username").asString
                sendToAll(gson.jsonMap(
                        "type" to "info-login",
                        "username" to username
                ))
                println("$username connected to room ${room.id}")
            }
            "message" -> {
                sendToAll(gson.jsonMap(
                        "type" to "message",
                        "username" to user.username,
                        "message" to message.get("message"),
                        "time" to now().second.millisOfDay
                ))
            }
        }
    }
}
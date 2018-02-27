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

    fun onLogin(user: User) {
        println(onlineUsers)
        for (u in onlineUsers) {
            if (u.publicId == user.publicId) {
                return // This user is logging back in after an unexpected web socket close. Do not send alert
            }
        }
        sendToAll(gson.jsonMap(
                "type" to "info-login",
                "username" to user.username,
                "user-id" to user.publicId
        ))
        println("${user.username} connected to room ${room.id}")
    }

    // Gets called by the ChatSocket when a WebSocket messages comes in
    fun onMessage(message: JsonObject, user: User) {
        when (message.get("type").asString) {
            "message" -> {
                if (!message.has("message")) return
                sendToAll(gson.jsonMap(
                        "type" to "message",
                        "username" to user.username,
                        "user-id" to user.publicId,
                        "message" to message.get("message"),
                        "time" to now().second.millisOfDay
                ))
            }
        }
    }
}
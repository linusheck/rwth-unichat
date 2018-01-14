package me.glatteis.unichat.chat

import com.google.gson.JsonObject
import me.glatteis.unichat.data.Room

/**
 * Created by Linus on 21.12.2017!
 */
class ChatRoom(val id: String, val room: Room) {

    val onlineUsers = HashSet<User>()

    fun onMessage(message: JsonObject, user: User) {
        when (message.get("type").asString) {
            "login" -> {
                val nickname = message.get("username").asString
                println("$nickname connected to room ${room.id}")
            }
            "message" -> {
                println("Recieved chat message: ${message.get("message")}")
            }
        }
    }

}


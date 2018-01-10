package me.glatteis.unichat.chat

import com.google.gson.JsonObject
import me.glatteis.unichat.data.Room
import me.glatteis.unichat.users.User
import org.eclipse.jetty.server.session.Session as JSession

/**
 * Created by Linus on 21.12.2017!
 */
class ChatRoom(val id: String, val room: Room) {

    val onlineUsers = HashSet<User>()


    fun onMessage(message: JsonObject, session: JSession) {
        when (message.get("type").asString) {
            "login" -> {
                val nickname = message.get("nickname").asString

            }
        }
    }

}


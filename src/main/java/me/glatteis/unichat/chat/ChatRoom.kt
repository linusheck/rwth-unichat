package me.glatteis.unichat.chat

import com.google.gson.JsonObject
import me.glatteis.unichat.data.Room
import me.glatteis.unichat.gson
import me.glatteis.unichat.jsonMap
import me.glatteis.unichat.now
import org.eclipse.jetty.util.ConcurrentHashSet
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by Linus on 21.12.2017!
 */
class ChatRoom(val id: String, val room: Room) {

    // Users that have logged out in the past 5 seconds
    private val bufferUsers = ConcurrentHashSet<User>()
    val onlineUsers = ConcurrentHashSet<User>()

    // Removes online users that have closed the connection
    fun removeClosed() {
        onlineUsers.retainAll {
            it.isOpen()
        }
    }

    /**
     * Called by the ChatSocket when someone logs out
      */

    fun onLogout(user: User) {
        // Add this user to buffer users. If the users logs back in within 5 seconds,
        // We will not treat them as logged out.
        bufferUsers.add(user)
        Timer().schedule(5000) {
            removeClosed()
            bufferUsers.remove(user)
            onlineUsers.forEach {
                if (it.publicId == user.publicId) {
                    return@schedule
                }
            }
            sendToAll(gson.jsonMap(
                    "type" to "info-logout",
                    "username" to user.username
            ))
        }
    }

    // Send a message to all users in this room
    fun sendToAll(message: String) {
        removeClosed()
        for (u in onlineUsers) {
            u.webSocket.remote.sendString(message)
        }
    }

    /**
     * Called by ChatSocket when a user logs in, before they are added to onlineUsers
     */
    fun onLogin(user: User) {
        for (u in bufferUsers) {
            if (u.publicId == user.publicId) {
                return // This user is logging back in after an unexpected web socket close. Do not send alert
            }
        }
        sendToAll(gson.jsonMap(
                "type" to "info-login",
                "username" to user.username,
                "user-id" to user.publicId
        ))
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
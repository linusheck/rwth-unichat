package me.glatteis.unichat.chat

import com.google.gson.JsonParser
import me.glatteis.unichat.chatRooms
import me.glatteis.unichat.gson
import me.glatteis.unichat.jsonMap
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

class ChatRoomWebSocket(port: Int) : WebSocketServer(InetSocketAddress(port)) {



    private val socketsToRooms = HashMap<WebSocket, User>()

    override fun onOpen(connection: WebSocket, handshake: ClientHandshake) {

    }

    override fun onClose(connection: WebSocket, code: Int, reason: String?, remote: Boolean) {
        socketsToRooms.remove(connection)
    }

    override fun onMessage(socket: WebSocket, messageAsString: String) {
        println(messageAsString)
        val jsonParser = JsonParser()
        val message = jsonParser.parse(messageAsString).asJsonObject
        if (message["type"].asString == "login") {
            val roomString = message.get("room").asString
            val username = message.get("username").asString
            val chatRoom = chatRooms[roomString]
            if (chatRoom == null) {
                socket.send(gson.jsonMap(
                        "type" to "error",
                        "reason" to "room_does_not_exist"
                ))
            } else {
                val user = User(chatRoom, username, socket)
                socketsToRooms[socket] = user
                chatRoom.onMessage(message, user)
            }
        } else {
            val user = socketsToRooms[socket]
            if (user == null) {
                socket.send(gson.jsonMap(
                        "type" to "error",
                        "reason" to "you_are_not_logged_in"
                ))
                return
            }
            user.room.onMessage(message, user)
        }

    }

    override fun onStart() {
    }

    override fun onError(p0: WebSocket?, p1: Exception?) {
    }

}
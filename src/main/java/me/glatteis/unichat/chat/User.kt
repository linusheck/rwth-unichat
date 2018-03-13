package me.glatteis.unichat.chat

import org.eclipse.jetty.websocket.api.Session

data class User(val room: ChatRoom, val username: String, val publicId: String, val webSocket: Session) {

    fun isOpen() = webSocket.isOpen

}
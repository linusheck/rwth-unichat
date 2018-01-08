package me.glatteis.unichat.users

import org.eclipse.jetty.websocket.api.Session

/**
 * Created by Linus on 21.12.2017!
 */
class User(val nick: String) {
    var session: Session? = null
}
package me.glatteis.unichat

import com.google.gson.Gson
import org.eclipse.jetty.websocket.api.Session

fun <A, B> Gson.jsonMap(vararg pairs: Pair<A, B>): String {
    return toJson(pairs.toMap())
}

fun Session.error(reason: String, id: ErrorCode) = remote.sendString(gson.jsonMap(
        "type" to "error",
        "reason" to reason,
        "id" to id.value
))
package me.glatteis.unichat

import com.fatboyindustrial.gsonjodatime.Converters
import com.google.gson.Gson

import com.google.gson.GsonBuilder
import me.glatteis.unichat.chat.ChatRoom
import me.glatteis.unichat.chat.ChatSocket
import me.glatteis.unichat.data.UniData
import spark.kotlin.halt
import spark.kotlin.ignite
/**
 * Created by Linus on 19.12.2017!
 */

val chatRooms = HashMap<String, ChatRoom>()

val gson: Gson = Converters.registerAll(GsonBuilder()).create()

fun main(args: Array<String>) {
    UniData.init()
    val portAsString = if (args.isNotEmpty()) args[0] else "4567"
    val thisPort = portAsString.toInt()

    ChatSocket.initSocket()

    val http = ignite().port(thisPort)

    http.before {
        response.header("Access-Control-Allow-Origin", "*")
    }
    // Returns a complete list of rooms
    var allAsSendableCache = Pair(UniData.allAsSendable(), System.currentTimeMillis())
    http.get("/allrooms") {
        type("application/json")
        if (System.currentTimeMillis() - allAsSendableCache.second > 60000) {
            allAsSendableCache = Pair(UniData.allAsSendable(), System.currentTimeMillis())
        }
        allAsSendableCache.first
    }
    // Returns a list of rooms matching a query, in descending order of number of seats available
    http.before("/searchrooms") {
        val query = request.queryParams("q")
        if (query == null || query.isBlank()) {
            halt(403, "Query is null or empty")
        }
    }
    http.get("/searchrooms") {
        type("application/json")
        val query = request.queryParams("q")
        UniData.findRoomsInJson(query)
    }

}

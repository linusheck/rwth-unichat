package me.glatteis.unichat

import com.fatboyindustrial.gsonjodatime.Converters
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.glatteis.unichat.chat.ChatRoom
import me.glatteis.unichat.chat.ChatSocket
import me.glatteis.unichat.data.UniData
import spark.Filter
import spark.Spark.*


/**
 * Created by Linus on 19.12.2017!
 */

val chatRooms = HashMap<String, ChatRoom>()
val gson: Gson = Converters.registerAll(GsonBuilder()).create()
const val DO_NOT_UPDATE = false

fun main(args: Array<String>) {
    UniData.init()

    val portAsString = if (args.isNotEmpty()) args[0] else "4567"
    val thisPort = portAsString.toInt()

    webSocket("/chatsocket", ChatSocket)
    port(thisPort)


    before(Filter { _, response ->
        response.header("Access-Control-Allow-Origin", "*")
    })


    // Returns a complete list of rooms
    get("/allrooms") { _, response ->
        response.type("application/json")
        UniData.allAsSendable()
    }
    // Returns a list of rooms matching a query, in descending order of number of seats available
    before("/searchrooms") { request, _ ->
        val query = request.queryParams("q")
        if (query == null || query.isBlank()) {
            halt(403, "Query is null or empty")
        }
    }
    get("/searchrooms") { request, response ->
        response.type("application/json")
        val query = request.queryParams("q")
        UniData.findRoomsInJson(query)
    }

}


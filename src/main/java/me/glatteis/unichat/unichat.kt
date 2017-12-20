package me.glatteis.unichat

import me.glatteis.unichat.crawler.UniData
import spark.kotlin.halt
import spark.kotlin.ignite
import spark.kotlin.port

/**
 * Created by Linus on 19.12.2017!
 */


fun main(args: Array<String>) {
    val portAsString = if (args.isNotEmpty()) args[0] else "4567"
    val thisPort = portAsString.toInt()
    UniData.init()

    val http = ignite().port(thisPort)
    // Returns a complete list of rooms
    http.get("/allrooms") {
        type("application/json")
        UniData.allAsSendable()
    }
    // Returns a list of rooms matching a query, in descending order of number of seats available
    http.before("/searchrooms") {
        val query = request.queryParams("q")
        if (query == null || query.isBlank()) {
            halt(403, "Query is null or empty")
        }
    }
    http.get("/searchrooms") {
        val query = request.queryParams("q")
        type("application/json")
        UniData.findRoomsInJson(query)
    }
}
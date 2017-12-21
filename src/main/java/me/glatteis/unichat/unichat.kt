package me.glatteis.unichat

import me.glatteis.unichat.crawler.UniData
import spark.kotlin.halt
import spark.kotlin.ignite

/**
 * Created by Linus on 19.12.2017!
 */

fun main(args: Array<String>) {
    UniData.init()
    val portAsString = if (args.isNotEmpty()) args[0] else "4567"
    val thisPort = portAsString.toInt()

    val http = ignite().port(thisPort)
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
        val query = request.queryParams("q")
        type("application/json")
        UniData.findRoomsInJson(query)
    }
}
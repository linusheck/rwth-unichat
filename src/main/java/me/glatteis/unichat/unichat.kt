import me.glatteis.unichat.crawler.UniData
import spark.kotlin.after
import spark.kotlin.halt
import spark.kotlin.ignite

/**
 * Created by Linus on 19.12.2017!
 */



fun main(args: Array<String>) {
    UniData.loadFromJson()

    val http = ignite()
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
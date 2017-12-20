package me.glatteis.unichat.crawler

import com.fatboyindustrial.gsonjodatime.Converters
import com.google.gson.GsonBuilder
import me.glatteis.unichat.data.Room
import me.glatteis.unichat.data.SendableRoom
import me.glatteis.unichat.now
import java.io.File
import kotlin.concurrent.thread

/**
 * Created by Linus on 19.12.2017!
 */
object UniData {

    private val rooms = ArrayList<Room>()
    private val gson = Converters.registerAll(GsonBuilder()).create()

    fun init() {
        val file =  File("week.json")
        if (!file.exists()) {
            println("Crawling...")
            crawlAndSave()
            println("Done.")
        } else {
            println("Loading file...")
            loadFromJson()
            println("Done.")
        }
    }

    fun crawl() {
        val crawler = Crawler()
        rooms.clear()
        rooms.addAll(crawler.getEverything())
    }

    fun crawlAndSave() {
        thread {
            crawl()
            File("week.json").writeText(asJson())
        }
    }

    fun loadFromJson() {
        val loadedList: Array<Room> = gson.fromJson<Array<Room>>(File("week.json").readText(), Array<Room>::class.java)
        rooms.clear()
        rooms.addAll(loadedList)
    }

    fun asJson(): String {
        return gson.toJson(rooms.toArray())
    }

    fun allAsSendable() : String {
        val (weekday, time) = now()
        val sendRooms = rooms.map {
            it.sendable(weekday, time)
        }.sortedByDescending {
            it.seats
        }

        val buildings = HashMap<String, ArrayList<SendableRoom>>()

        for (r in sendRooms) {
            if (!buildings.containsKey(r.address)) {
                buildings[r.address] = ArrayList()
            }
            buildings[r.address]!!.add(r)
        }

        return gson.toJson(mapOf("buildings" to buildings))
    }

    fun findRoomsInJson(query: String): String {
        val (weekday, time) = now()
        val sublist = rooms.filter {
            it.name.contains(query, ignoreCase = true)
        }.sortedByDescending {
            it.seats
        }.map {
            it.sendable(weekday, time)
        }
        return gson.toJson(mapOf("query" to query, "rooms" to sublist))
    }

}
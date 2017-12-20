package me.glatteis.unichat.crawler

import com.fatboyindustrial.gsonjodatime.Converters
import com.google.gson.GsonBuilder
import me.glatteis.unichat.data.Room
import me.glatteis.unichat.data.SendableRoom
import me.glatteis.unichat.data.Weekday
import me.glatteis.unichat.now
import org.joda.time.DateTime
import org.joda.time.LocalTime
import java.io.File

/**
 * Created by Linus on 19.12.2017!
 */
object UniData {

    private val rooms = ArrayList<Room>()
    private val gson = Converters.registerAll(GsonBuilder()).create()

    fun crawl() {
        val crawler = Crawler()
        rooms.clear()
        rooms.addAll(crawler.getEverything())
    }

    fun crawlAndSave() {
        crawl()
        File("week.json").writeText(asJson())
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
        return  gson.toJson(rooms.map {
            it.sendable(weekday, time)
        })
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
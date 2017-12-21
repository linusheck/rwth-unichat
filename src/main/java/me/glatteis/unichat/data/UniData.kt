package me.glatteis.unichat.data

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import me.glatteis.unichat.crawler.Crawler
import me.glatteis.unichat.crawler.RandomStringGenerator
import me.glatteis.unichat.gson
import me.glatteis.unichat.now
import java.io.File
import java.security.SecureRandom

/**
 * Created by Linus on 19.12.2017!
 */
object UniData {

    private val rooms = ArrayList<Room>()
    val roomIds: BiMap<Room, String> = HashBiMap.create<Room, String>()
    private val stringGenerator = RandomStringGenerator(SecureRandom())

    fun init() {
        val file = File("week.json")
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

    private fun crawl() {
        val crawler = Crawler()
        swapRooms(crawler.getEverything())
    }

    private fun crawlAndSave() {
        crawl()
        File("week.json").writeText(asJson())
    }

    private fun loadFromJson() {
        val loadedList: Array<Room> = gson.fromJson<Array<Room>>(File("week.json").readText(), Array<Room>::class.java)
        swapRooms(loadedList.toList())
    }

    private fun swapRooms(newList: List<Room>) {
        rooms.clear()
        rooms.addAll(newList)
        for (r in rooms) {
            roomIds[r] = stringGenerator.randomString(10)
        }
    }

    fun asJson(): String {
        return gson.toJson(rooms.toArray())
    }

    fun allAsSendable(): String {
        val (weekday, time) = now()
        val sendRooms = rooms.map {
            it.sendable(weekday, time)
        }.sortedByDescending {
            it.seats
        }

        val buildings = HashMap<String, ArrayList<SendableRoom>>()
        val numRooms = HashMap<String, Int>()

        for (r in sendRooms) {
            if (!buildings.containsKey(r.building)) {
                buildings[r.building] = ArrayList()
            }
            numRooms[r.building] = numRooms.getOrDefault(r.building, 0) + r.seats
            buildings[r.building]!!.add(r)
        }

        val sortedBuildings = buildings.toSortedMap(Comparator { o1, o2 -> numRooms[o2]?.compareTo(numRooms[o1] ?: 0) ?: 0 })
        return gson.toJson(mapOf("buildings" to sortedBuildings.map {
            mapOf("name" to it.key, "rooms" to it.value)
        }))
    }

    fun findRoomsInJson(query: String): String {
        val (weekday, time) = now()
        val sublist = rooms.map {
            it.sendable(weekday, time)
        }.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.current.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true) ||
                    it.id.contains(query, ignoreCase = true)
        }.sortedByDescending {
            it.seats
        }
        return gson.toJson(mapOf("query" to query, "rooms" to sublist))
    }

}
package me.glatteis.unichat.data

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import me.glatteis.unichat.crawler.Crawler
import me.glatteis.unichat.crawler.RandomStringGenerator
import me.glatteis.unichat.gson
import me.glatteis.unichat.now
import org.joda.time.DateTime
import java.io.File
import java.security.SecureRandom
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

/**
 * Created by Linus on 19.12.2017!
 */
object UniData {

    private val rooms = ArrayList<Room>()
    private lateinit var lastUpdate: DateTime
    val roomIds: BiMap<Room, String> = HashBiMap.create<Room, String>()
    private val stringGenerator = RandomStringGenerator(SecureRandom())

    // Prevent ConcurrentModificationException without much hassle
    var readBlock = false

    fun init() {
        val file = File("week.json")
        if (!file.exists()) {
            println("Crawling...")
            crawlAndSave()
            println("Done.")
        } else {
            println("Loading file...")
            try {
                loadFromJson()
            } catch (e: Exception) {
                e.printStackTrace()
                println("Deleting file and trying again")
                file.delete()
                init()
                return
            }
            println("Done.")
        }

        timer(period = 60 * 60 * 1000L, daemon = true) {
            println("Checking data age")
            if (lastUpdate.isBefore(DateTime.now().minusHours(4))) {
                println("Data is too old")
                thread {
                    println("Crawling for new data...")
                    crawlAndSave()
                    println("Done.")
                }
            }
        }
    }

    private fun crawl() {
        val crawler = Crawler()
        swapRooms(crawler.getEverything())
    }

    private fun crawlAndSave() {
        crawl()
        File("week.json").writeText(asJson())
        lastUpdate = DateTime.now()
    }

    private fun loadFromJson() {
        val loadedList: Data = gson.fromJson<Data>(File("week.json").readText(), Data::class.java)
        lastUpdate = loadedList.lastUpdate
        swapRooms(loadedList.rooms.toList())
    }

    private fun swapRooms(newList: List<Room>) {
        readBlock = true
        val existingChatRooms = HashMap<Room, String>()
        for (room in newList) {
            val sameRooms = rooms.filter {
                it.name == room.name && it.building == room.building && it.address == room.address
            }
            if (sameRooms.isNotEmpty()) {
                existingChatRooms[room] = roomIds[sameRooms[0]] ?: continue
            }
        }
        rooms.clear()
        rooms.addAll(newList)
        roomIds.clear()
        for (r in rooms) {
            roomIds[r] = if (existingChatRooms.containsKey(r)) existingChatRooms[r] else stringGenerator.randomString(10)
        }
        readBlock = false
    }

    private fun asJson(): String {
        return gson.toJson(Data(rooms.toTypedArray(), DateTime.now()))
    }

    fun allAsSendable(): String {
        if (readBlock) return ""
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
        if (readBlock) return ""
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

    private data class Data(val rooms: Array<Room>, val lastUpdate: DateTime) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Data

            if (!Arrays.equals(rooms, other.rooms)) return false
            if (lastUpdate != other.lastUpdate) return false

            return true
        }

        override fun hashCode(): Int {
            var result = Arrays.hashCode(rooms)
            result = 31 * result + lastUpdate.hashCode()
            return result
        }
    }

}
package me.glatteis.unichat.data

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import me.glatteis.unichat.DO_NOT_UPDATE
import me.glatteis.unichat.chat.ChatRoom
import me.glatteis.unichat.chatRooms
import me.glatteis.unichat.crawler.Crawler
import me.glatteis.unichat.crawler.RandomStringGenerator
import me.glatteis.unichat.gson
import me.glatteis.unichat.now
import org.joda.time.DateTime
import java.io.File
import java.security.SecureRandom
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import kotlin.concurrent.timer

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
        // Load the file with data
        val file = File("week.json")
        println(file.exists())
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

        // If data is too old, crawl the servers for new data
        timer(period = 60 * 60 * 1000L, daemon = true) {
            if (lastUpdate.isBefore(DateTime.now().minusHours(4)) && !DO_NOT_UPDATE) {
                println("Data is too old")
                thread {
                    try {
                        println("Crawling for new data...")
                        crawlAndSave()
                        println("Done.")
                    } catch (e: Exception) {
                        println("Failed: $e")
                    }
                }
            }
        }
    }

    // Crawl for new data and set that as the current data
    private fun crawl() {
        val crawler = Crawler()
        val rooms = crawler.getEverything() + Room("Unichat Chat", "general", "",
                Int.MAX_VALUE, "General", RoomCalendar(ArrayList()))
        swapRooms(rooms)
    }

    // Crawl for new data, set that as the current data, save it in the file week.json
    private fun crawlAndSave() {
        crawl()
        File("week.json").writeText(asJson())
        lastUpdate = DateTime.now()
    }

    // Load week.json file and set it as current data
    private fun loadFromJson() {
        val loadedList: Data = gson.fromJson<Data>(File("week.json").readText(), Data::class.java)
        lastUpdate = loadedList.lastUpdate
        swapRooms(loadedList.rooms.toList())
    }

    // Set data as current data
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
        for ((room, id) in roomIds) {
            if (!chatRooms.containsKey(id)) {
                chatRooms[id] = ChatRoom(id, room)
            }
        }
        readBlock = false
    }

    // Return current data as JSON for internal storage
    private fun asJson(): String {
        if (readBlock) return ""
        return gson.toJson(Data(rooms.toTypedArray(), DateTime.now()))
    }

    // Return current data as sendable JSON
    fun allAsSendable(): String {
        if (readBlock) return ""
        val (weekday, time) = now()
        val sendRooms = rooms.map {
            it.sendable(weekday, time)
        }.sortedByDescending { it.seats }

        val buildings = HashMap<String, ArrayList<SendableRoom>>()
        val numRooms = HashMap<String, Int>()

        for (r in sendRooms) {
            if (!buildings.containsKey(r.building)) {
                buildings[r.building] = ArrayList()
            }
            numRooms[r.building] = numRooms.getOrDefault(r.building, 0) + r.seats
            buildings[r.building]!!.add(r)
        }

        val sortedBuildings = buildings.toSortedMap(Comparator { o1, o2 ->
            numRooms[o2]?.compareTo(numRooms[o1] ?: 0) ?: 0
        })
        return gson.toJson(mapOf("buildings" to sortedBuildings.map {
            mapOf("name" to it.key, "rooms" to it.value)
        }))
    }

    // Return query as sendable JSON
    fun findRoomsInJson(query: String): String {
        if (readBlock) return ""
        val (weekday, time) = now()
        val sublist = rooms.map {
            it.sendable(weekday, time)
        }.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.current.contains(query, ignoreCase = true) ||
                            it.address.contains(query, ignoreCase = true) ||
                            it.building.contains(query, ignoreCase = true) ||
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
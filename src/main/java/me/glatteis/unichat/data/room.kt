package me.glatteis.unichat.data

import me.glatteis.unichat.chatRooms
import org.joda.time.LocalTime
import java.util.*

data class Room(val name: String, val id: String, val address: String, val seats: Int, val building: String,
                val calendar: RoomCalendar) {
    fun sendable(day: Weekday, time: LocalTime): SendableRoom {
        val current = calendar.occurrences.filter {
            it.weekday == day && it.start.isBefore(time) && it.end.isAfter(time)
        }
        val toReturn = if (current.isEmpty()) {
            "Keine Veranstaltung"
        } else {
            current.joinToString(", ") {
                it.name
            }
        }
        val chatRoomId = UniData.roomIds[this] ?: ""
        val onlineUsers = chatRooms[chatRoomId]?.onlineUsers?.size ?: 0
        return SendableRoom(name, id, address, seats, building, toReturn, chatRoomId, onlineUsers)
    }
}

data class SendableRoom(val name: String, val id: String, val address: String, val seats: Int, val building: String,
                        val current: String, val chatRoomId: String, val onlineUsers: Int)

enum class Weekday {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    companion object {
        fun get(weekday: Int): Weekday {
            return Weekday.values()[weekday]
        }
    }
}

data class Occurrence(val name: String, val start: LocalTime, val end: LocalTime, val weekday: Weekday)

data class RoomCalendar(val occurrences: List<Occurrence>)

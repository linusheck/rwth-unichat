package me.glatteis.unichat.data

import org.joda.time.LocalTime
import java.util.*

data class Room(val name: String, val id: String, val address: String, val seats: Int, val building: String,
                val calendar: RoomCalendar) {
    fun sendable(day: Weekday, time: LocalTime): SendableRoom {
        val current = calendar.occurrences.filter {
            it.weekday == day && it.start.isBefore(time) && it.end.isAfter(time)
        }
        val toReturn = if (current.isEmpty()) {
            ""
        } else {
            current.joinToString(", ") {
                it.name
            }
        }
        return SendableRoom(name, id, address, seats, building,  toReturn, UniData.roomIds[this] ?: "")
    }
}

data class SendableRoom(val name: String, val id: String, val address: String, val seats: Int, val building: String,
                        val current: String, val chatRoomId: String)

enum class Weekday {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    companion object {
        fun get(weekday: Int): Weekday {
            return Weekday.values()[weekday]
        }
    }
}

data class Occurrence(val name: String, val start: LocalTime, val end: LocalTime, val weekday: Weekday)

data class RoomCalendar(val occurrences: ArrayList<Occurrence>)

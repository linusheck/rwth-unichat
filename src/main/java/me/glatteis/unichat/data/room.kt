package me.glatteis.unichat.data

import org.joda.time.LocalTime
import java.io.Serializable
import java.util.*

data class Room(val name: String, val id: String, val address: String, val calendar: RoomCalendar): Serializable

enum class Weekday: Serializable{
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
    companion object {
        fun get(weekday: Int): Weekday {
            return Weekday.values()[weekday]
        }
    }
}

data class Occurrence(val name: String, val start: LocalTime, val end: LocalTime, val weekday: Weekday): Serializable

data class RoomCalendar(val occurrences: ArrayList<Occurrence>): Serializable

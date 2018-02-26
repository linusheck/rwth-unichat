package me.glatteis.unichat

import me.glatteis.unichat.data.Weekday
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalTime
import java.util.*

/**
 * Created by Linus on 20.12.2017!
 */

private val timeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Berlin"))

fun now(): Pair<Weekday, LocalTime> {
    val time = LocalTime.now(timeZone)
    val weekday = Weekday.get((DateTime.now(timeZone).dayOfWeek().get() - 1) % 7)
    return Pair(weekday, time)
}
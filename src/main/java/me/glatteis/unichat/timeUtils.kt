package me.glatteis.unichat

import me.glatteis.unichat.data.Weekday
import org.joda.time.DateTime
import org.joda.time.LocalTime

/**
 * Created by Linus on 20.12.2017!
 */

fun now() : Pair<Weekday, LocalTime> {
    val time = LocalTime.now()
    val weekday = Weekday.get((DateTime.now().dayOfWeek().get() - 1) % 7)
    return Pair(weekday, time)
}
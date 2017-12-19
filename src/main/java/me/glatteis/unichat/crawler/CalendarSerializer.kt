package me.glatteis.unichat.crawler

import com.google.gson.Gson
import java.io.File

/**
 * Created by Linus on 19.12.2017!
 */
class CalendarSerializer {

    fun crawlAndSave() {
        val crawler = Crawler()
        val rooms = crawler.getEverything()
        println(rooms)
        val gson = Gson()
        val json = gson.toJson(rooms)
        println(json)
        File("week.json").writeText(json)
    }

}
package me.glatteis.unichat.crawler

import me.glatteis.unichat.data.Occurrence
import me.glatteis.unichat.data.Room
import me.glatteis.unichat.data.RoomCalendar
import me.glatteis.unichat.data.Weekday
import org.joda.time.format.DateTimeFormat
import org.jsoup.Jsoup

class Crawler {

    val pageBeginning = "http://www.campus.rwth-aachen.de/rwth/all/"

    fun getEverything(): List<Room> {
        return openRoomGroupPage()
    }

    private fun openRoomGroupPage() : List<Room> {
        val pageName = pageBeginning + "roomGroups.asp"
        val document = Jsoup.connect(pageName).get()
        println(document.title())

        // First of all, let's get the table elements with the room categories

        val allLinks = ArrayList<String>()

        for (tableElement in document.getElementsByClass("hierarchy0")) {
            val tableLinks = tableElement.getElementsByClass("link3")
            if (tableLinks.size < 2) {
                continue
            }
            val tableLink = tableLinks[0]
            if (tableLink.children().size < 3) {
                continue
            }
            val link = tableLink.child(2)
            allLinks.add(link.attr("href"))
        }

        // For every room in this part of the uni, get detailed info
        val rooms = ArrayList<Room>()

        for (link in allLinks) {
            val subDocument = Jsoup.connect(pageBeginning + link).get()
            println(subDocument.title())
            val table = subDocument.getElementsByClass("print")[0]
                    .getElementsByTag("tbody")[0]
            for (element in table.allElements) {
                if (!element.className().startsWith("blue")) continue
                val tds = element.getElementsByTag("td")
                val id = tds[0].html()
                val name = tds[1].html()
                val address = tds[2].html()
                val timesLink = tds[7].getElementsByTag("a").attr("href")
                val calendar = getTimes(timesLink)
                val room = Room(name, id, address, calendar)
                rooms.add(room)
                print("#")
            }
            println()
        }
        println(rooms)
        return rooms
    }

    private val leftBeginning = 261 // Beginning of occurrences' x - position in the calendar corresponding to weekday
    private val leftShift = 120 // Shift of occurrences' x - position in the calendar corresponding to weekday

    /**
     * Get the full calendar corresponding to a relative link
     */
    private fun getTimes(link: String): RoomCalendar {
        val subDocument = Jsoup.connect(pageBeginning + link).get()
        val calendarTable = subDocument.getElementsByAttributeValueContaining("style", "overflow:hidden; font-size:")
        val occurrences = ArrayList<Occurrence>()
        for (e in calendarTable) {
            if (e.allElements.size < 2) continue
            val titleElement = e.allElements[1]
            val title = titleElement.attr("title")
            if (e.textNodes().isEmpty()) {
                continue
            }
            val htmlTime = e.textNodes()[0].wholeText
            if (!htmlTime.matches(Regex("[0-9][0-9]:[0-9][0-9]-[0-9][0-9]:[0-9][0-9]"))) {
                continue
            }
            val time = htmlTime.split("-")
            val weekdayCoords = e.attr("style").split(";").filter { it.contains("left") }[0]
                    .replace("left:", "").replace("px", "").trim().toInt()
            val weekday = Weekday.get((weekdayCoords - leftBeginning) / leftShift)
            val format = DateTimeFormat.forPattern("HH:mm")
            val start = format.parseLocalTime(time[0])
            val end = format.parseLocalTime(time[1])
            val occurrence = Occurrence(title, start, end, weekday)
            occurrences.add(occurrence)
        }
        return RoomCalendar(occurrences)
    }

}
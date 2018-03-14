package me.glatteis.unichat.crawler

import java.util.*

/**
 * Created by Linus on 19.03.2017!
 */
class RandomStringGenerator(private val random: Random) {

    private val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()

    fun randomString(len: Int): String {
        val sb = StringBuilder(len)
        for (i in 0..(len - 1))
            sb.append(alphabet[random.nextInt(alphabet.size)])
        return sb.toString()
    }

}
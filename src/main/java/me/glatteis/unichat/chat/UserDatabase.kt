package me.glatteis.unichat.chat

import com.google.common.hash.Hashing
import com.google.gson.Gson
import me.glatteis.unichat.crawler.RandomStringGenerator
import me.glatteis.unichat.gson
import java.io.File
import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.*

object UserDatabase {

    // Hash table privateId(sha256) -> publicId
    private val users = HashMap<String, DataUser>()
    private val randomStringGenerator = RandomStringGenerator(SecureRandom())

    /**
     * Get a user's public ID by their private ID.
     * @return The public ID of this user.
     */
    fun getPublicId(privateId: String): String {
        val privateKey = Hashing.sha256().hashString(privateId, Charset.defaultCharset()).toString()
        var publicId = users[privateKey]?.publicId
        if (publicId == null) {
            do {
                publicId = randomStringGenerator.randomString(16)
            } while (users.containsKey(publicId))
            users[privateKey] = DataUser(privateKey, publicId!!)
        }
        return publicId
    }

    class DataUser(val privateId: String, val publicId: String)
    class DataUserList(val list: List<DataUser>)

    init {
        loadFromFile()
    }

    //todo save to file

    // If a file exists, load it
    fun loadFromFile() {
        val file = File("user_database.json")
        if (file.exists()) {
            val loadedList: DataUserList = gson.fromJson<DataUserList>(file.readText(), DataUserList::class.java)
            users.clear()
            loadedList.list.forEach {
                users[it.privateId] = it
            }
        }
    }

}
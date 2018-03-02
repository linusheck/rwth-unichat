package me.glatteis.unichat.chat

import com.google.common.hash.Hashing
import me.glatteis.unichat.crawler.RandomStringGenerator
import me.glatteis.unichat.gson
import java.io.File
import java.nio.charset.Charset
import java.security.SecureRandom

private object UserDatabase {

    class DataUser(val privateId: String, // This is the SHA256 hash of the ID that only the user themselves has
                   val publicId: String, // This is the unique string that identifies this user to other users
                   val buddyList: List<String> // List of publicIds of the users that this users is a buddy of
    )

    // Hash table privateId(sha256) -> user
    private val users = HashMap<String, DataUser>()
    // Hash table publicId -> privateId
    private val privateIds = HashMap<String, String>()

    private val randomStringGenerator = RandomStringGenerator(SecureRandom())

    private val file = File("user_database.json")

    init {
        loadFromFile()
    }

    /**
     * Get a user's public ID by their private ID.
     * If there is no public ID assigned to this user, this will add the new user to the list and save the
     * newly changed data to user_database.json.
     * @return The public ID of this user.
     */
    fun getPublicId(privateId: String): String {
        val privateKey = Hashing.sha256().hashString(privateId, Charset.defaultCharset()).toString()
        var publicId = users[privateKey]?.publicId
        if (publicId == null) {
            do {
                publicId = randomStringGenerator.randomString(16)
            } while (users.containsKey(publicId))
            users[privateKey] = DataUser(privateKey, publicId!!, emptyList())
            privateIds[publicId] = privateId
            saveToFile()
        }
        return publicId
    }

    // Only for putting data into the file.
    // This is obviously only a wrapper, but it's a unique class because Java is stupid with serializing <T> types
    class DataUserList(val list: List<DataUser>)


    /**
     * Iff a file exists, this will load it and assign the contents of users to the contents of the file
     */
    fun loadFromFile() {
        if (file.exists()) {
            val loadedList: DataUserList = gson.fromJson<DataUserList>(file.readText(), DataUserList::class.java)
            users.clear()
            loadedList.list.forEach {
                users[it.privateId] = it
                privateIds[it.publicId] = it.privateId
            }
        }
    }

    /**
     * This will save the contents of users to the file, overwriting what was in it before
     */
    fun saveToFile() {
        val userList = users.map { (_, user) ->
            user
        }
        val jsonList = gson.toJson(DataUserList(userList))
        file.writeText(jsonList)
    }

}
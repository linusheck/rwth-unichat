package me.glatteis.unichat.chat

import com.google.common.hash.Hashing

object UserIdentification {

    fun getPublicId(privateId: String): String {
        return Hashing.sha256().hashString(privateId, charset("utf-8")).toString().substring(0, 16)
    }

}
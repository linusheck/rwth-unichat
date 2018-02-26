package me.glatteis.unichat

import com.google.gson.Gson

fun <A, B> Gson.jsonMap(vararg pairs: Pair<A, B>): String {
    return toJson(pairs.toMap())
}
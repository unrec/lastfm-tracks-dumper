package com.unrec.lastfm.tracks.dumper.utils

import com.unrec.lastfm.tracks.dumper.Constants.defaultPageSize
import com.unrec.lastfm.tracks.dumper.Constants.strategyKey
import com.unrec.lastfm.tracks.dumper.Constants.tokenKey
import com.unrec.lastfm.tracks.dumper.Constants.userKey
import kotlin.system.exitProcess

fun Array<String>.asConfig(): Map<String, String> {

    if (this.size % 2 != 0) {
        println("Incorrect parameters are provided")
        exitProcess(1)
    }

    val map = this.toList().chunked(2).associate { it[0] to it[1] }

    if (!map.keys.contains(userKey)) {
        println("User is not specified")
        exitProcess(1)
    }

    if (!map.keys.contains(tokenKey)) {
        println("API token is not provided")
        exitProcess(1)
    }

    if (map[strategyKey] == null) {
        println("Strategy is not specified, tracks will not be filtered.")
    }

    return map
}

fun countPages(total: Int, pageSize: Int) = kotlin.math.ceil(total.toDouble() / pageSize).toInt()

fun Int.toSitePage() = (this / defaultPageSize + 1) * defaultPageSize

fun userPageUrl(user: String, page: Int) = "https://www.last.fm/user/$user/library?page=$page"

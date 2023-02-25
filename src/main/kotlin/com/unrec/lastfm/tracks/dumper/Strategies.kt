package com.unrec.lastfm.tracks.dumper

import com.unrec.lastfm.tracks.dumper.model.Track

val defaultStrategy = { list: List<Track> -> list }

val withoutDuplicatesStrategy = { list: List<Track> -> list.removeAdjacentDuplicates() }

val duplicatesOnlyStrategy = { list: List<Track> -> list.onlyDuplicates() }

fun <T : Any> Iterable<T>.removeAdjacentDuplicates(): List<T> {
    var last: T? = null
    return mapNotNull {
        if (it == last) {
            null
        } else {
            last = it
            it
        }
    }
}

fun <T : Any> Iterable<T>.onlyDuplicates(): List<T> {
    return this.zipWithNext()
        .filter { it.first == it.second }
        .map { it.second }
}

val strategiesMap = mapOf(
    "default" to defaultStrategy,
    "without-duplicates" to withoutDuplicatesStrategy,
    "only-duplicates" to duplicatesOnlyStrategy
)

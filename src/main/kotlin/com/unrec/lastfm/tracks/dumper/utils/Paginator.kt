package com.unrec.lastfm.tracks.dumper.utils

import com.unrec.lastfm.tracks.dumper.Constants.defaultPageSize
import com.unrec.lastfm.tracks.dumper.Constants.fetchPageSize

class Paginator(totalScrobbles: Int) {

    val defaultPages: Int = countPages(totalScrobbles, defaultPageSize)

    val fetchPages: Int = countPages(totalScrobbles, fetchPageSize)

    val fetchRemainder: Int = totalScrobbles % fetchPageSize

    val defaultRemainder: Int = totalScrobbles % defaultPageSize

    private fun countPages(total: Int, pageSize: Int) = kotlin.math.ceil(total.toDouble() / pageSize).toInt()

    fun countNormalizedIndex(index: Int, page: Int): Int {
        return if (page == fetchPages) {
            fetchRemainder - index
        } else {
            val reversedIndex = fetchPageSize - index
            if (fetchRemainder == 0) {
                (fetchPages - page) * fetchPageSize + reversedIndex
            } else {
                (fetchPages - page - 1) * fetchPageSize + fetchRemainder + reversedIndex
            }
        }
    }

    fun countNormalizedPage(index: Int): Int {
        return if (index <= defaultRemainder) {
            defaultPages
        } else {
            val offsetIndex = index - defaultRemainder - 1
            defaultPages - (offsetIndex / defaultPageSize) - 1
        }
    }
}

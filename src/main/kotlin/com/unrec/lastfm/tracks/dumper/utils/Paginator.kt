package com.unrec.lastfm.tracks.dumper.utils

import com.unrec.lastfm.tracks.dumper.Constants.defaultPageSize
import com.unrec.lastfm.tracks.dumper.Constants.fetchPageSize

class Paginator(totalScrobbles: Int) {

    val defaultPages: Int = countPages(totalScrobbles, defaultPageSize)

    val fetchPages: Int = countPages(totalScrobbles, fetchPageSize)

    val remainder: Int = totalScrobbles % defaultPageSize

    private fun countPages(total: Int, pageSize: Int) = kotlin.math.ceil(total.toDouble() / pageSize).toInt()

    fun countNormalizedIndex(index: Int, page: Int): Int {
        return if (page == fetchPages) {
            remainder - index
        } else {
            val reversedIndex = fetchPageSize - index
            if (remainder == 0) {
                (fetchPages - page) * fetchPageSize + reversedIndex
            } else {
                (fetchPages - page - 1) * fetchPageSize + remainder + reversedIndex
            }
        }
    }

    fun countNormalizedPage(index: Int): Int {
        return if (index <= remainder) {
            defaultPages
        } else {
            val offsetIndex = index - remainder - 1
            defaultPages - (offsetIndex / defaultPageSize) - 1
        }
    }
}

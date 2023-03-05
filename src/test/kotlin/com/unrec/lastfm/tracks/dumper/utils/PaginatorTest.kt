package com.unrec.lastfm.tracks.dumper.utils

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class PaginatorTest {

    companion object {

        private const val totalScrobbles = 234609
        private val paginator = Paginator(totalScrobbles)

        @JvmStatic
        private fun indicesToNormalizeParams(): List<Arguments> {
            val lastFetchPage = paginator.fetchPages
            // 1 - index on the fetch page, 2 - fetch page, 3 - normalized index
            return listOf(
                Arguments.of(2, lastFetchPage, 7),
                Arguments.of(199, lastFetchPage - 1, 10),
                Arguments.of(100, 300, 174709),
                Arguments.of(5, 1, 234604),
            )
        }

        @JvmStatic
        private fun indicesToPages(): List<Arguments> {
            val lastPage = paginator.defaultPages
            return listOf(
                Arguments.of(5, lastPage),
                Arguments.of(15, lastPage - 1),
                Arguments.of(59, lastPage - 1),
                Arguments.of(174207, 1209),
                Arguments.of(234605, 1),
                Arguments.of(234571, 1),
                Arguments.of(totalScrobbles, 1),
            )
        }
    }

    @Nested
    inner class VariablesCountTest {

        @Test
        fun `count default pages`() {
            paginator.defaultPages shouldBe 4693
        }

        @Test
        fun `count fetch pages`() {
            paginator.fetchPages shouldBe 1174
        }

        @Test
        fun `count remainder`() {
            paginator.fetchRemainder shouldBe 9
        }
    }

    @ParameterizedTest
    @MethodSource("indicesToNormalizeParams")
    fun `count normalized index`(index: Int, page: Int, normalizedIndex: Int) {
        paginator.countNormalizedIndex(index, page) shouldBe normalizedIndex
    }

    @ParameterizedTest
    @MethodSource("indicesToPages")
    fun `count normalized page`(index: Int, page: Int) {
        paginator.countNormalizedPage(index) shouldBe page
    }
}

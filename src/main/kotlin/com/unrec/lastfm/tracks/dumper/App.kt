package com.unrec.lastfm.tracks.dumper

import com.unrec.lastfm.tracks.dumper.Constants.baseUrl
import com.unrec.lastfm.tracks.dumper.Constants.fetchPageSize
import com.unrec.lastfm.tracks.dumper.Constants.strategyKey
import com.unrec.lastfm.tracks.dumper.Constants.tokenKey
import com.unrec.lastfm.tracks.dumper.Constants.userKey
import com.unrec.lastfm.tracks.dumper.CsvSchemas.defaultSchema
import com.unrec.lastfm.tracks.dumper.UtilObjects.client
import com.unrec.lastfm.tracks.dumper.UtilObjects.csvMapper
import com.unrec.lastfm.tracks.dumper.UtilObjects.mapper
import com.unrec.lastfm.tracks.dumper.model.Track
import com.unrec.lastfm.tracks.dumper.utils.Paginator
import com.unrec.lastfm.tracks.dumper.utils.extractTracks
import com.unrec.lastfm.tracks.dumper.utils.extractUser
import com.unrec.lastfm.tracks.dumper.utils.recentTracksGetRequest
import com.unrec.lastfm.tracks.dumper.utils.userInfoGetRequest
import kotlinx.coroutines.runBlocking
import me.tongfei.progressbar.ProgressBar
import ru.gildor.coroutines.okhttp.await
import java.io.File
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main(args: Array<String>) {

    val measureTimeMillis = measureTimeMillis {

        defineSettingsFrom(args)
        checkIfUserExists()

        val totalScrobbles = getTotalScrobbles()
        paginator = Paginator(totalScrobbles)

        println("Total scrobbles: $totalScrobbles")
        println("Last.fm pages: ${paginator.defaultPages}")
        println("Total pages to fetch: ${paginator.fetchPages}")

        val tracks = fetchTracks()
        writeToCsv(tracks)
        println("Tracks were found: ${tracks.size}")
    }
    println("Total dump time: ${measureTimeMillis.toDuration(DurationUnit.MILLISECONDS)}")
    exitProcess(0)
}

private lateinit var user: String
private lateinit var token: String
private lateinit var filterStrategy: (List<Track>) -> List<Track>
private lateinit var paginator: Paginator

private val userInfoRequest by lazy { userInfoGetRequest(baseUrl, user, token) }
private val appender by lazy { if (filterStrategy == defaultStrategy) "full" else "duplicates" }

private fun defineSettingsFrom(args: Array<String>) {
    val settings = args.asConfig()
    user = settings[userKey]!!
    token = settings[tokenKey]!!
    filterStrategy = when (val strategy = settings[strategyKey]) {
        null -> defaultStrategy
        else -> strategiesMap[strategy] ?: throw IllegalArgumentException("Incorrect strategy is provided")
    }
}

private fun Array<String>.asConfig(): Map<String, String> {

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

private fun checkIfUserExists() {
    val response = client.newCall(userInfoRequest).execute()
    if (response.code == 404) {
        println("Failed to get data for the '$user' user")
        exitProcess(1)
    }
    response.close()
}

private fun getTotalScrobbles(): Int {
    val response = client.newCall(userInfoRequest).execute()
    val responseBody = response.body?.string()!!
    response.close()
    return mapper.extractUser(responseBody).playCount
}

private fun fetchTracks(): List<Track> {
    val map = ConcurrentHashMap<Int, List<Track>>()
    val pagesToFetch = paginator.fetchPages
    val progressBar = ProgressBar("Pages processed:", pagesToFetch.toLong())

    runBlocking {
        for (page in pagesToFetch downTo 1) {
            runCatching {
                val request = recentTracksGetRequest(baseUrl, user, token, page, fetchPageSize)
                val response = client.newCall(request).await()
                val tracks = mapper.extractTracks(response.body!!.string())

                response.close()

                tracks.withIndex().forEach { (index, track) ->
                    track.index = paginator.countNormalizedIndex(index, page)
                }

                val refinedTracks = tracks.let(filterStrategy)
                for (track in refinedTracks) {
                    val sitePage = paginator.countNormalizedPage(track.index)
                    track.page = sitePage
                    track.pageUrl = userPageUrl(user, sitePage)
                }
                map[page] = refinedTracks
                progressBar.step()
            }.onFailure {
                when (it) {
                    is SocketTimeoutException -> {
                        println("Failed to fetch data from Last.fm due to ${it.javaClass}: ${it.message}")
                        exitProcess(1)
                    }

                    else -> throw it
                }
            }
        }
    }
    progressBar.close()

    val result = mutableListOf<Track>()
    for (page in 1..pagesToFetch) {
        result.addAll(map[page]!!)
    }
    return result
}

private fun userPageUrl(user: String, page: Int) = "https://www.last.fm/user/$user/library?page=$page"

private fun writeToCsv(tracks: List<Track>) {
    val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd")
    val fileName = "${user}_${LocalDate.now().format(formatter)}_${appender}.csv"
    val objectWriter = csvMapper.writerFor(Track::class.java).with(defaultSchema)
    objectWriter.writeValues(File(fileName).bufferedWriter()).writeAll(tracks)
}

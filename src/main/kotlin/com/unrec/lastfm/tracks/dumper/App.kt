package com.unrec.lastfm.tracks.dumper

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvGenerator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.unrec.lastfm.tracks.dumper.Constants.baseUrl
import com.unrec.lastfm.tracks.dumper.Constants.defaultPageSize
import com.unrec.lastfm.tracks.dumper.Constants.fetchPageSize
import com.unrec.lastfm.tracks.dumper.Constants.strategyKey
import com.unrec.lastfm.tracks.dumper.Constants.tokenKey
import com.unrec.lastfm.tracks.dumper.Constants.userKey
import com.unrec.lastfm.tracks.dumper.CsvSchemas.schemaMap
import com.unrec.lastfm.tracks.dumper.model.Track
import com.unrec.lastfm.tracks.dumper.model.UserInfo
import com.unrec.lastfm.tracks.dumper.utils.asConfig
import com.unrec.lastfm.tracks.dumper.utils.countPages
import com.unrec.lastfm.tracks.dumper.utils.extractTracks
import com.unrec.lastfm.tracks.dumper.utils.extractUser
import com.unrec.lastfm.tracks.dumper.utils.recentTracksGetRequest
import com.unrec.lastfm.tracks.dumper.utils.toSitePage
import com.unrec.lastfm.tracks.dumper.utils.userInfoGetRequest
import com.unrec.lastfm.tracks.dumper.utils.userPageUrl
import kotlinx.coroutines.runBlocking
import me.tongfei.progressbar.ProgressBar
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import ru.gildor.coroutines.okhttp.await
import java.io.File
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main(args: Array<String>) {

    val measureTimeMillis = measureTimeMillis {

        // define the settings
        val settings = args.asConfig()
        val user = settings[userKey]!!
        val token = settings[tokenKey]!!
        val filterStrategy = when (val strategy = settings[strategyKey]) {
            null -> defaultStrategy
            else -> strategiesMap[strategy] ?: throw IllegalArgumentException("Incorrect strategy is provided")
        }

        // check if the user exists
        val userInfoRequest = userInfoGetRequest(baseUrl, user, token)
        val userResponse = client.newCall(userInfoRequest).execute()
        if (userResponse.code == 404) {
            println("Failed to get data for the '$user' user")
            exitProcess(1)
        }

        // get the user info for a total pages amount
        val userInfoResponse = client.newCall(userInfoRequest).execute().body?.string()
        val userInfo: UserInfo = mapper.extractUser(userInfoResponse!!)
        val totalScrobbles = userInfo.playCount
        println("Total scrobbles: $totalScrobbles, last.fm pages: ${countPages(totalScrobbles, defaultPageSize)} ")
        val pagesToFetch = countPages(totalScrobbles, fetchPageSize)

        // starting to consume tracks
        val map = ConcurrentHashMap<Int, List<Track>>()
        val progressBar = ProgressBar("Pages processed:", pagesToFetch.toLong())

        println("Starting to load Last.fm data for '$user' user. \nTotal pages to fetch: $pagesToFetch")

        runBlocking {
            for (page in pagesToFetch downTo 1) {
                runCatching {
                    val request = recentTracksGetRequest(baseUrl, user, token, page, fetchPageSize)
                    val response = client.newCall(request).await()
                    val tracks = mapper.extractTracks(response.body?.string()!!)
                    val refinedTracks = tracks.let(filterStrategy)

                    for ((index, track) in refinedTracks.withIndex()) {
                        val sitePage = index.toSitePage()
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

        val tracks = mutableListOf<Track>()
        for (page in 1..pagesToFetch) {
            tracks.addAll(map[page]!!)
        }
        println("Tracks found = ${tracks.size}")

        // save tracks to .csv file
        val schema = schemaMap[filterStrategy]
        val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd")
        val outputFile = File("${user}_${LocalDate.now().format(formatter)}.csv")
        val objectWriter = csvMapper.writerFor(Track::class.java).with(schema)
        objectWriter.writeValues(outputFile.bufferedWriter()).writeAll(tracks)
    }
    println("Total dump time = ${measureTimeMillis.toDuration(DurationUnit.MILLISECONDS)}")
    exitProcess(0)
}

val mapper = ObjectMapper().registerKotlinModule()

private val csvMapper: ObjectMapper = CsvMapper()
    .configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, false)
    .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)

private val client = OkHttpClient.Builder()
    .connectionPool(ConnectionPool(20, 5, TimeUnit.MINUTES))
    .readTimeout(60, TimeUnit.SECONDS)
    .connectTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()

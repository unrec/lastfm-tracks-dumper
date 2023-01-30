package com.unrec.lastfm.tracks.dumper

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvGenerator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.unrec.lastfm.tracks.dumper.model.Track
import com.unrec.lastfm.tracks.dumper.model.UserInfo
import com.unrec.lastfm.tracks.dumper.utils.extractTracks
import com.unrec.lastfm.tracks.dumper.utils.extractUser
import com.unrec.lastfm.tracks.dumper.utils.recentTracksGetRequest
import com.unrec.lastfm.tracks.dumper.utils.userInfoGetRequest
import kotlinx.coroutines.runBlocking
import me.tongfei.progressbar.ProgressBar
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import ru.gildor.coroutines.okhttp.await
import java.io.File
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

        // get user info for total pages value
        val userInfoRequest = userInfoGetRequest(baseUrl, args[0], args[1])

        val userResponse = client.newCall(userInfoRequest).execute()
        if (userResponse.code == 404) {
            println("Failed to get data for the '${args[0]}' user")
            exitProcess(1)
        }

        val userInfoResponse = client.newCall(userInfoRequest).execute().body?.string()
        val userInfo: UserInfo = mapper.extractUser(userInfoResponse!!)
        val totalPages = countPages(userInfo.playCount, pageSize)

        println("Starting to load Last.fm data for '${args[0]}' user. Total pages to fetch: $totalPages")

        // starting to consume tracks
        val map = ConcurrentHashMap<Int, List<Track>>()
        val progressBar = ProgressBar("Pages processed:", totalPages.toLong())

        runBlocking {
            for (page in totalPages downTo 1) {
                val request = recentTracksGetRequest(baseUrl, args[0], args[1], page, pageSize)
                val response = client.newCall(request).await()
                val tracks = mapper.extractTracks(response.body?.string()!!)
                map[page] = tracks
                progressBar.step()
            }
        }
        progressBar.close()

        val tracks = mutableListOf<Track>()
        for (page in 1..totalPages) {
            tracks.addAll(map[page]!!)
        }

        println("Tracks loaded = ${tracks.size}")

        // save tracks to .csv file
        val csvMapper = CsvMapper()
        csvMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
        csvMapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, false)

        val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd")
        val outputFile = File("${args[0]}_${LocalDate.now().format(formatter)}.csv")
        val objectWriter = csvMapper.writerFor(Track::class.java).with(schema)
        objectWriter.writeValues(outputFile.bufferedWriter()).writeAll(tracks)
    }
    println("Total dump time = ${measureTimeMillis.toDuration(DurationUnit.MILLISECONDS)}")
}

private const val baseUrl = "http://ws.audioscrobbler.com/2.0/"
private const val pageSize = 200

private val mapper = ObjectMapper().registerKotlinModule()

private val client = OkHttpClient.Builder()
    .connectionPool(ConnectionPool(20, 5, TimeUnit.MINUTES))
    .connectTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()

private val schema: CsvSchema = CsvSchema.builder()
    .setColumnSeparator(';')
    .disableQuoteChar()
    .setUseHeader(true)
    .addColumn("date")
    .addColumn("artist")
    .addColumn("track")
    .addColumn("album")
    .build()

private fun countPages(total: Int, pageSize: Int) = kotlin.math.ceil(total.toDouble() / pageSize).toInt()






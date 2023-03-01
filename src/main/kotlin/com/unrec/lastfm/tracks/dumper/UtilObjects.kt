package com.unrec.lastfm.tracks.dumper

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvGenerator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object UtilObjects {

    val mapper = ObjectMapper().registerKotlinModule()

    val csvMapper: ObjectMapper = CsvMapper()
        .configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, false)
        .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)

    val client =
        OkHttpClient.Builder()
            .connectionPool(ConnectionPool(20, 5, TimeUnit.MINUTES))
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
}

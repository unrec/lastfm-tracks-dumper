package com.unrec.lastfm.tracks.dumper

import com.fasterxml.jackson.dataformat.csv.CsvSchema

object CsvSchemas {

    private val defaultSchema: CsvSchema = CsvSchema.builder()
        .setColumnSeparator(';')
        .disableQuoteChar()
        .setUseHeader(true)
        .addColumn("date")
        .addColumn("artist")
        .addColumn("track")
        .addColumn("album")
        .build()

    private val schemaWithPages: CsvSchema = CsvSchema.builder()
        .setColumnSeparator(';')
        .disableQuoteChar()
        .setUseHeader(true)
        .addColumn("date")
        .addColumn("artist")
        .addColumn("track")
        .addColumn("album")
        .addColumn("page")
        .addColumn("pageLink")
        .build()

    val schemaMap = mapOf(
        defaultStrategy to defaultSchema,
        withoutDuplicatesStrategy to defaultSchema,
        duplicatesOnlyStrategy to schemaWithPages
    )
}


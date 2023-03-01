package com.unrec.lastfm.tracks.dumper

import com.fasterxml.jackson.dataformat.csv.CsvSchema

object CsvSchemas {

    val defaultSchema: CsvSchema = CsvSchema.builder()
        .setColumnSeparator('\t')
        .disableQuoteChar()
        .setUseHeader(true)
        .addColumn("date")
        .addColumn("artist")
        .addColumn("track")
        .addColumn("album")
        .addColumn("page")
        .addColumn("pageUrl")
        .addColumn("index")
        .build()
}

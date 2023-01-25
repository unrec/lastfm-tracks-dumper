package com.unrec.lastfm.tracks.dumper.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = TrackDeserializer::class)
data class Track(
    @field:JsonProperty("track_id")
    val trackId: String?,

    @field:JsonProperty("track_name")
    val trackName: String,

    @field:JsonProperty("artist_id")
    val artistId: String?,

    @field:JsonProperty("artist_name")
    val artistName: String,

    @field:JsonProperty("album_id")
    val albumId: String?,

    @field:JsonProperty("album_name")
    val albumName: String,

    @field:JsonProperty("url")
    val url: String,

    @field:JsonProperty("uts_date")
    val utsDate: Long,

    @field:JsonProperty("date")
    val textDate: String

)

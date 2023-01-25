package com.unrec.lastfm.tracks.dumper.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfo(
    val name: String,

    @field:JsonProperty("realname")
    val realName: String,

    @field:JsonProperty("playcount")
    val playCount: Int,

    @field:JsonProperty("artist_count")
    val artistCount: Int,

    val playlists: Int,

    @field:JsonProperty("track_count")
    val trackCount: Int,

    @field:JsonProperty("album_count")
    val albumCount: Int,

    val url: String,
)

package com.unrec.lastfm.tracks.dumper.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.unrec.lastfm.tracks.dumper.model.Track
import com.unrec.lastfm.tracks.dumper.model.UserInfo

fun ObjectMapper.extractUser(json: String): UserInfo =
    this.readValue(this.readTree(json).get("user").toString())

fun ObjectMapper.extractTracks(json: String): List<Track> =
    this.readValue(
        this.readTree(json)
            .get("recenttracks")
            .get("track")
            .toString()
    )

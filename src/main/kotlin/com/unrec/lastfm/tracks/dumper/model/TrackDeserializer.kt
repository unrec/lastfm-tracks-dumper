package com.unrec.lastfm.tracks.dumper.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

class TrackDeserializer : JsonDeserializer<Track>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Track {
        val node: JsonNode = p.codec.readTree(p)

        return Track(
            trackId = node.getValue(id).stringOrNull(),
            trackName = node.getValue(name),
            artistId = node.getNestedValue(artist, id),
            artistName = node.getNestedValue(artist, text),
            albumId = node.getNestedValue(album, id),
            albumName = node.getNestedValue(album, text),
            url = node.getValue(url),
            utsDate = node.getNestedValue(date, uts).toLong(),
            textDate = node.getNestedValue(date, text)
        )
    }

    private fun String.stringOrNull() = if (this == "") null else this

    private fun JsonNode.getValue(name: String) = this[name].textValue()

    private fun JsonNode.getNestedValue(outerName: String, innerName: String) =
        this[outerName][innerName].textValue()

    companion object {

        const val album = "album"
        const val artist = "artist"
        const val date = "date"
        const val name = "name"
        const val url = "url"
        const val uts = "uts"
        const val id = "mbid"
        const val text = "#text"
    }
}

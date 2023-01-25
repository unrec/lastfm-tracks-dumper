package com.unrec.lastfm.tracks.dumper.utils

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

const val userInfoMethod = "user.getinfo"
const val recentTracksMethod = "user.getrecenttracks"

fun HttpUrl.Builder.auth(token: String) = this.addQueryParameter("api_key", token)
fun HttpUrl.Builder.jsonResponse() = this.addQueryParameter("format", "json")
fun HttpUrl.Builder.limit(size: Int) = this.addQueryParameter("limit", size.toString())
fun HttpUrl.Builder.method(name: String) = this.addQueryParameter("method", name)
fun HttpUrl.Builder.page(page: Int? = null) = this.addQueryParameter("page", page?.toString())
fun HttpUrl.Builder.user(name: String) = this.addQueryParameter("user", name)

fun HttpUrl.Builder.getUserInfo() = method(userInfoMethod)
fun HttpUrl.Builder.getRecentTracks() = method(recentTracksMethod)

fun HttpUrl.toGetRequest() = Request.Builder().url(this).get().build()

fun userInfoGetRequest(baseUrl: String, userName: String, token: String) =
    baseUrl.toHttpUrl().newBuilder()
        .getUserInfo()
        .jsonResponse()
        .user(userName)
        .auth(token)
        .build()
        .toGetRequest()

fun recentTracksGetRequest(baseUrl: String, userName: String, token: String, page: Int? = null, pageSize: Int) =
    baseUrl.toHttpUrl().newBuilder()
        .getRecentTracks()
        .jsonResponse()
        .user(userName)
        .auth(token)
        .page(page)
        .limit(pageSize)
        .build()
        .toGetRequest()




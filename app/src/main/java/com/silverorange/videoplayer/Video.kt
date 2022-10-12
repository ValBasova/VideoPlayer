package com.silverorange.videoplayer

import com.squareup.moshi.Json

data class Video(
    val id: String,
    @Json(name = "fullURL")
    val url: String,
    val title: String,
//    val author: String,
    val description: String
)

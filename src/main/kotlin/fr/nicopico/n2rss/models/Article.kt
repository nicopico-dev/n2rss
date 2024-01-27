package fr.nicopico.n2rss.models

import java.net.URL

data class Article(
    val title: String,
    val link: URL,
    val description: String,
)

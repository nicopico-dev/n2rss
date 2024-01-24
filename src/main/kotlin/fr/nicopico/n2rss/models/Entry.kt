package fr.nicopico.n2rss.models

import kotlinx.datetime.LocalDate
import java.net.URL

data class Entry(
    val title: String,
    val link: URL,
    val description: String,
    val pubDate: LocalDate,
    val source: EntrySource,
)

package fr.nicopico.n2rss.models

import kotlinx.datetime.LocalDate

data class Publication(
    val title: String,
    val date: LocalDate,
    val newsletter: Newsletter,
    val articles: List<Article>,
)

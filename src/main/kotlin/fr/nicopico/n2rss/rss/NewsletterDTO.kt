package fr.nicopico.n2rss.rss

import com.fasterxml.jackson.annotation.JsonProperty

data class NewsletterDTO(
    @JsonProperty("code")
    val code: String,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("publicationCount")
    val publicationCount: Long,
)

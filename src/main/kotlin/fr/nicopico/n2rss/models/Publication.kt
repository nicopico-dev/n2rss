package fr.nicopico.n2rss.models

import kotlinx.datetime.LocalDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "publications")
data class Publication(
    @Id
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val date: LocalDate,
    val newsletter: Newsletter,
    val articles: List<Article>,
)

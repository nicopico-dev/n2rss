package fr.nicopico.n2rss.models

import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import java.net.URL
import kotlin.reflect.KClass

data class Entry(
    val source: KClass<out NewsletterHandler>,
    val title: String,
    val preview: String,
    val url: URL,
)

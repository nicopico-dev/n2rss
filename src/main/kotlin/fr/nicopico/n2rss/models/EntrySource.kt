package fr.nicopico.n2rss.models

import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import kotlin.reflect.KClass

data class EntrySource(
    val handler: KClass<out NewsletterHandler>,
    val title: String,
)

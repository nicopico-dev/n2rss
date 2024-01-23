package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Entry

interface NewsletterHandler {
    fun canHandle(email: Email): Boolean
    fun process(email: Email): Entry
}

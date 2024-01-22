package fr.nicopico.n2rss.mail.processor

import fr.nicopico.n2rss.models.Email

interface EmailProcessor {
    fun canProcess(email: Email): Boolean
    fun process(email: Email)
}

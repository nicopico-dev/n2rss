package fr.nicopico.n2rss.mail.client

import fr.nicopico.n2rss.models.Email

interface EmailClient {
    fun checkEmails(): List<Email>
}

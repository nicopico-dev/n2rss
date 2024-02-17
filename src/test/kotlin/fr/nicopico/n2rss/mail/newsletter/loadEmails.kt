package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.mail.client.ResourceFileEmailClient
import fr.nicopico.n2rss.mail.client.toEmail
import fr.nicopico.n2rss.models.Email
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage

fun loadEmails(folder: String): List<Email> {
    return ResourceFileEmailClient(folder).checkEmails()
}

fun loadEmail(filePath: String): Email {
    val mailSession = Session.getDefaultInstance(System.getProperties())
    return ResourceFileEmailClient::class.java.classLoader
        .getResourceAsStream(filePath)
        .use { inputStream ->
            MimeMessage(mailSession, inputStream)
        }
        .toEmail()
}

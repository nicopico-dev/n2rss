package fr.nicopico.n2rss.mail.client

import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Sender
import fr.nicopico.n2rss.utils.toKotlinLocaleDate
import javax.mail.Message
import javax.mail.internet.MimeMultipart

fun Message.toEmail() = Email(
    Sender(email = from[0].toString()),
    date = this.sentDate.toKotlinLocaleDate(),
    subject = subject,
    content = when(content) {
        is MimeMultipart -> (content as MimeMultipart).getHtmlBodyPart()
        else -> content.toString()
    }
)

private fun MimeMultipart.getHtmlBodyPart(): String {
    val partCount = this.count
    for (i in 0 until partCount) {
        val bodyPart = this.getBodyPart(i)
        if (bodyPart.isMimeType("text/html")) {
            return bodyPart.content as String
        }
    }
    throw NoSuchElementException("no text/html part found in the Message")
}

package fr.nicopico.n2rss.mail.client

import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Sender
import fr.nicopico.n2rss.utils.toKotlinLocaleDate
import javax.mail.Message

fun Message.toEmail() = Email(
    Sender(email = from[0].toString()),
    date = this.sentDate.toKotlinLocaleDate(),
    subject = subject,
    content = content.toString().trim()
)

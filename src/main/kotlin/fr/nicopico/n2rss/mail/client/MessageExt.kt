package fr.nicopico.n2rss.mail.client

import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Sender
import javax.mail.Message

fun Message.toEmail() = Email(
    Sender(from[0].toString()),
    subject,
    content.toString().trim()
)

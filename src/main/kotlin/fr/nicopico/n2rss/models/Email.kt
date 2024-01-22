package fr.nicopico.n2rss.models

data class Email(
    val sender: Sender,
    val subject: String,
    val content: String,
)

@JvmInline
value class Sender(val email: String)

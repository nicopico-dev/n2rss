package fr.nicopico.n2rss.models

import kotlinx.datetime.LocalDate


data class Email(
    val sender: Sender,
    val date: LocalDate,
    val subject: String,
    val content: String,
)

@JvmInline
value class Sender(val email: String)

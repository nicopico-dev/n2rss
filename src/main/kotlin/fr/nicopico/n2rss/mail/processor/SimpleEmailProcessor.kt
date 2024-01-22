package fr.nicopico.n2rss.mail.processor

import fr.nicopico.n2rss.models.Email
import org.springframework.stereotype.Component

@Component
class SimpleEmailProcessor : EmailProcessor {
    override fun canProcess(email: Email): Boolean = true

    override fun process(email: Email) {
        println(email)
    }
}

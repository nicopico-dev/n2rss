package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Entry

class AndroidWeeklyNewsletterHandler : NewsletterHandler {
    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("contact@androidweekly.net")
    }

    override fun process(email: Email): List<Entry> {
        TODO("Not yet implemented")
    }
}

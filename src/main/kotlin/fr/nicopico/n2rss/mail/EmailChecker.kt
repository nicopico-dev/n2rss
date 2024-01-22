package fr.nicopico.n2rss.mail

import fr.nicopico.n2rss.mail.client.EmailClient
import fr.nicopico.n2rss.mail.processor.EmailProcessor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class EmailChecker(
    private val emailClient: EmailClient,
    private val emailProcessors: List<EmailProcessor>,
) {
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    fun checkEmails() {
        val emails = emailClient.checkEmails()
        for (email in emails) {
            for (processor in emailProcessors) {
                if (processor.canProcess(email)) {
                    processor.process(email)
                }
            }
        }
    }
}

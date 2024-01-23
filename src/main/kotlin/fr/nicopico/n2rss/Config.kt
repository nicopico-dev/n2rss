package fr.nicopico.n2rss

import fr.nicopico.n2rss.mail.client.EmailClient
import fr.nicopico.n2rss.mail.client.EmailConfiguration
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import fr.nicopico.n2rss.mail.client.JavaxEmailClient
import fr.nicopico.n2rss.mail.newsletter.PrinterNewsletterHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Config {

    @Bean
    fun emailClient(
        config: EmailConfiguration
    ): EmailClient = JavaxEmailClient(
        protocol = config.protocol,
        host = config.host,
        port = config.port,
        user = config.username,
        password = config.password,
        inboxFolder = config.inboxFolder,
    )

    @Bean
    fun emailProcessors(): List<NewsletterHandler> = listOf(
        PrinterNewsletterHandler()
    )
}

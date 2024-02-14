package fr.nicopico.n2rss

import fr.nicopico.n2rss.mail.client.EmailClient
import fr.nicopico.n2rss.mail.client.EmailConfiguration
import fr.nicopico.n2rss.mail.client.JavaxEmailClient
import fr.nicopico.n2rss.mail.client.ResourceFileEmailClient
import fr.nicopico.n2rss.mail.newsletter.AndroidWeeklyNewsletterHandler
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class Config {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    @Profile("default")
    fun emailClient(config: EmailConfiguration): EmailClient {
        return JavaxEmailClient(
            protocol = config.protocol,
            host = config.host,
            port = config.port,
            user = config.username,
            password = config.password,
            inboxFolder = config.inboxFolder,
        )
    }

    @Bean
    @Profile("local")
    fun fakeEmailClient(): EmailClient = ResourceFileEmailClient("emails")

    @Bean
    fun emailProcessors(): List<NewsletterHandler> = listOf(
        AndroidWeeklyNewsletterHandler()
    )
}

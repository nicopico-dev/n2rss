package fr.nicopico.n2rss.data

import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import fr.nicopico.n2rss.models.Newsletter
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.data.convert.PropertyValueConverter
import org.springframework.data.convert.ValueConversionContext
import org.springframework.stereotype.Component

@Component
class NewsletterValueConverter
    : PropertyValueConverter<Newsletter, String, ValueConversionContext<*>>, ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext

    @Suppress("UNCHECKED_CAST")
    private val handlers: List<NewsletterHandler>
        get() = applicationContext
            .getBean("newsletterHandlers", List::class.java)
                as List<NewsletterHandler>

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun read(value: String, context: ValueConversionContext<*>): Newsletter? {
        return handlers
            .map { it.newsletter }
            .firstOrNull { it.code == value }
    }

    override fun write(value: Newsletter, context: ValueConversionContext<*>): String {
        return value.code
    }
}

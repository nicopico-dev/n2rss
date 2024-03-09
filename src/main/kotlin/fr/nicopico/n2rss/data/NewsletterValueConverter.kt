package fr.nicopico.n2rss.data

import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import fr.nicopico.n2rss.models.Newsletter
import org.springframework.data.convert.PropertyValueConverter
import org.springframework.data.convert.ValueConversionContext
import org.springframework.stereotype.Component

@Component
class NewsletterValueConverter(
    private val handlers: List<NewsletterHandler>
) : PropertyValueConverter<Newsletter, String, ValueConversionContext<*>> {

    override fun read(value: String, context: ValueConversionContext<*>): Newsletter? {
        return handlers
            .map { it.newsletter }
            .firstOrNull { it.code == value }
    }

    override fun write(value: Newsletter, context: ValueConversionContext<*>): String {
        return value.code
    }
}

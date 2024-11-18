/*
 * Copyright (c) 2024 Nicolas PICON
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package fr.nicopico.n2rss.newsletter.handlers

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.newsletter.handlers.jsoup.Section
import fr.nicopico.n2rss.newsletter.handlers.jsoup.extractSections
import fr.nicopico.n2rss.newsletter.handlers.jsoup.hasGrayscaleColor
import fr.nicopico.n2rss.newsletter.handlers.jsoup.process
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.utils.url.toUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component

@Component
class TechReadersNewsletterHandler : NewsletterHandlerSingleFeed {
    override val newsletter: Newsletter = Newsletter(
        code = "tech-readers",
        name = "Tech Readers",
        websiteUrl = "https://share.hsforms.com/1fINml3OxSkaUjbqb9Gy7Ug3b2p9",
        notes = "a newsletter by Tech Rocks"
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email == "hello@tech.rocks"
            && email.subject.matches(EMAIL_SUBJECT_REGEX)
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content,
            Safelist.basic()
                .addAttributes("span", "style")
                .addAttributes("a", "style"),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        val sections = document
            .extractSections(
                cssQuery = "span[style]",
                filter = { it.attr("style").contains(SECTION_STYLE_REGEX) },
                // Stop articles at "La Newsletter faite par et pour les Tech Leaders !"
                stopElement = document
                    .select(":containsOwn(La Newsletter faite par et pour les Tech Leaders !)")
                    .first(),
            )

        // TODO Add newsletter introduction as the first article

        return sections.flatMap { it.process() }
    }

    private fun Section.process(): List<Article> = process { sectionDocument ->
        sectionDocument.select("a[href]")
            .filter { link ->
                link.text().isNotBlank()
                    && !link.hasGrayscaleColor()
            }
            .mapNotNull { tag ->
                // Ignore entries with an invalid link
                tag.attr("href").toUrlOrNull()
                    ?.let { link ->
                        val title = tag.text().trim()
                        Article(
                            title = title,
                            link = link,
                            // TODO Extract article description
                            description = "DESCRIPTION",
                        )
                    }
            }
    }

    companion object {
        private val EMAIL_SUBJECT_REGEX = Regex("Tech Readers #\\d+.*")
        private val SECTION_STYLE_REGEX = Regex("\\bbackground-color\\s*:\\s*#ef7a66;")
    }
}

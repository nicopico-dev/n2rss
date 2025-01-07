/*
 * Copyright (c) 2025 Nicolas PICON
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
import fr.nicopico.n2rss.newsletter.handlers.exception.NewsletterParsingException
import fr.nicopico.n2rss.newsletter.handlers.jsoup.Section
import fr.nicopico.n2rss.newsletter.handlers.jsoup.extractSections
import fr.nicopico.n2rss.newsletter.handlers.jsoup.process
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.utils.url.toUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component

@Component
class KotlinWeeklyNewsletterHandler : NewsletterHandlerMultipleFeeds {

    override val newsletters = listOf(
        defaultNewsletter,
        librariesNewsletter,
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("mailinglist@kotlinweekly.net")
    }

    override fun extractArticles(email: Email): Map<Newsletter, List<Article>> {
        val cleanedHtml = Jsoup.clean(
            email.content,
            Safelist.basic(),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        val sections = document.extractSections("p:has(strong)")

        val articles = sections
            .filter { it.title !in excludedSections }
            .associateWith { it.process() }
            .values
            .flatten()

        val libraries = sections
            .firstOrNull { it.title == LIBRARIES_SECTION_TITLE }
            ?.process()

        return buildMap {
            put(defaultNewsletter, articles)
            if (libraries != null) {
                put(librariesNewsletter, libraries)
            }
        }
    }

    private fun Section.process() = process { sectionDocument ->
        sectionDocument.select("a[href]:has(span)")
            .mapNotNull { tag ->
                // Ignore entries with invalid link
                val link = tag.attr("href").toUrlOrNull()
                    ?: return@mapNotNull null
                val title = markSponsoredTitle(this, tag.text()).trim()

                val description = tag
                    .nextSibling()
                    ?.nextSibling() // <br>
                    ?.nextSibling() // <span> with description
                    ?.let { it as? Element }
                    ?.text()
                    ?: throw NewsletterParsingException(
                        "Cannot find article description for article \"$title\" in Kotlin Weekly"
                    )

                Article(
                    title = title,
                    link = link,
                    description = description,
                )
            }
    }

    private fun markSponsoredTitle(section: Section, articleTitle: String) =
        if (section.title == "Sponsored") {
            "SPONSORED - $articleTitle"
        } else {
            articleTitle
        }

    companion object {
        private const val LIBRARIES_SECTION_TITLE = "Libraries"

        private val excludedSections = listOf("Videos", "Libraries", "Contribute")

        val defaultNewsletter = Newsletter(
            code = "kotlin_weekly",
            name = "Kotlin Weekly",
            websiteUrl = "https://kotlinweekly.net/",
            notes = "Articles",
            feedTitle = "Kotlin Weekly (Articles)",
        )

        val librariesNewsletter = Newsletter(
            code = "kotlin_weekly/libraries",
            name = "Kotlin Weekly",
            websiteUrl = "https://kotlinweekly.net/",
            notes = "Libraries",
            feedTitle = "Kotlin Weekly (Libraries)",
        )
    }
}

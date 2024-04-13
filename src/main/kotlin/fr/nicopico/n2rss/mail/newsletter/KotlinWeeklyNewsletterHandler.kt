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
package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.mail.newsletter.jsoup.Section
import fr.nicopico.n2rss.mail.newsletter.jsoup.extractSections
import fr.nicopico.n2rss.mail.newsletter.jsoup.process
import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.utils.url.toUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist

class KotlinWeeklyNewsletterHandler : NewsletterHandler {

    override val newsletter: Newsletter = Newsletter(
        code = "kotlin_weekly",
        name = "Kotlin Weekly",
        websiteUrl = "http://kotlinweekly.net/",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("mailinglist@kotlinweekly.net")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content,
            Safelist.basic(),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        val sections = document.extractSections("p:has(strong)")
            .filter { it.title !in excludedSections }
        return sections.associateWith { section ->
            section.process { sectionDocument ->
                sectionDocument.select("a[href]")
                    .mapNotNull { tag ->
                        // Ignore entries with invalid link
                        val link = tag.attr("href").toUrlOrNull()
                            ?: return@mapNotNull null
                        val title = markSponsoredTitle(section, tag.text()).trim()

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
        }.values.flatten()
    }

    private fun markSponsoredTitle(section: Section, articleTitle: String) =
        if (section.title == "Sponsored") {
            "SPONSORED - $articleTitle"
        } else {
            articleTitle
        }

    companion object {
        private val excludedSections = listOf("Videos", "Libraries", "Contribute")
    }
}

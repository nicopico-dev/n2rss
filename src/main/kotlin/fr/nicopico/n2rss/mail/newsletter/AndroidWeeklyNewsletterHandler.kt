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
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component

@Component
class AndroidWeeklyNewsletterHandler : NewsletterHandlerMultipleFeeds {

    override val newsletters = listOf(
        articlesNewsletter,
        librariesNewsletter,
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("contact@androidweekly.net")
    }

    override fun extractArticles(email: Email): Map<Newsletter, List<Article>> {
        val cleanedHtml = Jsoup.clean(
            email.content,
            Safelist.basic()
                .addAttributes("span", "style"),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        // Retrieve sections based on the style of the Articles section
        val sectionStyle = document.select("span")
            .first { it.ownText() == ARTICLES_SECTION_TITLE }
            .attr("style")

        val sections = document
            .extractSections(
                "span[style]",
                filter = { it.attr("style") == sectionStyle },
                getSectionTitle = { it.ownText() },
            )
            .filter { it.title in PROCESS_SECTION_TITLES }

        return sections
            .associate { section ->
                val newsletter = when (section.title) {
                    ARTICLES_SECTION_TITLE -> articlesNewsletter
                    LIBRARIES_SECTION_TITLE -> librariesNewsletter
                    else -> throw UnsupportedOperationException(
                        "Section ${section.title} not supported"
                    )
                }

                newsletter to section.process()
            }
    }

    private fun Section.process() = process { sectionDocument ->
        sectionDocument.select("a[href]")
            .filter { link -> link.text().isNotBlank() }
            .mapNotNull { tag ->
                // Ignore entries with invalid link
                tag.attr("href").toUrlOrNull()
                    ?.let { link ->
                        val title = tag.text().trim()
                        Article(
                            title = title,
                            link = link,
                            description = tag.nextSibling()?.toString()?.trim()
                                ?: throw NewsletterParsingException(
                                    "Cannot find article description for article \"$title\" in Android Weekly"
                                ),
                        )
                    }
            }
    }

    companion object {
        private const val ARTICLES_SECTION_TITLE = "Articles & Tutorials"
        private const val LIBRARIES_SECTION_TITLE = "Libraries & Code"

        private val PROCESS_SECTION_TITLES = listOf(ARTICLES_SECTION_TITLE, LIBRARIES_SECTION_TITLE)

        private val articlesNewsletter = Newsletter(
            code = "android_weekly",
            name = "Android Weekly (Articles)",
            websiteUrl = "https://androidweekly.net",
        )

        private val librariesNewsletter = Newsletter(
            code = "android_weekly/libs",
            name = "Android Weekly (Libraries)",
            websiteUrl = "https://androidweekly.net",
        )
    }
}

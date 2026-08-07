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
 * Nicolas PICON
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package fr.nicopico.n2rss.newsletter.handlers

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.mail.models.html
import fr.nicopico.n2rss.newsletter.handlers.exception.NewsletterParsingException
import fr.nicopico.n2rss.newsletter.handlers.jsoup.extractSections
import fr.nicopico.n2rss.newsletter.handlers.jsoup.process
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.utils.url.toUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component

/**
 * [NewsletterHandler] for "AI Dev Weekly" newsletter.
 * Website: https://www.aimadetools.com
 */
@Component
class AiDevWeeklyNewsletterHandler : NewsletterHandlerSingleFeed {

    override val newsletter: Newsletter = Newsletter(
        code = "ai-dev-weekly",
        name = "AI Dev Weekly",
        websiteUrl = "https://www.aimadetools.com",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("hello@aimadetools.com")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.relaxed()
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        val fallbackLink = document.select("a:contains($FULL_ISSUE_LINK_TEXT)")
            .firstOrNull()
            ?.attr("href")
            ?.toUrlOrNull()
            ?: throw NewsletterParsingException("Unable to find fallback link")

        val sections = document.extractSections("h2")

        return sections
            .filter { !it.title.contains(COMING_NEXT_WEEK_TITLE) }
            .flatMap { section ->
                if (section.title.contains(NEW_THIS_WEEK_TITLE)) {
                    section.process { sectionDocument ->
                        sectionDocument.select("li")
                            .mapNotNull { li ->
                                val linkElement = li.select("a[href]").firstOrNull()
                                val title = linkElement?.text()?.cleanText() ?: li.text().cleanText()
                                if (title.isBlank()) return@mapNotNull null

                                val articleLink = linkElement?.attr("href")?.toUrlOrNull() ?: fallbackLink

                                Article(
                                    title = title,
                                    link = articleLink,
                                    description = "",
                                )
                            }
                    }
                } else {
                    section.process { sectionDocument ->
                        val links = sectionDocument.select("a[href]")
                            .mapNotNull { it.attr("href").toUrlOrNull() }
                            .distinct()

                        val articleLink = if (links.size == 1) {
                            links[0]
                        } else {
                            fallbackLink
                        }

                        // The description is the first paragraph of the section
                        val description = sectionDocument.select("p")
                            .firstOrNull { it.select("a:contains($FULL_ISSUE_LINK_TEXT)").isEmpty() }
                            ?.text()
                            ?.cleanText()
                            ?: ""

                        listOf(
                            Article(
                                title = section.title.cleanText(),
                                link = articleLink,
                                description = description,
                            )
                        )
                    }
                }
            }
    }

    private fun String.cleanText(): String = this.trim().replace("\u00A0", " ")

    companion object {
        private const val FULL_ISSUE_LINK_TEXT = "Read the full issue"
        private const val NEW_THIS_WEEK_TITLE = "New This Week"
        private const val COMING_NEXT_WEEK_TITLE = "Coming Next Week"
    }
}

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
import fr.nicopico.n2rss.mail.models.html
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component
import java.net.URL

@Component
class MITWeekendReadsNewsletterHandler : NewsletterHandlerSingleFeed {
    override val newsletter: Newsletter = Newsletter(
        code = "mit/weekend_reads",
        name = "MIT - Weekend Reads",
        websiteUrl = "https://forms.technologyreview.com/newsletters/tech-weekend-reads/",
        notes = "Most articles are behind a paywall",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("promotions@technologyreview.com")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.basic()
                .addTags("h2")
                .addAttributes("h2", "class"),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        return document.select("h2.article-title")
            .mapNotNull { h2 ->
                val link = h2.selectFirst("a[href]")
                    ?.attribute("href")
                    ?.value
                    ?.let { URL(it) }
                    ?: return@mapNotNull null
                val title = h2.text().trim()
                val description = h2.nextElementSiblings().select("p").firstOrNull()?.ownText()
                    // Articles can have no description
                    .orEmpty()

                Article(
                    title = title,
                    link = link,
                    description = description,
                )
            }
    }
}

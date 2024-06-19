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

import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Newsletter
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component
import java.net.URL

@Component
class BuiltForMarsNewsletterHandler : NewsletterHandler {
    override val newsletter: Newsletter = Newsletter(
        code = "builtformars",
        name = "Built for Mars",
        websiteUrl = "https://builtformars.com",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("peter@builtformars.com")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content,
            Safelist.basic()
                .addTags("table", "tr", "td")
                .addAttributes("tr", "id"),
        )

        val document = Jsoup.clean(
            Jsoup.parseBodyFragment(cleanedHtml)
                .selectFirst("tr#content-blocks")
                ?.html()
                ?: throw NewsletterParsingException("Unable to find content-blocks"),
            Safelist.basic()
        ).let {
            Jsoup.parseBodyFragment(it)
        }

        val title = email.subject.substringAfter(":").trim()
        val link = document.select("a[href]")
            .firstOrNull { it.text().isNotBlank() }
            ?.let { URL(it.attr("href")) }
            ?: throw NewsletterParsingException("Unable to find article link")
        val description = document.select("p")
            .takeWhile { it.select("span").isEmpty() }
            .joinToString(separator = "\n\n") { it.text() }

        return listOf(
            Article(
                title = title,
                link = link,
                description = description,
            )
        )
    }
}

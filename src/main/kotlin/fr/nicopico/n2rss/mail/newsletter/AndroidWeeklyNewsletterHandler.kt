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
import fr.nicopico.n2rss.utils.toURL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist

class AndroidWeeklyNewsletterHandler : NewsletterHandler {

    override val newsletter: Newsletter = Newsletter(
        code = "android_weekly",
        name = "Android Weekly (Articles only)",
        websiteUrl = "https://androidweekly.net",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("contact@androidweekly.net")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content,
            Safelist.basic()
                .addAttributes("span", "style"),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)
        val start: Element = document.select("span").first { it.ownText() == "Articles & Tutorials" }
        val sectionTitleStyle = start.attr("style")
        val end: Element = start.nextElementSiblings().first { it.attr("style") == sectionTitleStyle }

        val articleSectionDocument = Document("").apply {
            val nodesBetween = (start.parent()?.childNodes() ?: emptyList())
                .dropWhile { it != start }
                .takeWhile { it != end }
            appendChildren(nodesBetween)
        }

        @Suppress("ExplicitItLambdaParameter")
        return articleSectionDocument.select("a[href]")
            .filter { it -> it.text().isNotBlank() }
            .mapNotNull { tag ->
                // Ignore entries with invalid link
                tag.attr("href").toURL()
                    ?.let { link ->
                        Article(
                            title = tag.text().trim(),
                            link = link,
                            description = tag.nextSibling().toString().trim(),
                        )
                    }
            }
    }
}

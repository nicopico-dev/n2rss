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
import fr.nicopico.n2rss.newsletter.handlers.legacy.LegacyPointer540NewsletterHandler
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component

@Component
class PointerNewsletterHandler : NewsletterHandlerSingleFeed {

    override val newsletter: Newsletter = Newsletter(
        code = "pointer",
        name = "Pointer",
        websiteUrl = "http://www.pointer.io/",
    )

    private val legacyHandler = LegacyPointer540NewsletterHandler()

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("suraj@pointer.io")
    }

    override fun extractArticles(email: Email): List<Article> {
        if (legacyHandler.canHandle(email)) {
            return legacyHandler.extractArticles(email)
        }

        val cleanedHtml = Jsoup.clean(
            email.content.preserveArticleTitles("n2rss-article-title"),
            Safelist.basic()
                .addAttributes("div", "class"),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)
        println(document)

        // TODO Sponsors are articles without author. Add the "SPONSOR -" prefix
        return emptyList()
    }

    private fun String.preserveArticleTitles(cssClass: String): String {
        val doc = Jsoup.parse(this)
        doc.select("td[id].dd")
            .forEach { td ->
                // Insert the <p> element as the only child of the <td> element
                val tdElement = Element("td")
                    .appendChild(
                        Element("div")
                            .attr("class", cssClass)
                            .appendChildren(td.children())
                    )
                td.replaceWith(tdElement)
            }
        return doc.html()
    }
}

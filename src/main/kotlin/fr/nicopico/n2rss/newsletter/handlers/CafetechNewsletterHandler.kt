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
import fr.nicopico.n2rss.newsletter.handlers.exception.NewsletterParsingException
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.utils.url.toUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

@Component
class CafetechNewsletterHandler : NewsletterHandlerSingleFeed {

    override val newsletter: Newsletter = Newsletter(
        code = "cafetech",
        name = "Cafétech",
        websiteUrl = "https://cafetech.fr/",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("cafetech@substack.com")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.none()
                .addTags("h1", "p", "a")
                .addAttributes("h1", "class")
                .addAttributes("a", "href", "class"),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        // All articles of this newsletter share the same URL
        val newsletterLink: URL = document.selectFirst("a.email-button-outline[href]")?.attr("href")
            ?.toUrlOrNull()?.removeAppStoreRedirect()
            ?: throw NewsletterParsingException("Could not find the newsletter URL for \"${email.subject}\"")

        return document
            .select("h1.header-anchor-post")
            .map { articleTitleElement ->
                val title = articleTitleElement.text()

                // Take <p> elements after the title, until "Pour aller plus loin"
                val paragraphCount = AtomicInteger(0)
                val contentElements = articleTitleElement
                    .nextElementSibling()!! // Picture
                    .nextElementSiblings()
                    .takeWhile {
                        it.tagName() == "p"
                            && paragraphCount.incrementAndGet() <= ARTICLE_DESCRIPTION_PARAGRAPHS
                            && !it.text().startsWith(ARTICLE_LINKS_P_PREFIX)
                    }
                val articleDescription = contentElements
                    .joinToString(
                        separator = "\n\n",
                        transform = Element::text,
                        postfix = ARTICLE_DESCRIPTION_POSTFIX,
                    )

                Article(
                    title = title,
                    description = articleDescription,
                    link = newsletterLink,
                )
            }
    }

    /**
     * Remove the query-parameter causing a redirection to the App Store when opening the article
     */
    private fun URL.removeAppStoreRedirect(): URL {
        val queryParams = query
            ?.split("&")
            ?.filterNot { it.startsWith("$REDIRECT_QUERY_PARAMETER=") }
            ?.joinToString("&")

        return URL(
            protocol,
            host,
            port,
            if (queryParams.isNullOrEmpty()) path else "$path?$queryParams"
        )
    }

    companion object {
        /**
         * Define how many paragraphs should be used to create the article description
         */
        private const val ARTICLE_DESCRIPTION_PARAGRAPHS = 1
        private const val ARTICLE_DESCRIPTION_POSTFIX = "\n\n(Ouvrir l'article pour continuer)"

        /**
         * Text indicating the content of this <p> element contains links of the article
         */
        private const val ARTICLE_LINKS_P_PREFIX = "Pour aller plus loin"

        private const val REDIRECT_QUERY_PARAMETER = "redirect"
    }
}

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
import fr.nicopico.n2rss.newsletter.handlers.jsoup.extractSections
import fr.nicopico.n2rss.newsletter.handlers.jsoup.process
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.utils.url.toUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component

@Component
class CommonMainDevNewsletterHandler : NewsletterHandlerMultipleFeeds {
    override val newsletters = listOf(
        mainNewsletter,
        librariesNewsletter,
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("commonmain.dev", ignoreCase = true)
    }

    override fun extractArticles(email: Email): Map<Newsletter, List<Article>> {
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.none()
                .addTags("a", "div", "span", "p", "ul", "li", "h3", "strong", "br", "table", "tr", "td", "tbody", "b")
                .addAttributes("a", "href", "class")
                .addAttributes("div", "class")
                .addAttributes("span", "class")
                .addAttributes("table", "class")
                .addAttributes("td", "class")
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        val sections = document.extractSections("h3")

        val articlesByNewsletter = mutableMapOf<Newsletter, MutableList<Article>>()

        // Handle articles before the first h3 (if any)
        val firstSectionStart = sections.firstOrNull()?.start
        if (firstSectionStart != null) {
            val introElement = Document("").apply {
                val nodesBefore = firstSectionStart.parent()?.childNodes().orEmpty()
                    .takeWhile { it != firstSectionStart }
                appendChildren(nodesBefore)
            }
            val introArticles = extractArticlesFromElement(introElement)
            if (introArticles.isNotEmpty()) {
                articlesByNewsletter.getOrPut(mainNewsletter) { mutableListOf() }.addAll(introArticles)
            }
        }

        for (section in sections) {
            val targetNewsletter = if (section.title.contains("Dependency Graph", ignoreCase = true)) {
                librariesNewsletter
            } else {
                mainNewsletter
            }

            val articles = section.process { sectionDoc ->
                extractArticlesFromElement(sectionDoc)
            }

            if (articles.isNotEmpty()) {
                articlesByNewsletter.getOrPut(targetNewsletter) { mutableListOf() }.addAll(articles)
            }
        }

        return articlesByNewsletter
    }

    private fun extractArticlesFromElement(element: Element): List<Article> {
        val articles = mutableListOf<Article>()

        // Important: we want to preserve the relative order of articles in the section.
        val articleElements = element.select(".kg-callout-card, ul > li, .kg-bookmark-card, .kg-cta-card")

        for (articleElement in articleElements) {
            when {
                articleElement.hasClass("kg-callout-card") -> {
                    val linkElement = articleElement.selectFirst(".kg-callout-text a")
                    val title = linkElement?.text()
                    val link = linkElement?.attr("href")?.toUrlOrNull()
                    if (title != null && link != null) {
                        val description = articleElement.selectFirst(".kg-callout-text")?.text()
                            ?.removePrefix(title)
                            ?.trim()
                            ?: ""
                        articles.add(Article(title, link, description))
                    }
                }

                articleElement.tagName() == "li" -> {
                    val linkElement = articleElement.selectFirst("a")
                    val title = linkElement?.text()
                    val link = linkElement?.attr("href")?.toUrlOrNull()
                    if (title != null && link != null) {
                        val description = articleElement.text().removePrefix(title).trim()
                        articles.add(Article(title, link, description))
                    }
                }

                articleElement.hasClass("kg-bookmark-card") -> {
                    val linkElement = articleElement.selectFirst(".kg-bookmark-container")
                    val title = articleElement.selectFirst(".kg-bookmark-title")?.text()
                    val link = linkElement?.attr("href")?.toUrlOrNull()
                    if (title != null && link != null) {
                        val description = articleElement.selectFirst(".kg-bookmark-description")?.text() ?: ""
                        val caption = articleElement.selectFirst(".kg-card-figcaption")?.text() ?: ""
                        val fullDescription = listOf(description, caption).filter { it.isNotBlank() }.joinToString("\n")
                        articles.add(Article(title, link, fullDescription))
                    }
                }

                articleElement.hasClass("kg-cta-card") -> {
                    val linkInText = articleElement.selectFirst(".kg-cta-text a")
                    val title = linkInText?.text()
                        ?: articleElement.selectFirst(".kg-cta-text b, .kg-cta-text strong")?.text()

                    val link = linkInText?.attr("href")?.toUrlOrNull()
                        ?: articleElement.selectFirst("a")?.attr("href")?.toUrlOrNull()

                    if (title != null && link != null) {
                        val isSponsored = articleElement.select(".kg-cta-sponsor-label").isNotEmpty()
                        val displayTitle = if (isSponsored) "SPONSORED - $title" else title
                        val description = articleElement.selectFirst(".kg-cta-text")?.text()
                            ?.removePrefix(title)
                            ?.trim()
                            ?: ""
                        articles.add(Article(displayTitle, link, description))
                    }
                }
            }
        }
        return articles
    }

    companion object {
        val mainNewsletter = Newsletter(
            code = "commonmain_dev",
            name = "commonMain.dev",
            websiteUrl = "https://commonmain.dev/",
            notes = "Articles",
            feedTitle = "commonMain.dev (Articles)",
        )

        val librariesNewsletter = Newsletter(
            code = "commonmain_dev/libraries",
            name = "commonMain.dev",
            websiteUrl = "https://commonmain.dev/",
            notes = "Libraries",
            feedTitle = "commonMain.dev (Libraries)",
        )
    }
}

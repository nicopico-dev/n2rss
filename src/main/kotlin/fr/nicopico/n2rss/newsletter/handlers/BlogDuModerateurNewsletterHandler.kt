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
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist

class BlogDuModerateurNewsletterHandler : NewsletterHandlerMultipleFeeds {

    override val newsletters: List<Newsletter> = listOf(
        mainNewsletter,
        toolsNewsletter,
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email == "newsletter@blogdumoderateur.com"
    }

    override fun extractArticles(email: Email): Map<Newsletter, List<Article>> {
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.basic()
                .addAttributes("a", "class"),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)
        val documentContent = document.body().toString()
        val bestToolsIndex = documentContent.indexOf("Découvrez les meilleurs outils web")

        val articles: List<Article>
        val tools: List<Article>
        if (bestToolsIndex == -1) {
            articles = document.extractArticles()
            tools = emptyList()
        } else {
            val articlesDocument = documentContent.substring(0, bestToolsIndex)
                .let { Jsoup.parseBodyFragment(it) }
            val toolsDocument = documentContent.substring(bestToolsIndex)
                .let { Jsoup.parseBodyFragment(it) }
            articles = articlesDocument.extractArticles()
            tools = toolsDocument.extractArticles()
        }

        return mapOf(
            mainNewsletter to articles,
            toolsNewsletter to tools,
        )
    }

    private fun Document.extractArticles(): List<Article> {
        return select("a.title")
            .map { linkElement ->
                val title = linkElement.text()
                val link = linkElement.attr("href").toUrlOrNull()
                    ?: throw NewsletterParsingException("Could not retrieve the link for article '$title'")

                Article(
                    title = title,
                    link = link,
                    description = "TODO",
                )
            }
    }

    companion object {
        const val BDM_MAIN_NEWSLETTER_CODE = "bdm"
        const val BDM_TOOLS_NEWSLETTER_CODE = "bdm-tools"

        private val mainNewsletter = Newsletter(
            code = BDM_MAIN_NEWSLETTER_CODE,
            name = "Blog du Modérateur - Articles",
            websiteUrl = "https://www.blogdumoderateur.com",
        )

        private val toolsNewsletter = Newsletter(
            code = BDM_TOOLS_NEWSLETTER_CODE,
            name = "Blog du Modérateur - Outils",
            websiteUrl = "https://www.blogdumoderateur.com",
        )
    }
}

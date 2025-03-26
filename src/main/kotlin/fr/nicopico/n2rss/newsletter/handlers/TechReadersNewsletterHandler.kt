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
import fr.nicopico.n2rss.mail.models.text
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import org.springframework.stereotype.Component
import java.net.URL
import java.text.Normalizer

@Component
class TechReadersNewsletterHandler : NewsletterHandlerSingleFeed {
    override val newsletter: Newsletter = Newsletter(
        code = "tech-readers",
        name = "Tech Readers",
        websiteUrl = "https://share.hsforms.com/1fINml3OxSkaUjbqb9Gy7Ug3b2p9",
        notes = "a newsletter by Tech Rocks"
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email == "hello@tech.rocks"
            && email.subject.matches(EMAIL_SUBJECT_REGEX)
    }

    override fun extractArticles(email: Email): List<Article> {
        val textContent = Normalizer.normalize(email.content.text, Normalizer.Form.NFKC)
        // TODO Add newsletter introduction as the first article

        // Find the first and last sections in the text
        val firstSectionIndex: Int = SECTION_REGEX.find(textContent)?.range?.first ?: 0
        val endingIndex: Int = END_REGEX.find(textContent)?.range?.first ?: textContent.lastIndex
        val articlesText = textContent.substring(
            startIndex = firstSectionIndex,
            endIndex = endingIndex,
        )

        return ARTICLE_REGEX
            .findAll(articlesText)
            .flatMap { matchResult ->
                val title = matchResult.groupValues[ARTICLE_GROUP_TITLE]
                val url = URL(matchResult.groupValues[ARTICLE_GROUP_URL])

                val description = matchResult.groupValues[ARTICLE_GROUP_INFOS].trim()
                    .plus("\n")
                    .plus(matchResult.groupValues[ARTICLE_GROUP_DESCRIPTION])
                    .trim()

                buildList {
                    add(
                        Article(
                            title = title,
                            link = url,
                            description = description,
                        )
                    )

                    if (matchResult.groups[ARTICLE_GROUP_SECONDARY_TITLE] != null) {
                        val secondaryTitle = matchResult.groupValues[ARTICLE_GROUP_SECONDARY_TITLE]
                        val secondaryUrl = URL(matchResult.groupValues[ARTICLE_GROUP_SECONDARY_URL])

                        add(
                            Article(
                                title = secondaryTitle,
                                link = secondaryUrl,
                                description = description,
                            )
                        )
                    }
                }
            }
            .toList()
    }

    companion object {
        private val EMAIL_SUBJECT_REGEX = Regex("Tech Readers #\\d+.*")

        private val SECTION_REGEX = Regex("""(?:[^/\n\r]+\s/\s[^/\n\r]+)+""", RegexOption.CANON_EQ)
        private val END_REGEX = Regex("""La Newsletter faite par et pour les Tech Leaders !""")

        /**
         * Groups:
         * - title
         * - url
         * - infos (duration and author)
         * - description
         *
         * An article can have a secondary title and link (cf. #123)
         */
        private val ARTICLE_REGEX = Regex(
            pattern = """(.+?)\s+\((https://[^)]+)\)(?: et (.+?)\s+\((https://[^)]+)\))?\s+(.+)\s+(.+)""",
            options = setOf(RegexOption.MULTILINE, RegexOption.CANON_EQ),
        )
        private const val ARTICLE_GROUP_TITLE = 1
        private const val ARTICLE_GROUP_URL = 2
        private const val ARTICLE_GROUP_SECONDARY_TITLE = 3
        private const val ARTICLE_GROUP_SECONDARY_URL = 4
        private const val ARTICLE_GROUP_INFOS = 5
        private const val ARTICLE_GROUP_DESCRIPTION = 6
    }
}

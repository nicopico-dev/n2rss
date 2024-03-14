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
package fr.nicopico.n2rss.controller.rss

import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeedImpl
import fr.nicopico.n2rss.data.NewsletterRepository
import fr.nicopico.n2rss.data.PublicationRepository
import fr.nicopico.n2rss.service.NewsletterService
import fr.nicopico.n2rss.utils.toLegacyDate
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rss")
class RssFeedController(
    private val newsletterService: NewsletterService,
    private val newsletterRepository: NewsletterRepository,
    private val publicationRepository: PublicationRepository,
    private val rssOutputWriter: RssOutputWriter,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getRssFeeds(): List<NewsletterDTO> {
        return newsletterService.getNewslettersInfo()
            .map { it.toDTO() }
    }

    /**
     * Retrieves the RSS feed of publications.
     *
     * @param response The HttpServletResponse object used for writing the feed to the response output stream.
     * @param publicationStart The starting index of publications to retrieve. Default is 0.
     * @param publicationCount The maximum number of publications to retrieve. Default is 2.
     */
    @GetMapping(
        "{feed}",
        produces = [MediaType.APPLICATION_RSS_XML_VALUE],
    )
    fun getFeed(
        @PathVariable("feed") code: String,
        @RequestParam(value = "publicationStart", defaultValue = "0") publicationStart: Int,
        @RequestParam(value = "publicationCount", defaultValue = "2") publicationCount: Int,
        response: HttpServletResponse,
    ) {
        val newsletter = newsletterRepository.findNewsletterByCode(code)
        if (newsletter == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        val feed = SyndFeedImpl().apply {
            feedType = "rss_2.0"
            title = newsletter.name
            link = newsletter.websiteUrl
            description = "This is an RSS Feed for the newsletter \"${newsletter.name}\""
        }

        val sort = Sort.by(Sort.Direction.DESC, "date")
        val pageable = PageRequest.of(publicationStart, publicationCount, sort)
        val publicationPage = publicationRepository.findByNewsletter(newsletter, pageable)

        feed.entries = publicationPage.content
            .flatMap { publication ->
                publication.articles
                    .map { article ->
                        SyndEntryImpl().apply {
                            title = article.title
                            link = article.link.toString()
                            description = SyndContentImpl().apply {
                                type = "text/html"
                                value = article.description
                            }
                            publishedDate = publication.date.toLegacyDate()
                        }
                    }
            }

        response.contentType = MediaType.APPLICATION_RSS_XML_VALUE
        rssOutputWriter.write(feed, response)
    }
}

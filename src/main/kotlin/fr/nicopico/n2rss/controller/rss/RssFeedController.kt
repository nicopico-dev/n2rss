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

import fr.nicopico.n2rss.service.NewsletterService
import fr.nicopico.n2rss.service.RssService
import jakarta.servlet.http.HttpServletResponse
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
    private val rssService: RssService,
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
     * @param code Code associated with the feed
     * @param publicationStart The starting index of publications to retrieve. Default is 0.
     * @param publicationCount The maximum number of publications to retrieve. Default is 2.
     * @param response The HttpServletResponse object used for writing the feed to the response output stream.
     */
    @GetMapping(
        "{feed}",
        produces = [RSS_CONTENT_TYPE],
    )
    fun getFeed(
        @PathVariable("feed") code: String,
        @RequestParam(value = "publicationStart", defaultValue = "0") publicationStart: Int,
        @RequestParam(value = "publicationCount", defaultValue = "2") publicationCount: Int,
        response: HttpServletResponse,
    ) {
        try {
            val feed = rssService.getFeed(code, publicationStart, publicationCount)
            response.contentType = RSS_CONTENT_TYPE
            rssOutputWriter.write(feed, response)
        } catch (_: NoSuchElementException) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }

    companion object {
        private const val RSS_CONTENT_TYPE = MediaType.APPLICATION_XML_VALUE
    }
}

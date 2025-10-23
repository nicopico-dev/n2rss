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
package fr.nicopico.n2rss.controller.rss

import fr.nicopico.n2rss.controller.dto.NewsletterDTO
import fr.nicopico.n2rss.controller.dto.toDTO
import fr.nicopico.n2rss.newsletter.service.NewsletterService
import fr.nicopico.n2rss.newsletter.service.RssService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
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
    fun getRssFeeds(
        @Suppress("UnusedParameter") /* Used by AnalyticsAspect */
        @RequestHeader(value = "User-Agent") userAgent: String,
    ): List<NewsletterDTO> {
        return newsletterService.getNonHiddenEnabledNewslettersInfo()
            .map { it.toDTO() }
    }

    /**
     * Retrieves the RSS feed of publications.
     *
     * @param feed Code associated with the feed
     * @param publicationStart The starting index of publications to retrieve. Default is 0.
     * @param publicationCount The maximum number of publications to retrieve. Default is 2.
     * @param response The HttpServletResponse object used for writing the feed to the response output stream.
     */
    @GetMapping("{feed}", produces = [RSS_CONTENT_TYPE])
    fun getFeed(
        @PathVariable("feed") feed: String,
        @RequestParam(value = "publicationStart", defaultValue = "0") publicationStart: Int,
        @RequestParam(value = "publicationCount", defaultValue = "2") publicationCount: Int,
        @Suppress("UnusedParameter") /* Used by AnalyticsAspect */
        @RequestHeader(value = "User-Agent") userAgent: String,
        response: HttpServletResponse,
    ) {
        writeFeedToResponse(feed, publicationStart, publicationCount, response)
    }

    /**
     * Retrieves the RSS feed of publications, for feeds whose type is of format "folder/code"
     *
     * @param folder folder part of the code associated with the feed
     * @param feed feed part of the code associated with the feed
     * @param publicationStart The starting index of publications to retrieve. Default is 0.
     * @param publicationCount The maximum number of publications to retrieve. Default is 2.
     * @param response The HttpServletResponse object used for writing the feed to the response output stream.
     */
    @GetMapping("{folder}/{feed}", produces = [RSS_CONTENT_TYPE])
    @Suppress("LongParameterList")
    fun getFeed(
        @PathVariable("folder") folder: String,
        @PathVariable("feed") feed: String,
        @RequestParam(value = "publicationStart", defaultValue = "0") publicationStart: Int,
        @RequestParam(value = "publicationCount", defaultValue = "2") publicationCount: Int,
        @Suppress("UnusedParameter") /* Used by AnalyticsAspect */
        @RequestHeader(value = "User-Agent") userAgent: String,
        response: HttpServletResponse,
    ) {
        val code = "$folder/$feed"
        writeFeedToResponse(code, publicationStart, publicationCount, response)
    }

    private fun writeFeedToResponse(
        code: String,
        publicationStart: Int,
        publicationCount: Int,
        response: HttpServletResponse
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

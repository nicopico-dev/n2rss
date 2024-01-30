package fr.nicopico.n2rss.rss

import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeedImpl
import com.rometools.rome.io.SyndFeedOutput
import fr.nicopico.n2rss.data.NewsletterRepository
import fr.nicopico.n2rss.data.PublicationRepository
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
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
    private val newsletterHandlers: List<NewsletterHandler>,
    private val newsletterRepository: NewsletterRepository,
    private val publicationRepository: PublicationRepository,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getRssFeeds(): List<NewsletterDTO> {
        return newsletterHandlers
            .map { it.newsletter }
            .map {
                NewsletterDTO(
                    code = it.code,
                    title = it.name,
                    publicationCount = publicationRepository.countPublicationsByNewsletter(it),
                )
            }
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
        produces = [MediaType.APPLICATION_XML_VALUE]
    )
    fun getFeed(
        @PathVariable("feed") code: String,
        response: HttpServletResponse,
        @RequestParam(value = "publicationStart", defaultValue = "0") publicationStart: Int,
        @RequestParam(value = "publicationCount", defaultValue = "2") publicationCount: Int,
    ) {
        val newsletter = newsletterRepository.findNewsletterByCode(code)
        if (newsletter == null) {
            response.sendError(404)
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

        SyndFeedOutput().output(feed, response.outputStream.writer())
    }
}

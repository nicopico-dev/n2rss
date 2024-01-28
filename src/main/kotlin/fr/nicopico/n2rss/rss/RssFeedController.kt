package fr.nicopico.n2rss.rss

import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeedImpl
import com.rometools.rome.io.SyndFeedOutput
import fr.nicopico.n2rss.data.PublicationRepository
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.utils.toLegacyDate
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rss")
class RssFeedController(
    private val publicationRepository: PublicationRepository,
) {
    /**
     * Retrieves the RSS feed of publications.
     *
     * @param response The HttpServletResponse object used for writing the feed to the response output stream.
     * @param publicationStart The starting index of publications to retrieve. Default is 0.
     * @param publicationCount The maximum number of publications to retrieve. Default is 2.
     */
    @GetMapping(produces = [MediaType.APPLICATION_XML_VALUE])
    fun getFeed(
        response: HttpServletResponse,
        @RequestParam(value = "publicationStart", defaultValue = "0") publicationStart: Int,
        @RequestParam(value = "publicationCount", defaultValue = "2") publicationCount: Int,
    ) {
        val feed = SyndFeedImpl().apply {
            feedType = "rss_2.0"
            title = "Publications RSS Feed"
            link = "https://link-to-your-app.com"
            description = "This is the RSS feed for the publications."
        }

        // Retrieve publications
        val newsletter = Newsletter("Android Weekly")

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

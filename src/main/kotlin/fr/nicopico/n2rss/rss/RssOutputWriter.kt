package fr.nicopico.n2rss.rss

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedOutput
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class RssOutputWriter {
    fun write(feed: SyndFeed, response: HttpServletResponse) {
        SyndFeedOutput().output(feed, response.outputStream.writer())
    }
}

package fr.nicopico.n2rss.rss

import com.rometools.rome.feed.synd.SyndFeedImpl
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RssOutputWriterTest {

    @MockK(relaxed = true)
    private lateinit var response: HttpServletResponse

    private lateinit var rssOutputWriter: RssOutputWriter

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        rssOutputWriter = RssOutputWriter()
    }

    @Test
    fun `RssOutputWriter should be able to write RSS feed to an HTTP response`() {
        // GIVEN
        val feed = SyndFeedImpl().apply {
            feedType = "rss_2.0"
            title = "title"
            link = "link"
            description = "description"
        }

        // WHEN - THEN
        shouldNotThrowAny {
            rssOutputWriter.write(feed, response)
        }
    }
}

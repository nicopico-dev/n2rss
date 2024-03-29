package fr.nicopico.n2rss.controller.home

import fr.nicopico.n2rss.config.N2RssProperties
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.models.NewsletterInfo
import fr.nicopico.n2rss.service.NewsletterService
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ui.Model

class HomeControllerTest {

    @MockK
    private lateinit var newsletterService: NewsletterService
    @MockK
    private lateinit var feedProperties: N2RssProperties.FeedsProperties

    private lateinit var homeController: HomeController

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        val properties = mockk<N2RssProperties>() {
            every { feeds } returns feedProperties
        }

        homeController = HomeController(newsletterService, properties)
    }

    @Test
    fun `home should provide necessary information to the template`() {
        // GIVEN
        val newslettersInfo = listOf(
            NewsletterInfo("A", "Newsletter A", "Website A", 12, null),
            NewsletterInfo("B", "Newsletter B", "Website B", 3, null),
            NewsletterInfo("C", "Newsletter C", "Website C", 0, null),
            NewsletterInfo("D", "Newsletter D", "Website D", 1, null),
        )
        every { newsletterService.getNewslettersInfo() } returns newslettersInfo
        every { feedProperties.forceHttps } returns false

        val requestUrl = StringBuffer("http://localhost:8134")
        val request = mockk<HttpServletRequest> {
            every { requestURL } returns requestUrl
        }

        val model = mockk<Model>(relaxed = true)

        // WHEN
        val result = homeController.home(request, model)

        // THEN
        result shouldBe "index"
        val newslettersSlot = slot<List<Newsletter>>()
        verify {
            model.addAttribute("newsletters", capture(newslettersSlot))
            model.addAttribute("requestUrl", "http://localhost:8134")
        }

        // Newsletters without publication should not be displayed
        newslettersSlot.captured shouldContainOnly listOf(
            NewsletterInfo("A", "Newsletter A", "Website A", 12, null),
            NewsletterInfo("B", "Newsletter B", "Website B", 3, null),
            NewsletterInfo("D", "Newsletter D", "Website D", 1, null),
        )
    }

    @Test
    fun `home should use HTTPS feed when the feature is activated`() {
        // GIVEN
        val newslettersInfo = listOf<NewsletterInfo>()
        every { newsletterService.getNewslettersInfo() } returns newslettersInfo
        every { feedProperties.forceHttps } returns true

        val requestUrl = StringBuffer("http://localhost:8134")
        val request = mockk<HttpServletRequest> {
            every { requestURL } returns requestUrl
        }

        val model = mockk<Model>(relaxed = true)

        // WHEN
        val result = homeController.home(request, model)

        // THEN
        result shouldBe "index"
        verify {
            model.addAttribute("newsletters", any())
            model.addAttribute("requestUrl", "https://localhost:8134")
        }
    }
}

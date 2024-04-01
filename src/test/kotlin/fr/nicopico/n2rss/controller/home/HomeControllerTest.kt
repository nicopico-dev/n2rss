package fr.nicopico.n2rss.controller.home

import fr.nicopico.n2rss.config.N2RssProperties
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.models.NewsletterInfo
import fr.nicopico.n2rss.service.NewsletterService
import fr.nicopico.n2rss.service.ReCaptchaService
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ui.Model
import java.net.MalformedURLException
import java.net.URL

class HomeControllerTest {

    @MockK
    private lateinit var newsletterService: NewsletterService
    @MockK
    private lateinit var reCaptchaService: ReCaptchaService
    @MockK
    private lateinit var feedProperties: N2RssProperties.FeedsProperties
    @MockK(relaxed = true)
    private lateinit var reCaptchaProperties: N2RssProperties.ReCaptchaProperties

    private lateinit var homeController: HomeController

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        val properties = mockk<N2RssProperties>() {
            every { feeds } returns feedProperties
            every { recaptcha } returns reCaptchaProperties
        }

        homeController = HomeController(newsletterService, reCaptchaService, properties)
    }

    //region GET /
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
    //endregion

    //region POST /send-request
    @Test
    fun `sendRequest should save the request if captcha is valid`() {
        // GIVEN
        val newsletterUrl = "http://localhost:8134"
        val captchaResponse = "captchaResponse"
        val captchaSecretKey = "captchaSecretKey"

        // SETUP
        val response = mockk<HttpServletResponse>(relaxed = true)
        every { newsletterService.saveRequest(any()) } just Runs
        every { reCaptchaProperties.secretKey } returns captchaSecretKey
        every { reCaptchaService.isCaptchaValid(any(), any()) } returns true

        // WHEN
        homeController.requestNewsletter(newsletterUrl, captchaResponse, response)

        // THEN
        verify { newsletterService.saveRequest(URL(newsletterUrl)) }
        verify { reCaptchaService.isCaptchaValid(captchaSecretKey, captchaResponse) }
        verify { response.sendRedirect("/") }
    }

    @Test
    fun `sendRequest should not save the request if captcha is not valid`() {
        // GIVEN
        val newsletterUrl = "http://localhost:8134"
        val captchaResponse = "captchaResponse"
        val captchaSecretKey = "captchaSecretKey"

        // SETUP
        val response = mockk<HttpServletResponse>(relaxed = true)
        every { reCaptchaProperties.secretKey } returns captchaSecretKey
        every { reCaptchaService.isCaptchaValid(any(), any()) } returns false

        // WHEN
        homeController.requestNewsletter(newsletterUrl, captchaResponse, response)

        // THEN
        verify(exactly = 0) { newsletterService.saveRequest(any()) }
        verify { response.sendError(400, any()) }
    }

    @Test
    fun `sendRequest should fail if newsletterUrl is not a valid url`() {
        // GIVEN
        val newsletterUrl = "something"
        val captchaResponse = "captchaResponse"
        val captchaSecretKey = "captchaSecretKey"

        // SETUP
        val response = mockk<HttpServletResponse>(relaxed = true)
        every { newsletterService.saveRequest(any()) } just Runs
        every { reCaptchaProperties.secretKey } returns captchaSecretKey
        every { reCaptchaService.isCaptchaValid(any(), any()) } returns true

        // WHEN
        val error = shouldThrowAny {
            homeController.requestNewsletter(newsletterUrl, captchaResponse, response)
        }

        // THEN
        verify(exactly = 0) { newsletterService.saveRequest(any()) }
        error should beOfType<MalformedURLException>()
    }
    //endregion
}

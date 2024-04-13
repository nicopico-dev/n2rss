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
package fr.nicopico.n2rss.controller.home

import fr.nicopico.n2rss.config.N2RssProperties
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.models.NewsletterInfo
import fr.nicopico.n2rss.service.NewsletterService
import fr.nicopico.n2rss.service.ReCaptchaService
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
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

    @Nested
    inner class GetTest {
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

    @Nested
    inner class SendRequestTest {
        @Test
        fun `sendRequest should save the request if captcha is valid`() {
            // GIVEN
            val newsletterUrl = "http://localhost:8134"
            val captchaResponse = "captchaResponse"
            val captchaSecretKey = "captchaSecretKey"

            // SETUP
            every { reCaptchaProperties.enabled } returns true
            every { reCaptchaProperties.secretKey } returns captchaSecretKey
            every { newsletterService.saveRequest(any()) } just Runs
            every { reCaptchaService.isCaptchaValid(any(), any()) } returns true

            // WHEN
            val response = homeController.requestNewsletter(newsletterUrl, captchaResponse)

            // THEN
            verify { newsletterService.saveRequest(URL(newsletterUrl)) }
            verify { reCaptchaService.isCaptchaValid(captchaSecretKey, captchaResponse) }
            response.statusCode shouldBe HttpStatus.OK
        }

        @Test
        fun `sendRequest should not save the request if captcha is not valid`() {
            // GIVEN
            val newsletterUrl = "http://localhost:8134"
            val captchaResponse = "captchaResponse"
            val captchaSecretKey = "captchaSecretKey"

            // SETUP
            every { reCaptchaProperties.enabled } returns true
            every { reCaptchaProperties.secretKey } returns captchaSecretKey
            every { reCaptchaService.isCaptchaValid(any(), any()) } returns false

            // WHEN
            val response = homeController.requestNewsletter(newsletterUrl, captchaResponse)

            // THEN
            verify(exactly = 0) { newsletterService.saveRequest(any()) }
            response.statusCode shouldBe HttpStatus.BAD_REQUEST
        }

        @Test
        fun `sendRequest should validate without captcha if disabled`() {
            // GIVEN
            val newsletterUrl = "http://localhost:8134"

            // SETUP
            every { reCaptchaProperties.enabled } returns false
            every { newsletterService.saveRequest(any()) } just Runs

            // WHEN
            val response = homeController.requestNewsletter(newsletterUrl)

            // THEN
            verify(exactly = 1) { newsletterService.saveRequest(any()) }
            verify(exactly = 0) { reCaptchaService.isCaptchaValid(any(), any()) }
            response.statusCode shouldBe HttpStatus.OK
        }

        @Test
        fun `sendRequest should use assume https protocol if none are passed`() {
            // GIVEN
            val newsletterUrl = "www.google.com"
            val captchaResponse = "captchaResponse"

            // SETUP
            every { reCaptchaProperties.enabled } returns false
            every { newsletterService.saveRequest(any()) } just Runs

            // WHEN
            homeController.requestNewsletter(newsletterUrl, captchaResponse)

            // THEN
            val slotUrl = slot<URL>()
            verify { newsletterService.saveRequest(capture(slotUrl)) }
            slotUrl.captured.toString() shouldBe "https://www.google.com"
        }
    }
}

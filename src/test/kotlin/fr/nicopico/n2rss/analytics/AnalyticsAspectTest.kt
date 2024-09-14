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
package fr.nicopico.n2rss.analytics

import fr.nicopico.n2rss.analytics.models.AnalyticsEvent
import fr.nicopico.n2rss.analytics.models.AnalyticsException
import fr.nicopico.n2rss.analytics.service.AnalyticsService
import fr.nicopico.n2rss.controller.home.HomeController
import fr.nicopico.n2rss.controller.rss.RssFeedController
import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.mail.models.Sender
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandlerMultipleFeeds
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandlerSingleFeed
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.springframework.aop.framework.DefaultAopProxyFactory
import org.springframework.ui.Model

/**
 * DISCLAIMER : This test case is completely bonkers !
 * Events are sent that should not, Mocks configured to throw do not throw...
 * Probably some conflicts between Mocks and Aspect shenanigans
 *
 * Check if I can make it works better with https://github.com/mock4aj/mock4aj ?
 *
 * It seems linked to the userAgent parameter ??
 * Since I added the parameter, the mocks do not throw exceptions when configured
 */
@ExtendWith(MockKExtension::class)
class AnalyticsAspectTest {

    @MockK(relaxed = true)
    lateinit var analyticsService: AnalyticsService

    private inline fun <reified T : Any> createWithAspect(): T {
        val proxyFactory = AspectJProxyFactory(mockk<T>(relaxed = true))
        proxyFactory.addAspect(AnalyticsAspect(analyticsService))

        val aopProxyFactory = DefaultAopProxyFactory()
            .createAopProxy(proxyFactory)

        return aopProxyFactory.proxy as T
    }

    @Nested
    inner class HomeControllerTest {

        lateinit var homeController: HomeController

        @BeforeEach
        fun setUp() {
            homeController = createWithAspect()
        }

        @Nested
        inner class Home {
            @Test
            fun `success should trigger an analytic event`() {
                // GIVEN
                val request = mockk<HttpServletRequest>() {
                    every { requestURL } returns StringBuffer("https://www.google.com")
                }
                val model = mockk<Model>(relaxed = true)
                val userAgent = "ua"

                // WHEN
                homeController.home(request = request, model = model, userAgent = userAgent)

                // THEN
                verify { analyticsService.track(AnalyticsEvent.Home(userAgent = userAgent)) }
            }

            @Test
            @Disabled("homeController.home() do not throw ?? 🙃")
            fun `error should trigger an analytic event`() {
                // GIVEN
                val request = mockk<HttpServletRequest>() {
                    every { requestURL } returns StringBuffer("https://www.google.com")
                }
                val model = mockk<Model>(relaxed = true)
                val userAgent = "ua"

                // SETUP
                every { homeController.home(any(), any(), any()) } throws RuntimeException("TEST")

                // WHEN
                shouldThrowAny {
                    homeController.home(request = request, model = model, userAgent = userAgent)
                }

                // THEN
                verify { analyticsService.track(AnalyticsEvent.Error.HomeError) }
                // NOTE: for some reason, a Home event is sent before the HomeError event during the tests
                // but only the error event is sent during a live execution
            }

            @Test
            fun `analytics error should not propagate`() {
                // GIVEN
                every { analyticsService.track(any()) } throws AnalyticsException()

                // WHEN - THEN
                shouldNotThrowAny {
                    homeController.home(mockk(), mockk(), "ua")
                }
            }
        }

        @Nested
        inner class RequestNewsletter {
            @Test
            fun `requestNewsletter success should trigger an analytic event`() {
                // GIVEN
                val newsletterUrl = "https://www.androidweekly.com"
                val captchaResponse = ""
                val userAgent = "ua"

                // WHEN
                homeController.requestNewsletter(
                    newsletterUrl = newsletterUrl,
                    captchaResponse = captchaResponse,
                    userAgent = userAgent
                )

                // THEN
                verify { analyticsService.track(AnalyticsEvent.RequestNewsletter(newsletterUrl, userAgent)) }
            }

            @Test
            @Disabled("homeController.requestNewsletter() do not throw ?? 🙃")
            fun `requestNewsletter error should trigger an analytic event`() {
                // GIVEN
                val newsletterUrl = "https://www.androidweekly.com"
                val captchaResponse = ""
                val userAgent = "ua"

                // SETUP
                every { homeController.requestNewsletter(any(), any(), any()) } throws RuntimeException("TEST")

                // WHEN
                shouldThrowAny {
                    homeController.requestNewsletter(newsletterUrl, captchaResponse, userAgent)
                }

                // THEN
                verify { analyticsService.track(AnalyticsEvent.Error.RequestNewsletterError) }
            }

            @Test
            fun `analytics error should not propagate`() {
                // GIVEN
                every { analyticsService.track(any()) } throws AnalyticsException()

                // WHEN - THEN
                shouldNotThrowAny {
                    homeController.requestNewsletter("some-url", "", "ua")
                }
            }
        }
    }

    @Nested
    inner class RssFeedControllerTest {

        lateinit var rssFeedController: RssFeedController

        @BeforeEach
        fun setUp() {
            rssFeedController = createWithAspect()
        }

        @Nested
        inner class GetRssFeeds {
            @Test
            fun `getRssFeeds success should trigger an analytic event`() {
                // GIVEN
                val userAgent = "ua"

                // WHEN
                rssFeedController.getRssFeeds(userAgent = userAgent)

                // THEN
                verify { analyticsService.track(AnalyticsEvent.GetRssFeeds(userAgent)) }
            }

            @Test
            @Disabled("rssFeedController.getRssFeeds() do not throw ?? 🙃")
            fun `getRssFeeds error should trigger an analytic event`() {
                // SETUP
                every { rssFeedController.getRssFeeds(any()) } throws RuntimeException("TEST")

                // WHEN
                shouldThrowAny {
                    rssFeedController.getRssFeeds("ua")
                }

                // THEN
                verify { analyticsService.track(AnalyticsEvent.Error.GetRssFeedsError) }
            }

            @Test
            fun `analytics error should not propagate`() {
                // GIVEN
                every { analyticsService.track(any()) } throws AnalyticsException()

                // WHEN - THEN
                shouldNotThrowAny {
                    rssFeedController.getRssFeeds("ua")
                }
            }
        }

        @Nested
        inner class GetFeedByCode {
            @Test
            fun `getFeed (code) success should trigger an analytic event`() {
                // GIVEN
                val feedCode = "stability"
                val userAgent = "cord"

                // WHEN
                rssFeedController.getFeed(
                    feed = feedCode,
                    publicationStart = 0,
                    publicationCount = 1,
                    userAgent = userAgent,
                    response = mockk()
                )

                // THEN
                verify { analyticsService.track(AnalyticsEvent.GetFeed(feedCode, userAgent)) }
            }

            @Test
            @Disabled("rssFeedController.getFeed() do not throw ?? 🙃")
            fun `getFeed (code) error should trigger an analytic event`() {
                // GIVEN
                val feedCode = "citizens"
                val userAgent = "display"

                // SETUP
                every {
                    rssFeedController.getFeed(
                        feed = any(),
                        publicationStart = any(),
                        publicationCount = any(),
                        userAgent = any(),
                        response = any()
                    )
                } throws RuntimeException("TEST")

                // WHEN
                shouldThrowAny {
                    rssFeedController.getFeed(
                        feed = feedCode,
                        publicationStart = 0,
                        publicationCount = 1,
                        userAgent = userAgent,
                        response = mockk()
                    )
                }

                // THEN
                verify { analyticsService.track(AnalyticsEvent.Error.GetFeedError(feedCode)) }
            }

            @Test
            fun `analytics error should not propagate`() {
                // GIVEN
                every { analyticsService.track(any()) } throws AnalyticsException()

                // WHEN - THEN
                shouldNotThrowAny {
                    rssFeedController.getFeed(
                        feed = "",
                        publicationStart = 0,
                        publicationCount = 0,
                        userAgent = "",
                        response = mockk()
                    )
                }
            }
        }

        @Nested
        inner class GetFeedByFolderAndCode {
            @Test
            fun `getFeed (folder) success should trigger an analytic event`() {
                // GIVEN
                val folder = "carlo"
                val feed = "kevin"
                val userAgent = "passed"

                // WHEN
                rssFeedController.getFeed(
                    folder = folder,
                    feed = feed,
                    publicationStart = 0,
                    publicationCount = 1,
                    userAgent = userAgent,
                    response = mockk()
                )

                // THEN
                val feedCode = "$folder/$feed"
                verify { analyticsService.track(AnalyticsEvent.GetFeed(feedCode, userAgent)) }
            }

            @Test
            @Disabled("rssFeedController.getFeed() do not throw ?? 🙃")
            fun `getFeed (folder) error should trigger an analytic event`() {
                // GIVEN
                val folder = "independent"
                val feed = "contemporary"
                val userAgent = "clouds"

                // SETUP
                every {
                    rssFeedController.getFeed(
                        folder = any(),
                        feed = any(),
                        publicationStart = any(),
                        publicationCount = any(),
                        userAgent = any(),
                        response = any()
                    )
                } throws RuntimeException("TEST")

                // WHEN
                shouldThrowAny {
                    rssFeedController.getFeed(
                        folder = folder,
                        feed = feed,
                        publicationStart = 0,
                        publicationCount = 1,
                        userAgent = userAgent,
                        response = mockk()
                    )
                }

                // THEN
                val feedCode = "$folder/$feed"
                verify { analyticsService.track(AnalyticsEvent.Error.GetFeedError(feedCode)) }
            }

            @Test
            fun `analytics error should not propagate`() {
                // GIVEN
                every { analyticsService.track(any()) } throws AnalyticsException()

                // WHEN - THEN
                shouldNotThrowAny {
                    rssFeedController.getFeed(
                        folder = "",
                        feed = "",
                        publicationStart = 0,
                        publicationCount = 0,
                        userAgent = "",
                        response = mockk<HttpServletResponse>()
                    )
                }
            }
        }
    }

    @Nested
    inner class NewsletterHandler {

        private lateinit var newsletterHandlerSingleFeed: NewsletterHandlerSingleFeed
        private lateinit var newsletterHandlerMultipleFeeds: NewsletterHandlerMultipleFeeds

        @BeforeEach
        fun setUp() {
            newsletterHandlerSingleFeed = createWithAspect()
            newsletterHandlerMultipleFeeds = createWithAspect()
        }

        @Test
        fun `SingeFeed - email parsing error should trigger an analytic event`() {
            // GIVEN
            val emailTitle = "Debate answered send collections tue."
            val email = Email(
                sender = Sender("foo@n2rss.fr"),
                date = LocalDate(2024, 7, 20),
                subject = emailTitle,
                content = "Mark pennsylvania link granted viewing snap ellen, spell burst sales.",
                msgnum = 0
            )

            // SETUP
            every { newsletterHandlerSingleFeed.process(any()) } throws RuntimeException("TEST")

            // WHEN
            shouldThrowAny {
                newsletterHandlerSingleFeed.process(email)
            }

            // THEN
            val eventSlot = slot<AnalyticsEvent.Error.EmailParsingError>()
            verify { analyticsService.track(capture(eventSlot)) }
            assertSoftly(eventSlot.captured) { event ->
                event.emailTitle shouldBe emailTitle
                event.handlerName shouldStartWith "NewsletterHandlerSingleFeed"
            }
        }

        @Test
        fun `MultipleFeeds - email parsing error should trigger an analytic event`() {
            // GIVEN
            val emailTitle = "Accordingly absorption nsw commitment recognized camping archives"
            val email = Email(
                sender = Sender("foo@n2rss.fr"),
                date = LocalDate(2024, 7, 20),
                subject = emailTitle,
                content = "Replacement islam traditional bruce connectivity boost theft, maritime dust south.",
                msgnum = 0
            )

            // SETUP
            every { newsletterHandlerMultipleFeeds.process(any()) } throws RuntimeException("TEST")

            // WHEN
            shouldThrowAny {
                newsletterHandlerMultipleFeeds.process(email)
            }

            // THEN
            val eventSlot = slot<AnalyticsEvent.Error.EmailParsingError>()
            verify { analyticsService.track(capture(eventSlot)) }
            assertSoftly(eventSlot.captured) { event ->
                event.emailTitle shouldBe emailTitle
                event.handlerName shouldStartWith "NewsletterHandlerMultipleFeeds"
            }
        }
    }
}

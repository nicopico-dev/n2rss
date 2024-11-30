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

package fr.nicopico.n2rss.newsletter.service

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.newsletter.data.NewsletterRepository
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandler
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.NewsletterInfo
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NewsletterServiceTest {

    private val newsletters = listOf(
        Newsletter("code1", "Name 1", "website1"),
        Newsletter("code2", "Name 2", "website2"),
        Newsletter("code3", "Name 3", "website3", "some notes"),
    )

    @MockK
    private lateinit var newsletterRepository: NewsletterRepository
    @MockK
    private lateinit var publicationService: PublicationService

    private lateinit var newsletterService: NewsletterService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        newsletterService = NewsletterService(
            newsletterRepository = newsletterRepository,
            publicationService = publicationService
        )
    }

    @Test
    fun `should give info about all supported newsletters`() {
        // GIVEN
        val firstPublicationCode1 = LocalDate(2022, 3, 21)
        val firstPublicationCode2 = LocalDate(2023, 7, 8)
        every { publicationService.getPublicationsCount(any()) } answers {
            when (firstArg<Newsletter>().code) {
                "code1" -> 32
                "code2" -> 3
                else -> 0
            }
        }
        every { publicationService.getOldestPublicationDate(any()) } answers {
            val newsletter = firstArg<Newsletter>()
            when (newsletter.code) {
                "code1" -> firstPublicationCode1
                "code2" -> firstPublicationCode2
                else -> null
            }
        }
        every { newsletterRepository.getEnabledNewsletters() } returns newsletters

        // WHEN
        val result = newsletterService.getEnabledNewslettersInfo()

        // WHEN
        result shouldContainOnly listOf(
            NewsletterInfo(
                code = "code1",
                title = "Name 1",
                websiteUrl = "website1",
                publicationCount = 32,
                startingDate = firstPublicationCode1,
                notes = null,
            ),
            NewsletterInfo(
                code = "code2",
                title = "Name 2",
                websiteUrl = "website2",
                publicationCount = 3,
                startingDate = firstPublicationCode2,
                notes = null,
            ),
            NewsletterInfo(
                code = "code3",
                title = "Name 3",
                websiteUrl = "website3",
                publicationCount = 0,
                startingDate = null,
                notes = "some notes",
            ),
        )
    }

    @Test
    fun `should retrieve the newsletter from its code`() {
        // GIVEN
        val code = "harvey"
        val expected: Newsletter = mockk()
        every { newsletterRepository.findNewsletterByCode(any()) } returns expected

        // WHEN
        val newsletter = newsletterService.findNewsletterByCode(code)

        // THEN
        newsletter shouldBe expected
    }

    @Test
    fun `should retrieve the correct handler for an email`() {
        // GIVEN
        val email: Email = mockk { every { subject } returns "Title" }
        val handlerA: NewsletterHandler = mockk {
            every { canHandle(email) } returns false
        }
        val handlerB: NewsletterHandler = mockk {
            every { canHandle(email) } returns true
        }
        val handlerC: NewsletterHandler = mockk {
            every { canHandle(email) } returns false
        }
        every { newsletterRepository.getEnabledNewsletterHandlers() } returns listOf(handlerA, handlerB, handlerC)

        // WHEN
        val actual = newsletterService.findNewsletterHandlerForEmail(email)

        // THEN
        actual shouldBe handlerB
    }

    @Test
    fun `should return null if there is no handler for an email`() {
        // GIVEN
        val email: Email = mockk { every { subject } returns "Title" }
        val handlerA: NewsletterHandler = mockk {
            every { canHandle(email) } returns false
        }
        val handlerB: NewsletterHandler = mockk {
            every { canHandle(email) } returns false
        }
        val handlerC: NewsletterHandler = mockk {
            every { canHandle(email) } returns false
        }
        every { newsletterRepository.getEnabledNewsletterHandlers() } returns listOf(handlerA, handlerB, handlerC)

        // WHEN
        val actual = newsletterService.findNewsletterHandlerForEmail(email)

        // THEN
        actual shouldBe null
    }

    @Test
    fun `should return null if there is more than one handler for an email`() {
        // GIVEN
        val email: Email = mockk { every { subject } returns "Title" }
        val handlerA: NewsletterHandler = mockk {
            every { canHandle(email) } returns true
        }
        val handlerB: NewsletterHandler = mockk {
            every { canHandle(email) } returns false
        }
        val handlerC: NewsletterHandler = mockk {
            every { canHandle(email) } returns true
        }
        every { newsletterRepository.getEnabledNewsletterHandlers() } returns listOf(handlerA, handlerB, handlerC)

        // WHEN
        val actual = newsletterService.findNewsletterHandlerForEmail(email)

        // THEN
        actual shouldBe null
    }
}

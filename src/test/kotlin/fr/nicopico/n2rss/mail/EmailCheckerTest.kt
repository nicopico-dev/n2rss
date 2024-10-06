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

package fr.nicopico.n2rss.mail

import fr.nicopico.n2rss.mail.client.EmailClient
import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.monitoring.MonitoringService
import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandler
import fr.nicopico.n2rss.newsletter.handlers.exception.NoPublicationFoundException
import fr.nicopico.n2rss.newsletter.handlers.process
import fr.nicopico.n2rss.newsletter.models.Publication
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.scheduling.TaskScheduler
import java.time.Instant

@ExtendWith(MockKExtension::class)
class EmailCheckerTest {

    @MockK(relaxUnitFun = true)
    private lateinit var emailClient: EmailClient
    @MockK(relaxed = true)
    private lateinit var newsletterHandlerA: NewsletterHandler
    @MockK(relaxed = true)
    private lateinit var newsletterHandlerB: NewsletterHandler
    @MockK(relaxed = true)
    private lateinit var publicationRepository: PublicationRepository
    @MockK(relaxed = true)
    private lateinit var taskScheduler: TaskScheduler
    @MockK
    private lateinit var monitoringService: MonitoringService

    private lateinit var emailChecker: EmailChecker

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        emailChecker = EmailChecker(
            emailClient,
            listOf(
                newsletterHandlerA,
                newsletterHandlerB,
            ),
            publicationRepository,
            taskScheduler,
            monitoringService,
        )
    }

    @Test
    fun `emailChecker should schedule a check on launch`() {
        // WHEN
        emailChecker.checkEmailsOnStart()

        // THEN (matcher does not work on method reference)
        verify { taskScheduler.schedule(any(), any<Instant>()) }
    }

    @Test
    fun `emailChecker handles email when appropriate handler is present`(
        @MockK(relaxed = true) email: Email,
        @MockK publication: Publication,
    ) {
        // Given an email that should be handled by newsletterHandlerA
        every { emailClient.checkEmails() } returns listOf(email)
        every { newsletterHandlerA.canHandle(email) } returns true
        every { newsletterHandlerA.process(email) } returns listOf(publication)
        every { publication.articles } returns listOf(mockk())

        // When we check the email
        emailChecker.savePublicationsFromEmails()

        // Then the email should be handled by newsletterHandlerA and not by newsletterHandlerB
        verify { newsletterHandlerA.process(email) }
        verify { publicationRepository.saveAll(eq(listOf(publication))) }
        verify(exactly = 0) { newsletterHandlerB.process(email) }
        verify { emailClient.markAsRead(email) }
    }

    @Test
    fun `emailChecker ignores email when no appropriate handler is present`(
        @MockK(relaxed = true) email: Email
    ) {
        // Given an email that should not be handled by any handler
        every { emailClient.checkEmails() } returns listOf(email)
        every { newsletterHandlerA.canHandle(email) } returns false
        every { newsletterHandlerB.canHandle(email) } returns false

        // When we check the email
        emailChecker.savePublicationsFromEmails()

        // Then no handler should try to handle the email and no publication should be saved
        verify(exactly = 0) { newsletterHandlerA.process(email) }
        verify(exactly = 0) { publicationRepository.saveAll(any<List<Publication>>()) }
        verify(exactly = 0) { newsletterHandlerB.process(email) }
        verify(exactly = 0) { emailClient.markAsRead(any()) }
    }

    @Test
    fun `emailChecker will not process an email if more than one handler can handle it`(
        @MockK(relaxed = true) email: Email
    ) {
        // Given an email that should be handled by both handlers
        every { emailClient.checkEmails() } returns listOf(email)
        every { newsletterHandlerA.canHandle(email) } returns true
        every { newsletterHandlerB.canHandle(email) } returns true

        // When we check the email
        emailChecker.savePublicationsFromEmails()

        // Then it should not process any email
        // And no publication should be saved
        verify(exactly = 0) { newsletterHandlerA.process(email) }
        verify(exactly = 0) { newsletterHandlerB.process(email) }
        verify(exactly = 0) { publicationRepository.saveAll(any<List<Publication>>()) }
        verify(exactly = 0) { emailClient.markAsRead(any()) }
    }

    @Test
    fun `emailChecker continues processing emails after a processing error`(
        @MockK(relaxed = true) errorEmail: Email,
        @MockK(relaxed = true) validEmail: Email,
        @MockK publication: Publication,
    ) {
        // Given an email that causes an error and a valid email
        every { emailClient.checkEmails() } returns listOf(errorEmail, validEmail)
        every { emailClient.markAsRead(any()) } just Runs

        every { newsletterHandlerA.canHandle(any()) } returns true
        every { newsletterHandlerB.canHandle(any()) } returns false
        val errorEmailProcessing = Exception("Processing error")
        every { newsletterHandlerA.process(errorEmail) } throws errorEmailProcessing
        every { newsletterHandlerA.process(validEmail) } returns listOf(publication)
        every { publication.articles } returns listOf(mockk())
        every { monitoringService.notifyEmailProcessingError(any(), any()) } just Runs

        // When we check the emails
        emailChecker.savePublicationsFromEmails()

        // Then emailChecker should process validEmail after failing with errorEmail
        verify { newsletterHandlerA.process(errorEmail) }
        verify { newsletterHandlerA.process(validEmail) }
        verify { publicationRepository.saveAll(eq(listOf(publication))) }

        verify { emailClient.markAsRead(validEmail) }
        verify(exactly = 0) { emailClient.markAsRead(errorEmail) }

        verify { monitoringService.notifyEmailProcessingError(errorEmail, errorEmailProcessing) }
    }

    @Test
    fun `emailChecker will not crash if the client fails`() {
        // Given that emailClient fails when checking emails
        val emailError = RuntimeException("TEST")
        every { emailClient.checkEmails() } throws emailError
        every { monitoringService.notifyEmailClientError(any()) } just Runs

        // When we check the emails
        emailChecker.savePublicationsFromEmails()

        // Then emailChecker should proceed without doing anything
        verify { emailClient.checkEmails() }
        verify { monitoringService.notifyEmailClientError(emailError) }

        confirmVerified(
            emailClient,
            newsletterHandlerA,
            newsletterHandlerB,
            publicationRepository,
            monitoringService,
        )
    }

    @Test
    fun `emailChecker should notify an error on empty publications`(
        @MockK(relaxed = true) email: Email,
        @MockK publication: Publication,
    ) {
        // Given an email without any articles
        every { emailClient.checkEmails() } returns listOf(email)
        every { newsletterHandlerA.canHandle(email) } returns true
        every { newsletterHandlerA.process(email) } returns listOf(publication)
        every { publication.articles } returns emptyList()
        every { monitoringService.notifyEmailProcessingError(any(), any()) } just Runs

        // When we check the email
        emailChecker.savePublicationsFromEmails()

        // Then the email should be marked as read without creating a publication
        verify(exactly = 0) {
            publicationRepository.saveAll(eq(listOf(publication)))
            emailClient.markAsRead(email)
        }
        verify {
            monitoringService.notifyEmailProcessingError(email, any(NoPublicationFoundException::class))
        }
    }
}

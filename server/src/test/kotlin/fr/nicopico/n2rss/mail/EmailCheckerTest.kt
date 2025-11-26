/*
 * Copyright (c) 2025 Nicolas PICON
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
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandler
import fr.nicopico.n2rss.newsletter.handlers.exception.NoPublicationFoundException
import fr.nicopico.n2rss.newsletter.handlers.process
import fr.nicopico.n2rss.newsletter.models.Publication
import fr.nicopico.n2rss.newsletter.service.NewsletterService
import fr.nicopico.n2rss.newsletter.service.PublicationService
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

@ExtendWith(MockKExtension::class)
class EmailCheckerTest {

    @MockK(relaxUnitFun = true)
    private lateinit var emailClient: EmailClient
    @MockK
    private lateinit var newsletterService: NewsletterService
    @MockK(relaxed = true)
    private lateinit var publicationService: PublicationService
    @MockK
    private lateinit var monitoringService: MonitoringService

    private lateinit var emailChecker: EmailChecker

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        emailChecker = EmailChecker(
            emailClient = emailClient,
            newsletterService = newsletterService,
            publicationService = publicationService,
            monitoringService = monitoringService,
            moveAfterProcessingEnabled = true,
        )
    }

    @Test
    fun `emailChecker handles email when appropriate handler is present`(
        @MockK(relaxed = true) email: Email,
        @MockK newsletterHandler: NewsletterHandler,
        @MockK publication: Publication,
    ) {
        // Given an email that should be handled by a newsletterHandler
        every { emailClient.checkEmails() } returns listOf(email)
        every { newsletterService.findNewsletterHandlerForEmail(email) } returns newsletterHandler
        every { newsletterHandler.process(email) } returns listOf(publication)
        every { publication.articles } returns listOf(mockk())

        // When we check the email
        emailChecker.savePublicationsFromEmails()

        // Then the email should be handled by newsletterHandlerA and not by newsletterHandlerB
        verify { newsletterHandler.process(email) }
        verify { publicationService.savePublications(eq(listOf(publication))) }
        verify { emailClient.markAsRead(email) }
        verify { emailClient.moveToProcessed(email) }
    }

    @Test
    fun `emailChecker ignores email when no appropriate handler is present`(
        @MockK(relaxed = true) email: Email
    ) {
        // Given an email that should not be handled by any handler
        every { emailClient.checkEmails() } returns listOf(email)
        every { newsletterService.findNewsletterHandlerForEmail(email) } returns null

        // When we check the email
        emailChecker.savePublicationsFromEmails()

        // Then no handler should try to handle the email and no publication should be saved
        verify(exactly = 0) { publicationService.savePublications(any()) }
        verify(exactly = 0) { emailClient.markAsRead(any()) }
        verify(exactly = 0) { emailClient.moveToProcessed(any()) }
    }

    @Test
    fun `emailChecker continues processing emails after a processing error`(
        @MockK(relaxed = true) errorEmail: Email,
        @MockK(relaxed = true) validEmail: Email,
        @MockK newsletterHandler: NewsletterHandler,
        @MockK publication: Publication,
    ) {
        // Given an email that causes an error and a valid email
        every { emailClient.checkEmails() } returns listOf(errorEmail, validEmail)
        every { emailClient.markAsRead(any()) } just Runs

        every { newsletterService.findNewsletterHandlerForEmail(any()) } returns newsletterHandler

        val errorEmailProcessing = Exception("Processing error")
        every { newsletterHandler.process(errorEmail) } throws errorEmailProcessing
        every { newsletterHandler.process(validEmail) } returns listOf(publication)
        every { publication.articles } returns listOf(mockk())
        every { monitoringService.notifyEmailProcessingError(any(), any(), any()) } just Runs

        // When we check the emails
        emailChecker.savePublicationsFromEmails()

        // Then emailChecker should process validEmail after failing with errorEmail
        verify { newsletterHandler.process(errorEmail) }
        verify { newsletterHandler.process(validEmail) }
        verify { publicationService.savePublications(eq(listOf(publication))) }

        verify { emailClient.markAsRead(validEmail) }
        verify(exactly = 0) { emailClient.markAsRead(errorEmail) }
        verify(exactly = 0) { emailClient.moveToProcessed(errorEmail) }

        verify {
            monitoringService.notifyEmailProcessingError(
                email = errorEmail,
                error = errorEmailProcessing,
                newsletterHandler = newsletterHandler,
            )
        }
    }

    @Test
    fun `emailChecker will not crash if the client fails`() {
        // Given that emailClient fails when checking emails
        val emailError = RuntimeException("TEST")
        every { emailClient.checkEmails() } throws emailError
        every { monitoringService.notifyGenericError(any(), any()) } just Runs

        // When we check the emails
        emailChecker.savePublicationsFromEmails()

        // Then emailChecker should proceed without doing anything
        verify { emailClient.checkEmails() }
        verify { monitoringService.notifyGenericError(emailError, "Checking emails") }

        confirmVerified(
            emailClient,
            publicationService,
            monitoringService,
        )
    }

    @Test
    fun `emailChecker should notify an error on empty publications`(
        @MockK(relaxed = true) email: Email,
        @MockK newsletterHandler: NewsletterHandler,
        @MockK publication: Publication,
    ) {
        // Given an email without any articles
        every { emailClient.checkEmails() } returns listOf(email)
        every { newsletterService.findNewsletterHandlerForEmail(email) } returns newsletterHandler
        every { newsletterHandler.process(email) } returns listOf(publication)
        every { publication.articles } returns emptyList()
        every { monitoringService.notifyEmailProcessingError(any(), any(), any()) } just Runs

        // When we check the email
        emailChecker.savePublicationsFromEmails()

        // Then the email should be marked as read without creating a publication
        verify(exactly = 0) {
            publicationService.savePublications(eq(listOf(publication)))
            emailClient.markAsRead(email)
            emailClient.moveToProcessed(email)
        }
        verify {
            monitoringService.notifyEmailProcessingError(
                email = email,
                error = any(NoPublicationFoundException::class),
                newsletterHandler = newsletterHandler,
            )
        }
    }

    @Test
    fun `emailChecker should not mark emails as read if the publications could not be saved`(
        @MockK(relaxed = true) email1: Email,
        @MockK(relaxed = true) email2: Email,
        @MockK newsletterHandler: NewsletterHandler,
        @MockK(relaxed = true) publication1: Publication,
        @MockK(relaxed = true) publication2: Publication,
    ) {
        // Given an email that should be handled by a newsletterHandler
        every { emailClient.checkEmails() } returns listOf(email1, email2)
        every { newsletterService.findNewsletterHandlerForEmail(any()) } returns newsletterHandler
        every { newsletterHandler.process(email1) } returns listOf(publication1)
        every { newsletterHandler.process(email2) } returns listOf(publication2)
        every { publication1.articles } returns listOf(mockk())
        every { publication2.articles } returns listOf(mockk())

        every { publicationService.savePublications(listOf(publication1)) } throws RuntimeException("TEST")
        every { publicationService.savePublications(listOf(publication2)) } just Runs

        every { monitoringService.notifyEmailProcessingError(any(), any(), any()) } just Runs

        // When we check the email
        emailChecker.savePublicationsFromEmails()

        // THEN
        verify(exactly = 0) {
            emailClient.markAsRead(email1)
            emailClient.moveToProcessed(email1)
        }
        verify {
            emailClient.markAsRead(email2)
            emailClient.moveToProcessed(email2)
        }
        verify {
            monitoringService.notifyEmailProcessingError(
                email = email1,
                error = any(),
                newsletterHandler = newsletterHandler,
            )
        }
        confirmVerified(monitoringService)
    }

    @Test
    fun `emailChecker should report markAsRead error as generic error`(
        @MockK(relaxed = true) email: Email,
        @MockK newsletterHandler: NewsletterHandler,
        @MockK publication: Publication,
    ) {
        // GIVEN
        every { emailClient.checkEmails() } returns listOf(email)
        every { newsletterService.findNewsletterHandlerForEmail(email) } returns newsletterHandler
        every { newsletterHandler.process(email) } returns listOf(publication)
        every { publication.articles } returns listOf(mockk())

        val markAsReadError = RuntimeException("TEST")
        every { emailClient.markAsRead(any()) } throws markAsReadError
        every { monitoringService.notifyGenericError(any(), any()) } just Runs

        // WHEN
        emailChecker.savePublicationsFromEmails()

        // THEN
        verify { newsletterHandler.process(email) }
        verify { publicationService.savePublications(eq(listOf(publication))) }
        verify { emailClient.markAsRead(email) }
        verify(exactly = 0) { emailClient.moveToProcessed(email) }

        verify { monitoringService.notifyGenericError(markAsReadError, any()) }
        confirmVerified(monitoringService)
    }

    @Test
    fun `emailChecker should report moveToProcessed error as generic error`(
        @MockK(relaxed = true) email: Email,
        @MockK newsletterHandler: NewsletterHandler,
        @MockK publication: Publication,
    ) {
        // GIVEN
        every { emailClient.checkEmails() } returns listOf(email)
        every { newsletterService.findNewsletterHandlerForEmail(email) } returns newsletterHandler
        every { newsletterHandler.process(email) } returns listOf(publication)
        every { publication.articles } returns listOf(mockk())

        val moveToProcessedError = RuntimeException("TEST")
        every { emailClient.moveToProcessed(any()) } throws moveToProcessedError
        every { monitoringService.notifyGenericError(any(), any()) } just Runs

        // WHEN
        emailChecker.savePublicationsFromEmails()

        // THEN
        verify { newsletterHandler.process(email) }
        verify { publicationService.savePublications(eq(listOf(publication))) }
        verify { emailClient.markAsRead(email) }
        verify { emailClient.moveToProcessed(email) }

        verify { monitoringService.notifyGenericError(moveToProcessedError, any()) }
        confirmVerified(monitoringService)
    }
}

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
import fr.nicopico.n2rss.mail.client.EmailServerConfiguration
import fr.nicopico.n2rss.mail.client.GreenMailTestBase
import fr.nicopico.n2rss.mail.client.JavaxEmailClient
import fr.nicopico.n2rss.monitoring.MonitoringService
import fr.nicopico.n2rss.newsletter.handlers.process
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Publication
import fr.nicopico.n2rss.newsletter.service.NewsletterService
import fr.nicopico.n2rss.newsletter.service.PublicationService
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EmailCheckerIntegrationTest : GreenMailTestBase(
    userEmail = USER_EMAIL,
    userPassword = USER_PASSWORD,
) {
    companion object {
        private const val INBOX_FOLDER = "INBOX"
        private const val TRASH_FOLDER = "TRASH"
        private const val USER_EMAIL = "user@example.com"
        private const val USER_PASSWORD = "secret"
    }

    @MockK
    private lateinit var newsletterService: NewsletterService
    @MockK(relaxUnitFun = true)
    private lateinit var publicationService: PublicationService
    @MockK(relaxUnitFun = true)
    private lateinit var monitoringService: MonitoringService

    private lateinit var emailClient: EmailClient
    private lateinit var emailChecker: EmailChecker

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        val user = greenMail.userManager.getUserByEmail(USER_EMAIL)
        emailClient = JavaxEmailClient(
            config = EmailServerConfiguration(
                protocol = "imap",
                host = greenMail.imap.bindTo,
                port = greenMail.imap.port,
                user = user.email,
                password = user.password,
            ),
            folders = listOf(INBOX_FOLDER),
            processedFolder = TRASH_FOLDER,
        )

        emailChecker = EmailChecker(
            emailClient,
            newsletterService,
            publicationService,
            monitoringService,
            true,
        )
    }

    @Test
    fun `should check emails for publications`() {
        // GIVEN
        deliverTextMessage(
            folderName = INBOX_FOLDER,
            from = "from@email.com",
            subject = "Subject 1",
            content = "Hello World! 1",
        )
        deliverTextMessage(
            folderName = INBOX_FOLDER,
            from = "from@another-email.com",
            subject = "Subject 2",
            content = "Hello World! 2",
        )

        every {
            newsletterService.findNewsletterHandlerForEmail(any())
        } returns mockk("NewsletterHandler") {
            // Each newsletter returns a non-empty publication
            every { process(any()) } returns listOf(
                mockk<Publication> {
                    every { articles } returns listOf(mockk<Article>())
                }
            )
        }
        every {
            publicationService.savePublications(any())
        } just Runs

        // WHEN
        emailChecker.savePublicationsFromEmails()

        // THEN
        confirmVerified(monitoringService)
    }
}

package fr.nicopico.n2rss.mail

import fr.nicopico.n2rss.data.PublicationRepository
import fr.nicopico.n2rss.mail.client.EmailClient
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Publication
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class EmailCheckerTest {

    @MockK
    private lateinit var emailClient: EmailClient
    @MockK(relaxed = true)
    private lateinit var newsletterHandlerA: NewsletterHandler
    @MockK(relaxed = true)
    private lateinit var newsletterHandlerB: NewsletterHandler
    @MockK(relaxed = true)
    private lateinit var publicationRepository: PublicationRepository

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
        )
    }

    @Test
    fun `emailChecker handles email when appropriate handler is present`(
        @MockK(relaxed = true) email: Email,
        @MockK publication: Publication,
    ) {
        // Given an email that should be handled by newsletterHandlerA
        every { emailClient.checkEmails() } returns listOf(email)
        every { newsletterHandlerA.canHandle(email) } returns true
        every { newsletterHandlerA.process(email) } returns publication
        every { emailClient.markAsRead(any()) } just Runs

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
        every { newsletterHandlerA.process(errorEmail) } throws Exception("Processing error")
        every { newsletterHandlerA.process(validEmail) } returns publication

        // When we check the emails
        emailChecker.savePublicationsFromEmails()

        // Then emailChecker should process validEmail after failing with errorEmail
        verify { newsletterHandlerA.process(errorEmail) }
        verify { newsletterHandlerA.process(validEmail) }
        verify { publicationRepository.saveAll(eq(listOf(publication))) }
        verify { emailClient.markAsRead(validEmail) }
        verify(exactly = 0) { emailClient.markAsRead(errorEmail) }
    }
}

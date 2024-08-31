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
package fr.nicopico.n2rss.monitoring

import fr.nicopico.n2rss.fakes.FixedClock
import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.mail.models.Sender
import fr.nicopico.n2rss.monitoring.data.GithubIssueData
import fr.nicopico.n2rss.monitoring.data.GithubIssueRepository
import fr.nicopico.n2rss.monitoring.github.GithubClient
import fr.nicopico.n2rss.monitoring.github.GithubException
import fr.nicopico.n2rss.monitoring.github.IssueId
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URL
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class GithubMonitoringServiceTest {

    @MockK
    private lateinit var repository: GithubIssueRepository
    @MockK
    private lateinit var client: GithubClient

    private lateinit var monitoringService: MonitoringService

    private val now by lazy { Instant.parse("2007-12-03T10:15:30.00Z") }

    @BeforeEach
    fun setUp() {
        monitoringService = GithubMonitoringService(
            repository = repository,
            client = client,
            clock = FixedClock(now),
        )
    }

    //region notifyEmailClientError
    @Test
    fun `notifyEmailClientError should create a new GitHub issue and save it to the repository`() {
        // GIVEN
        val errorMessage = "Some error"
        val error = Exception(errorMessage)
        val issueId = IssueId(Random.nextInt())

        // SETUP
        every { repository.findEmailClientError(any()) } returns null
        every { client.createIssue(any(), any(), any()) } returns issueId
        every { repository.save(any<GithubIssueData.EmailClientError>()) } answers {
            firstArg<GithubIssueData.EmailClientError>()
        }

        // WHEN
        monitoringService.notifyEmailClientError(error)

        // THEN
        val bodySlot = slot<String>()
        verifySequence {
            repository.findEmailClientError(errorMessage)
            client.createIssue(
                title = "Email client fails with `Some error`",
                body = capture(bodySlot),
                match { labels ->
                    "n2rss-bot" in labels
                    "email-client-error" in labels
                    "bug" in labels
                }
            )
            repository.save(
                match<GithubIssueData.EmailClientError> {
                    it.issueId == issueId && it.errorMessage == errorMessage
                }
            )
        }
        confirmVerified(repository, client)

        bodySlot.captured shouldStartWith """
            |Retrieving emails failed with the following error:
            |
            |```
            |java.lang.Exception: Some error
            |	at fr.nicopico.n2rss.monitoring.GithubMonitoringServiceTest.notifyEmailClientError should create a new GitHub issue and save it to the repository(GithubMonitoringServiceTest.kt:
        """.trimMargin()

        bodySlot.captured shouldEndWith """
            |```
            |
        """.trimMargin()
    }

    @Test
    fun `notifyEmailClientError should not do anything if a GitHub issue already exists`() {
        // GIVEN
        val errorMessage = "Some error"
        val error = Exception(errorMessage)
        val issueId = IssueId(Random.nextInt())

        // SETUP
        every { repository.findEmailClientError(any()) } returns GithubIssueData.EmailClientError(
            issueId = issueId,
            errorMessage = errorMessage,
        )

        // WHEN
        monitoringService.notifyEmailClientError(error)

        // THEN
        verify { repository.findEmailClientError(errorMessage) }
        confirmVerified(repository, client)
    }

    @Test
    fun `notifyEmailClientError should not throw if a GithubException occurs`() {
        // SETUP
        every { repository.findEmailClientError(any()) } returns null
        every { client.createIssue(any(), any(), any()) } throws GithubException("Some GitHub error !")

        // WHEN
        shouldNotThrowAny {
            monitoringService.notifyEmailClientError(Exception("TEST"))
        }

        // THEN
        verify(exactly = 0) { repository.save(any<GithubIssueData.EmailClientError>()) }
    }
    //endregion

    //region notifyEmailProcessingError
    @Test
    fun `notifyEmailProcessingError should create a new GitHub issue and save it to the repository`() {
        // GIVEN
        val email = Email(
            subject = "Any title",
            sender = Sender("test <test@example.com>"),
            date = LocalDate.fromEpochDays(2000),
            msgnum = 1,
            content = "Guatemala georgia duplicate dealer popular spectrum surface, block databases attempt aids phrase"
        )
        val error = Exception("Some error")
        val issueId = IssueId(Random.nextInt())

        // SETUP
        every { repository.findEmailProcessingError(any(), any()) } returns null
        every { client.createIssue(any(), any(), any()) } returns issueId
        every { repository.save(any<GithubIssueData.EmailProcessingError>()) } answers {
            firstArg<GithubIssueData.EmailProcessingError>()
        }

        // WHEN
        monitoringService.notifyEmailProcessingError(email, error)

        // THEN
        val bodySlot = slot<String>()
        verifySequence {
            repository.findEmailProcessingError(email, "Some error")
            client.createIssue(
                title = "Email processing error on \"Any title\"",
                body = capture(bodySlot),
                match { labels ->
                    "n2rss-bot" in labels
                    "email-processing-error" in labels
                    "bug" in labels
                }
            )
            repository.save(
                match<GithubIssueData.EmailProcessingError> {
                    it.issueId == issueId
                        && it.emailTitle == email.subject
                        && it.errorMessage == error.message
                }
            )
        }
        confirmVerified(repository, client)

        // 2007-12-03T10:15:30.00Z
        bodySlot.captured shouldStartWith """
            |Processing of email "Any title" sent by "test <test@example.com>" failed with the following error:
            |
            |```
            |java.lang.Exception: Some error
            |	at fr.nicopico.n2rss.monitoring.GithubMonitoringServiceTest.notifyEmailProcessingError should create a new GitHub issue and save it to the repository(GithubMonitoringServiceTest.kt:
        """.trimMargin()

        bodySlot.captured shouldEndWith """
            |```
            |
        """.trimMargin()
    }

    @Test
    fun `notifyEmailProcessingError should not do anything if a GitHub issue already exists`() {
        // GIVEN
        val email = mockk<Email> {
            every { subject } returns "Any title"
            every { sender } returns Sender("test <test@example.com>")
        }
        val error = Exception("Some error")
        val issueId = IssueId(Random.nextInt())
        val emailProcessingError = GithubIssueData.EmailProcessingError(
            issueId = issueId,
            emailTitle = "Any title",
            errorMessage = "Some error"
        )

        // SETUP
        every { repository.findEmailProcessingError(any(), any()) } returns emailProcessingError

        // WHEN
        monitoringService.notifyEmailProcessingError(email, error)

        // THEN
        verify {
            repository.findEmailProcessingError(email, "Some error")
        }
        confirmVerified(repository, client)
    }

    @Test
    fun `notifyEmailProcessingError should not throw if a GithubException occurs`() {
        // GIVEN
        val email = mockk<Email> {
            every { subject } returns "Any title"
            every { sender } returns Sender("test <test@example.com>")
        }

        // SETUP
        every { repository.findEmailProcessingError(any(), any()) } returns null
        every { client.createIssue(any(), any(), any()) } throws GithubException("Some GitHub error !")

        // WHEN
        shouldNotThrowAny {
            monitoringService.notifyEmailProcessingError(email, Exception("TEST"))
        }

        // THEN
        verify(exactly = 0) { repository.save(any<GithubIssueData.EmailProcessingError>()) }
    }
    //endregion

    //region notifyRequest
    @Test
    fun `notifyRequest should create a new GitHub issue and save it to the repository`() {
        // GIVEN
        val newsletterUrl = URL("https://www.androidweekly.net")
        val issueId = IssueId(Random.nextInt())

        // SETUP
        every { repository.findNewsletterRequest(any()) } returns null
        every { client.createIssue(any(), any(), any()) } returns issueId
        every { repository.save(any<GithubIssueData.NewsletterRequest>()) } answers {
            firstArg<GithubIssueData.NewsletterRequest>()
        }

        // WHEN
        monitoringService.notifyRequest(newsletterUrl)

        // THEN
        val bodySlot = slot<String>()
        verifySequence {
            repository.findNewsletterRequest(newsletterUrl)
            client.createIssue(
                title = "Add support for newsletter \"https://www.androidweekly.net\"",
                body = capture(bodySlot),
                match { labels ->
                    "n2rss-bot" in labels
                    "newsletter-request" in labels
                }
            )
            repository.save(
                match<GithubIssueData.NewsletterRequest> {
                    it.issueId == issueId
                        && it.newsletterUrl.toURI() == newsletterUrl.toURI()
                }
            )
        }
        confirmVerified(repository, client)

        bodySlot.captured shouldBe "Initial request to support \"https://www.androidweekly.net\"" +
            " received on 2007-12-03"
    }

    @Test
    fun `notifyRequest should add a comment to an existing GitHub issue`() {
        // GIVEN
        val newsletterUrl = URL("https://www.androidweekly.net")
        val issueId = IssueId(Random.nextInt())
        val newsletterRequest = GithubIssueData.NewsletterRequest(
            issueId = issueId,
            newsletterUrl = newsletterUrl,
        )

        // SETUP
        every { repository.findNewsletterRequest(any()) } returns newsletterRequest
        every { client.addCommentToIssue(any(), any()) } just Runs

        // WHEN
        monitoringService.notifyRequest(newsletterUrl)

        // THEN
        verifySequence {
            repository.findNewsletterRequest(newsletterUrl)
            client.addCommentToIssue(issueId, "New request received on 2007-12-03")
        }
        confirmVerified(repository, client)
    }

    @Test
    fun `notifyRequest should not throw if a GithubException occurs`() {
        // SETUP
        every { repository.findNewsletterRequest(any()) } returns null
        every { client.createIssue(any(), any(), any()) } throws GithubException("Some GitHub error !")

        // WHEN
        shouldNotThrowAny {
            monitoringService.notifyRequest(URL("https://github.com/test"))
        }

        // THEN
        verify(exactly = 0) { repository.save(any<GithubIssueData.NewsletterRequest>()) }
    }
    //endregion
}
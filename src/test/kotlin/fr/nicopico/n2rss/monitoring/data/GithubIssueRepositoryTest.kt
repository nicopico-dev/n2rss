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
package fr.nicopico.n2rss.monitoring.data

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.monitoring.github.IssueId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URL
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class GithubIssueRepositoryTest {

    @MockK
    private lateinit var emailClientErrorRepository: GithubIssueEmailClientErrorRepository
    @MockK
    private lateinit var emailProcessingErrorRepository: GithubIssueEmailProcessingErrorRepository
    @MockK
    private lateinit var newsletterRequestRepository: GithubIssueNewsletterRequestRepository

    private lateinit var githubRepository: GithubIssueRepository

    @BeforeEach
    fun setUp() {
        githubRepository = GithubIssueRepository(
            emailClientErrorRepository = emailClientErrorRepository,
            emailProcessingErrorRepository = emailProcessingErrorRepository,
            newsletterRequestRepository = newsletterRequestRepository,
        )
    }

    @Test
    fun `findEmailClientError should defer to the proper repository`() {
        // GIVEN
        val errorMessage =
            "Plug essentials inspections continuing crest chief plymouth, villages superior attempting airline porsche textbook. "
        val expected = GithubIssueData.EmailClientError(
            issueId = IssueId(Random.nextInt()),
            errorMessage = errorMessage,
        )

        // SETUP
        every { emailClientErrorRepository.getEmailClientErrorByErrorMessage(any()) } returns expected

        // WHEN
        val result = githubRepository.findEmailClientError(errorMessage)

        // THEN
        result shouldBe expected
        verify { emailClientErrorRepository.getEmailClientErrorByErrorMessage(errorMessage) }
    }

    @Test
    fun `save EmailClientError should defer to the proper repository`() {
        // GIVEN
        val data = GithubIssueData.EmailClientError(
            issueId = IssueId(Random.nextInt()),
            errorMessage = "Rolling removal cookie discover heavily typing bracket, units bundle belarus incredible reaction word joined, microwave equally cosmetic earliest monsters magnitude offset, donors opponent jewellery qty photograph. ",
        )

        // SETUP
        every { emailClientErrorRepository.save(any()) } returns data

        // WHEN
        githubRepository.save(data)

        // THEN
        verify { emailClientErrorRepository.save(data) }
    }

    @Test
    fun `findEmailProcessingError should defer to the proper repository`() {
        // GIVEN
        val emailTitle = "Johnphillip Windsor"
        val email = mockk<Email> {
            every { subject } returns emailTitle
        }
        val errorMessage =
            "Plug essentials inspections continuing crest chief plymouth, villages superior attempting airline porsche textbook. "
        val expected = GithubIssueData.EmailProcessingError(
            issueId = IssueId(Random.nextInt()),
            emailTitle = emailTitle,
            errorMessage = errorMessage,
        )

        // SETUP
        every {
            emailProcessingErrorRepository.getEmailProcessingErrorByEmailTitleAndErrorMessage(any(), any())
        } returns expected

        // WHEN
        val result = githubRepository.findEmailProcessingError(email, errorMessage)

        // THEN
        result shouldBe expected
        verify {
            emailProcessingErrorRepository.getEmailProcessingErrorByEmailTitleAndErrorMessage(
                emailTitle,
                errorMessage,
            )
        }
    }

    @Test
    fun `save EmailProcessingError should defer to the proper repository`() {
        // GIVEN
        val data = GithubIssueData.EmailProcessingError(
            issueId = IssueId(Random.nextInt()),
            emailTitle = "Retha Escoto",
            errorMessage = "Rolling removal cookie discover heavily typing bracket, units bundle belarus incredible reaction word joined, microwave equally cosmetic earliest monsters magnitude offset, donors opponent jewellery qty photograph. ",
        )

        // SETUP
        every { emailProcessingErrorRepository.save(any()) } returns data

        // WHEN
        githubRepository.save(data)

        // THEN
        verify { emailProcessingErrorRepository.save(data) }
    }

    @Test
    fun `findNewsletterRequest should defer to the proper repository`() {
        // GIVEN
        val newsletterUrl = URL("https://www.yahoo.fr")
        val expected = GithubIssueData.NewsletterRequest(
            issueId = IssueId(Random.nextInt()),
            newsletterUrl = newsletterUrl,
        )

        // SETUP
        every {
            newsletterRequestRepository.getNewsletterRequestByNewsletterUrl(any())
        } returns expected

        // WHEN
        val result = githubRepository.findNewsletterRequest(newsletterUrl)

        // THEN
        result shouldBe expected
        verify {
            newsletterRequestRepository.getNewsletterRequestByNewsletterUrl(newsletterUrl)
        }
    }

    @Test
    fun `save NewsletterRequest should defer to the proper repository`() {
        // GIVEN
        val data = GithubIssueData.NewsletterRequest(
            issueId = IssueId(Random.nextInt()),
            newsletterUrl = URL("https://www.google.com"),
        )

        // SETUP
        every { newsletterRequestRepository.save(any()) } returns data

        // WHEN
        githubRepository.save(data)

        // THEN
        verify { newsletterRequestRepository.save(data) }
    }
}

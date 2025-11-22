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
package fr.nicopico.n2rss.monitoring.data

import fr.nicopico.n2rss.monitoring.github.IssueId
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.net.URL

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    useMainMethod = SpringBootTest.UseMainMethod.WHEN_AVAILABLE,
    properties = [
        "spring.profiles.active=local, test",
    ],
)
class GithubIssueRepositoryIntegrationTest {

    @Autowired
    lateinit var githubIssueRepository: GithubIssueRepository

    // Sample test data
    val genericError = GithubIssueData.GenericError(
        issueId = IssueId(1),
        errorMessage = "Sample Error Message",
    )
    val emailProcessingError = GithubIssueData.EmailProcessingError(
        issueId = IssueId(2),
        emailTitle = "Sample Email Title",
        errorMessage = "Sample Error Message",
    )
    val newsletterRequest = GithubIssueData.NewsletterRequest(
        issueId = IssueId(3),
        newsletterUrl = URL("https://sample-newsletter-url.com"),
    )

    @Test
    fun `test findGenericError`() {
        // Save test data in the repository
        githubIssueRepository.save(genericError)

        val result = githubIssueRepository.findGenericError("Sample Error Message")

        result shouldNotBe null
        result?.issueId shouldBe IssueId(1)
        result?.should {
            it.issueId shouldBe IssueId(1)
            it.errorMessage shouldBe "Sample Error Message"
        }
    }

    @Test
    fun `test findEmailProcessingError`() {
        // Save test data in the repository
        githubIssueRepository.save(emailProcessingError)

        val result = githubIssueRepository.findEmailProcessingError("Sample Email Title", "Sample Error Message")

        result shouldNotBe null
        result?.should {
            it.issueId shouldBe IssueId(2)
            it.emailTitle shouldBe "Sample Email Title"
            it.errorMessage shouldBe "Sample Error Message"
        }
    }

    @Test
    fun `test findNewsletterRequest`() {
        // Save test data in the repository
        githubIssueRepository.save(newsletterRequest)

        val newsletterUrl = URL("https://sample-newsletter-url.com")
        val result = githubIssueRepository.findNewsletterRequest(newsletterUrl)

        result shouldNotBe null
        result?.should {
            it.issueId shouldBe IssueId(3)
            it.newsletterUrl shouldBe newsletterUrl
        }
    }
}

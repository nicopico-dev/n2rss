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
package fr.nicopico.n2rss.monitoring.github

import fr.nicopico.n2rss.config.N2RssProperties
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GithubClientTest {

    private val server = MockWebServer()

    @BeforeEach
    fun setUp() {
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun createGithubClient(
        owner: String = "nicopico",
        repository: String = "n2rss",
        accessToken: String = "Some access token",
    ): GithubClient {
        return GithubClient(
            githubApiBaseUrl = server.url("/").toString(),
            githubProperties = N2RssProperties.GithubProperties(
                owner = owner,
                repository = repository,
                accessToken = accessToken,
            )
        )
    }

    @Test
    fun `GitHubClient can create a new issue through the API`() {
        // GIVEN
        val owner = "nicopico"
        val repository = "n2rss"
        val accessToken = "Some-access-token"
        val issueTitle = "issue title"
        val issueBody = "body content"
        val issueLabels = listOf("label A", "label B")

        // SETUP
        val client = createGithubClient(
            owner = owner,
            repository = repository,
            accessToken = accessToken,
        )
        server.enqueue(
            MockResponse()
                .setBody(GithubIssueSuccessResponse)
                .addHeader("Content-Type", "application/json")
        )

        // WHEN
        val issueId = client.createIssue(issueTitle, issueBody, labels = issueLabels)

        // THEN
        issueId shouldBe IssueId(1347)
        server.takeRequest() should {
            it.requestLine shouldBe "POST /repos/nicopico/n2rss/issues HTTP/1.1"
            it.headers shouldContain ("Authorization" to "Bearer Some-access-token")

            it.body.readUtf8() should { body ->
                body shouldContain Regex("\"title\"\\s*:\\s*\"issue title\"")
                body shouldContain Regex("\"body\"\\s*:\\s*\"body content\"")
                body shouldContain Regex("\"labels\"\\s*:\\s*\\[\\s*\"label A\"\\s*,\\s*\"label B\"\\s*]")
            }
        }
    }

    @Test
    fun `GitHubClient can add a comment to an existing issue through the API`() {
        // GIVEN
        val owner = "piconico"
        val repository = "ssr2n"
        val accessToken = "Some-other-access-token"
        val issueId = IssueId(1347)
        val comment = "comment content"

        // SETUP
        val client = createGithubClient(
            owner = owner,
            repository = repository,
            accessToken = accessToken,
        )
        server.enqueue(
            MockResponse()
                .setBody(GithubCommentSuccessResponse)
                .addHeader("Content-Type", "application/json")
        )

        // WHEN
        client.addCommentToIssue(
            issueId = issueId,
            body = comment,
        )

        // THEN
        server.takeRequest() should {
            it.requestLine shouldBe "POST /repos/piconico/ssr2n/issues/1347/comments HTTP/1.1"
            it.headers shouldContain ("Authorization" to "Bearer Some-other-access-token")

            it.body.readUtf8() should { body ->
                body shouldContain Regex("\"body\"\\s*:\\s*\"comment content\"")
            }
        }
    }
}

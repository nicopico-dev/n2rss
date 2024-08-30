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
package fr.nicopico.n2rss.monitoring.github

import fr.nicopico.n2rss.config.N2RssProperties
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class GithubClient(
    clientBuilder: RestClient.Builder = RestClient.builder(),
    githubProperties: N2RssProperties.GitHubProperties,
) {
    private val restClient by lazy {
        val owner = githubProperties.owner
        val repository = githubProperties.repository
        val accessToken = githubProperties.accessToken

        LOG.debug("repos: $owner/$repository")

        clientBuilder
            .baseUrl("https://api.github.com/repos/$owner/$repository")
            .defaultHeader("Authorization", "Bearer $accessToken")
            .defaultHeader("Accept", "application/vnd.github+json")
            .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
            .build()
    }

    /**
     * Create a GitHub issue with the given [title] and [body].
     * [labels] can also be provided, default is none.
     * @return ID of the issue
     *
     *
     * API: https://docs.github.com/en/rest/issues/issues?apiVersion=2022-11-28#create-an-issue
     */
    fun createIssue(
        title: String,
        body: String,
        labels: List<String> = emptyList()
    ): IssueId {
        val issue = GithubIssueDTO(title, body, labels)

        val responseBody: Map<String, Any?> = try {
            restClient
                .post()
                .uri("/issues")
                .contentType(MediaType.APPLICATION_JSON)
                .body(issue)
                .retrieve()
                .body(ParameterizedTypeReference.forType(Map::class.java))
                ?: throw GithubException("Cannot create GitHub issue, response body is empty")

        } catch (e: RestClientResponseException) {
            throw GithubException("Failed to create GitHub issue", e)
        }

        val rawIssueId = responseBody["number"]

        return (rawIssueId as? Int)
            ?.let { IssueId(it) }
            ?: throw GithubException("Unable to retrieve Github issue ID ($rawIssueId)")
    }

    /**
     * Add a comment to an issue with the given [issueId]
     *
     * API: https://docs.github.com/en/rest/issues/comments?apiVersion=2022-11-28#create-an-issue-comment
     */
    fun addCommentToIssue(
        issueId: IssueId,
        body: String
    ) {
        try {
            restClient
                .post()
                .uri("/issues/${issueId.value}/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf("body" to body)
                )
                .retrieve()
                .toBodilessEntity()

        } catch (e: RestClientResponseException) {
            throw GithubException("Failed to add comment to GitHub issue $issueId", e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GithubClient::class.java)
    }
}

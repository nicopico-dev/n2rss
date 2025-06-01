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
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.net.URL

interface GithubIssueRepository : JpaRepository<GithubIssueData, IssueId> {

    @Query(
        """
        SELECT e 
        FROM GITHUB_ISSUES e
        WHERE TYPE(e) = fr.nicopico.n2rss.monitoring.data.GithubIssueData${"$"}GenericError  
        AND TREAT(e AS fr.nicopico.n2rss.monitoring.data.GithubIssueData${"$"}GenericError).errorMessage = :errorMessage
    """
    )
    fun findGenericError(
        @Param("errorMessage") errorMessage: String
    ): GithubIssueData.GenericError?

    @Query(
        """
        SELECT e 
        FROM GITHUB_ISSUES e
        WHERE TYPE(e) = fr.nicopico.n2rss.monitoring.data.GithubIssueData${"$"}EmailProcessingError 
        AND TREAT(e AS fr.nicopico.n2rss.monitoring.data.GithubIssueData${"$"}EmailProcessingError).emailTitle = :emailTitle
        AND TREAT(e AS fr.nicopico.n2rss.monitoring.data.GithubIssueData${"$"}EmailProcessingError).errorMessage = :errorMessage
    """
    )
    fun findEmailProcessingError(
        @Param("emailTitle") emailTitle: String,
        @Param("errorMessage") errorMessage: String,
    ): GithubIssueData.EmailProcessingError?

    @Query(
        """
        SELECT e
        FROM GITHUB_ISSUES e
        WHERE TYPE(e) = fr.nicopico.n2rss.monitoring.data.GithubIssueData${"$"}NewsletterRequest 
        AND TREAT(e AS fr.nicopico.n2rss.monitoring.data.GithubIssueData${"$"}NewsletterRequest).newsletterUrl = :newsletterUrl
    """
    )
    fun findNewsletterRequest(
        @Param("newsletterUrl") newsletterUrl: URL,
    ): GithubIssueData.NewsletterRequest?
}

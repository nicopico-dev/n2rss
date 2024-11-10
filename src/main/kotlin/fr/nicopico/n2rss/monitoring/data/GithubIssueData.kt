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

import fr.nicopico.n2rss.monitoring.github.IssueId
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import java.net.URL

@Entity(name = "GITHUB_ISSUES")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name = "issue_type",
    discriminatorType = DiscriminatorType.STRING,
)
open class GithubIssueData(
    @Id
    @Column(name = "issue_id")
    val issueId: IssueId
) {

    @Entity
    @DiscriminatorValue("email-client-error")
    class EmailClientError(
        issueId: IssueId,

        @Column(nullable = false)
        val errorMessage: String
    ) : GithubIssueData(issueId)

    @Entity
    @DiscriminatorValue("email-processing-error")
    class EmailProcessingError(
        issueId: IssueId,

        @Column(nullable = false)
        val emailTitle: String,

        @Column(nullable = false)
        val errorMessage: String,
    ) : GithubIssueData(issueId)

    @Entity
    @DiscriminatorValue("newsletter-request")
    class NewsletterRequest(
        issueId: IssueId,

        @Column(nullable = false)
        val newsletterUrl: URL,
    ) : GithubIssueData(issueId)
}

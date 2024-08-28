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
import org.springframework.stereotype.Repository
import java.net.URL

@Repository
class GithubIssueRepository(
    private val newsletterRequestRepository: GithubIssueNewsletterRequestRepository,
) {
    //region EmailClientError
    fun getEmailClientError(error: Exception): GithubIssueData.EmailClientError? {
        TODO("Not yet implemented")
    }

    fun save(emailClientError: GithubIssueData.EmailClientError) {
        TODO("Not yet implemented")
    }
    //endregion

    //region EmailProcessingError
    fun getEmailProcessingError(email: Email, error: Exception): GithubIssueData.EmailProcessingError? {
        TODO("Not yet implemented")
    }

    fun save(emailProcessingError: GithubIssueData.EmailProcessingError) {
        TODO("Not yet implemented")
    }
    //endregion

    //region NewsletterRequest
    fun getNewsletterRequestByNewsletterUrl(newsletterUrl: URL): GithubIssueData.NewsletterRequest? {
        return newsletterRequestRepository.getNewsletterRequestByNewsletterUrl(newsletterUrl)
    }

    fun save(newsletterRequest: GithubIssueData.NewsletterRequest) {
        newsletterRequestRepository.save(newsletterRequest)
    }
    //endregion
}

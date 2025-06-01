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

import fr.nicopico.n2rss.mail.models.Email
import org.springframework.stereotype.Service
import java.net.URL

@Service
class GithubIssueService(
    private val githubIssueRepository: GithubIssueRepository,
) {
    //region GenericError
    /**
     * Finds an GenericError from the repository based on the provided error message.
     *
     * @param errorMessage The error message to search for in the repository.
     * @return The GenericError corresponding to the provided error message, or null if not found.
     */
    fun findGenericError(errorMessage: String): GithubIssueData.GenericError? {
        return githubIssueRepository.findGenericError(errorMessage)
    }

    /**
     * Saves the provided GenericError to the repository.
     *
     * @param genericError The GenericError instance to be saved.
     */
    fun save(genericError: GithubIssueData.GenericError) {
        githubIssueRepository.save(genericError)
    }
    //endregion

    //region EmailProcessingError
    /**
     * Finds an EmailProcessingError from the repository based on the provided email and error message.
     *
     * @param email The email whose processing error is to be found.
     * @param errorMessage The error message associated with the email.
     * @return The EmailProcessingError corresponding to the provided email and error message, or null if not found.
     */
    fun findEmailProcessingError(email: Email, errorMessage: String): GithubIssueData.EmailProcessingError? {
        return githubIssueRepository.findEmailProcessingError(email.subject, errorMessage)
    }

    /**
     * Saves the provided EmailProcessingError to the repository.
     *
     * @param emailProcessingError The EmailProcessingError instance to be saved.
     */
    fun save(emailProcessingError: GithubIssueData.EmailProcessingError) {
        githubIssueRepository.save(emailProcessingError)
    }
    //endregion

    //region NewsletterRequest
    /**
     * Finds a NewsletterRequest from the repository based on the provided newsletter URL.
     *
     * @param newsletterUrl The URL of the newsletter to search for in the repository.
     * @return The NewsletterRequest corresponding to the provided URL, or null if not found.
     */
    fun findNewsletterRequest(newsletterUrl: URL): GithubIssueData.NewsletterRequest? {
        return githubIssueRepository.findNewsletterRequest(newsletterUrl)
    }

    /**
     * Saves the provided NewsletterRequest to the repository.
     *
     * @param newsletterRequest The NewsletterRequest instance to be saved.
     */
    fun save(newsletterRequest: GithubIssueData.NewsletterRequest) {
        githubIssueRepository.save(newsletterRequest)
    }
    //endregion
}

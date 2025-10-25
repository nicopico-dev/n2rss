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
package fr.nicopico.n2rss.monitoring

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandler
import org.springframework.scheduling.annotation.Async
import java.net.URL

interface MonitoringService {

    @Async
    fun notifyGenericError(error: Exception, context: String? = null)

    @Async
    fun notifyEmailProcessingError(email: Email, error: Exception, newsletterHandler: NewsletterHandler? = null)

    /**
     * Saves a given newsletter request by sanitizing the provided URL and notifying the monitoring service.
     *
     * @param newsletterUrl The URL of the newsletter request to be saved.
     */
    @Async
    fun notifyNewsletterRequest(newsletterUrl: URL)

    @Async
    fun notifyMissingPublications(newsletterCodes: List<String>)
}

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

package fr.nicopico.n2rss.utils

import fr.nicopico.n2rss.analytics.service.data.AnalyticsRepository
import fr.nicopico.n2rss.newsletter.data.NewsletterRequestRepository
import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.event.ContextRefreshedEvent

class CleanLocalDatabaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var publicationRepository: PublicationRepository
    @MockK(relaxUnitFun = true)
    private lateinit var newsletterRequestRepository: NewsletterRequestRepository
    @MockK(relaxUnitFun = true)
    private lateinit var analyticsRepository: AnalyticsRepository

    private lateinit var cleanLocalDatabase: CleanLocalDatabase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        cleanLocalDatabase = CleanLocalDatabase(
            publicationRepository,
            newsletterRequestRepository,
            analyticsRepository,
        )
    }

    @Test
    fun `all publications will be deleted when the application context is refreshed`() {
        // GIVEN
        val anyEvent = mockk<ContextRefreshedEvent>()

        // WHEN
        cleanLocalDatabase.onApplicationEvent(anyEvent)

        // THEN
        verify(exactly = 1) {
            publicationRepository.deleteAll()
            newsletterRequestRepository.deleteAll()
            analyticsRepository.deleteAll()
        }
        confirmVerified(
            publicationRepository,
            newsletterRequestRepository,
            analyticsRepository,
        )
    }
}

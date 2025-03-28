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

package fr.nicopico.n2rss.utils

import fr.nicopico.n2rss.external.temporary.data.TemporaryEndpointRepository
import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.event.ContextRefreshedEvent

@ExtendWith(MockKExtension::class)
class CleanLocalDatabaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var publicationRepository: PublicationRepository
    @MockK(relaxUnitFun = true)
    private lateinit var temporaryEndpointRepository: TemporaryEndpointRepository

    private lateinit var cleanLocalDatabase: CleanLocalDatabase

    @BeforeEach
    fun setUp() {
        cleanLocalDatabase = CleanLocalDatabase(
            publicationRepository = publicationRepository,
            temporaryEndpointRepository = temporaryEndpointRepository,
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
            temporaryEndpointRepository.deleteAll()
        }
        confirmVerified(
            publicationRepository,
            temporaryEndpointRepository,
        )
    }
}

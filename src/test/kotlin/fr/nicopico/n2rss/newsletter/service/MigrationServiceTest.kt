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

package fr.nicopico.n2rss.newsletter.service

import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import fr.nicopico.n2rss.newsletter.data.entity.PublicationEntity
import fr.nicopico.n2rss.newsletter.data.legacy.LegacyPublicationRepository
import fr.nicopico.n2rss.newsletter.data.legacy.PublicationDocument
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.utils.now
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.net.URL
import java.util.UUID

@ExtendWith(MockKExtension::class)
class MigrationServiceTest {

    @MockK
    private lateinit var publicationRepository: PublicationRepository
    @MockK
    private lateinit var legacyPublicationRepository: LegacyPublicationRepository

    private lateinit var migrationService: MigrationService

    @BeforeEach
    fun setUp() {
        migrationService = MigrationService(publicationRepository, legacyPublicationRepository)
    }

    @Test
    fun `migration should be performed correctly when legacy data exists`() {
        // GIVEN
        val legacyPage: Page<PublicationDocument> = PageImpl(
            listOf(
                PublicationDocument(
                    id = UUID.randomUUID(),
                    title = "Test title",
                    LocalDate.now(),
                    Newsletter(
                        code = "code1",
                        name = "name1",
                        websiteUrl = "url",
                    ),
                    listOf(
                        Article(
                            title = "Article title",
                            description = "Article description",
                            link = URL("https://example.com/article"),
                        )
                    )
                )
            )
        )
        val emptyPage = PageImpl<PublicationDocument>(listOf())
        every { legacyPublicationRepository.findAll(any<Pageable>()) } returnsMany listOf(legacyPage, emptyPage)
        every { publicationRepository.saveAll(any<Iterable<PublicationEntity>>()) } answers { firstArg() }
        every { legacyPublicationRepository.deleteAll(any()) } just Runs

        // WHEN
        migrationService.migrateToNewDatabase()

        // THEN
        verifySequence {
            legacyPublicationRepository.findAll(any<Pageable>())
            publicationRepository.saveAll(any<Iterable<PublicationEntity>>())
            legacyPublicationRepository.deleteAll(any())
            legacyPublicationRepository.findAll(any<Pageable>())
        }
    }

    @Test
    fun `migration should not perform actions when no legacy data exists`() {
        // GIVEN
        val emptyLegacyPage: Page<PublicationDocument> = PageImpl(emptyList())
        every { legacyPublicationRepository.findAll(any<Pageable>()) } returns emptyLegacyPage

        // WHEN
        migrationService.migrateToNewDatabase()

        // THEN
        verify { legacyPublicationRepository.findAll(any<Pageable>()) }
        confirmVerified(legacyPublicationRepository, publicationRepository)
    }
}

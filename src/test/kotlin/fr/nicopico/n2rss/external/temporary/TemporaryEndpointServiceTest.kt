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

package fr.nicopico.n2rss.external.temporary

import fr.nicopico.n2rss.external.temporary.data.TemporaryEndpointEntity
import fr.nicopico.n2rss.external.temporary.data.TemporaryEndpointRepository
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldNotEndWith
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.url.shouldHaveHost
import io.kotest.matchers.url.shouldHaveProtocol
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.net.URL
import java.util.function.Consumer
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class TemporaryEndpointServiceTest {

    @MockK
    private lateinit var repository: TemporaryEndpointRepository
    @MockK
    private lateinit var transactionTemplate: TransactionTemplate
    private lateinit var baseUrl: URL

    private lateinit var service: TemporaryEndpointService

    @BeforeEach
    fun setUp() {
        baseUrl = URL("http://localhost" + Random.nextInt())
        service = TemporaryEndpointService(repository, transactionTemplate, baseUrl)

        every { transactionTemplate.executeWithoutResult(any()) } answers {
            firstArg<Consumer<TransactionStatus>>()
                .accept(SimpleTransactionStatus())
        }
    }

    @Test
    fun `expose should create a new temporary endpoint`() {
        // GIVEN
        val label = "Vegas temp genius guarantee photoshop italy punch"
        val content = "Configuration torture parish harvey."

        every { repository.saveAndFlush(any()) } returns mockk()

        // WHEN
        val temporaryEndpoint = service.expose(label, content)

        // THEN
        val entitySlot = slot<TemporaryEndpointEntity>()
        verify { repository.saveAndFlush(capture(entitySlot)) }
        confirmVerified(repository)

        temporaryEndpoint should {
            it shouldNot beNull()
            it.url shouldHaveProtocol baseUrl.protocol
            it.url shouldHaveHost baseUrl.host
            it.url.path shouldStartWith "/temp-endpoint/"
            it.url.path shouldNotEndWith "/temp-endpoint/"
            it.url.path shouldEndWith entitySlot.captured.exposedId.toString()

            it.toString() shouldContain it.url.toString()
        }

        entitySlot.captured should {
            it.label shouldBe label
            it.content shouldBe content
        }
    }

    @Test
    fun `temporary endpoint should be removed when closed`() {
        // GIVEN
        val label = "Craft site configured techrepublic"
        val content = "edmonton backup decreased, minister pregnant normally loved."

        every { repository.saveAndFlush(any()) } returns mockk()
        every { repository.deleteByExposedId(any()) } just Runs

        // WHEN
        val temporaryEndpoint = service.expose(label, content)
        temporaryEndpoint.close()

        // THEN
        verify { transactionTemplate.executeWithoutResult(any()) }
        verify { repository.deleteByExposedId(any()) }
    }
}

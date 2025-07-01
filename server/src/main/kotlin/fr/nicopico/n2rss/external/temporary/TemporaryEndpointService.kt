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

import fr.nicopico.n2rss.external.ExternalConfig
import fr.nicopico.n2rss.external.temporary.data.TemporaryEndpointEntity
import fr.nicopico.n2rss.external.temporary.data.TemporaryEndpointRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.io.Closeable
import java.net.URL
import java.util.UUID

@Service
class TemporaryEndpointService(
    private val temporaryEndpointRepository: TemporaryEndpointRepository,
    private val transactionTemplate: TransactionTemplate,
    @param:Qualifier(ExternalConfig.BASE_URL_QUALIFIER)
    private val baseUrl: URL,
) {

    @Transactional
    fun expose(label: String, content: String): TemporaryEndpoint {
        val entity = TemporaryEndpointEntity(
            exposedId = UUID.randomUUID(),
            label = label,
            content = content,
        )
        temporaryEndpointRepository.saveAndFlush(entity)
        return TemporaryEndpoint(entity.exposedId)
    }

    private fun close(temporaryEndpointId: UUID) {
        transactionTemplate.executeWithoutResult {
            temporaryEndpointRepository.deleteByExposedId(temporaryEndpointId)
        }
    }

    inner class TemporaryEndpoint(
        private val id: UUID,
    ) : Closeable {

        val url: URL = URL(baseUrl, "/temp-endpoint/$id")

        override fun close() {
            close(id)
        }

        override fun toString(): String {
            return "TemporaryEndpoint(path='$url')"
        }
    }
}

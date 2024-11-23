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

package fr.nicopico.n2rss.external.temporary

import fr.nicopico.n2rss.external.temporary.TemporaryEndpointService.TemporaryEndpoint
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import kotlin.random.Random

@Profile("local & reset-db")
@Component
class TemporaryEndpointDataStub(
    private val service: TemporaryEndpointService,
) {
    @EventListener
    fun onApplicationEvent(ignored: ContextRefreshedEvent) {
        val tempEndpoint: TemporaryEndpoint = service.expose("This is some content ${Random.nextInt()}")
        LOG.warn("Expose temporary endpoint: {}", tempEndpoint)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TemporaryEndpointDataStub::class.java)
    }
}

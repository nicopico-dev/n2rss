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

package fr.nicopico.n2rss.analytics

import fr.nicopico.n2rss.analytics.models.AnalyticsEvent
import fr.nicopico.n2rss.analytics.service.AnalyticsService
import fr.nicopico.n2rss.analytics.service.AnalyticsServiceDelegate
import io.kotest.assertions.throwables.shouldThrowAny
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AnalyticsServiceDelegateTest {

    @MockK(relaxUnitFun = true)
    private lateinit var analyticsServiceA: AnalyticsService
    @MockK(relaxUnitFun = true)
    private lateinit var analyticsServiceB: AnalyticsService
    @MockK(relaxUnitFun = true)
    private lateinit var analyticsServiceC: AnalyticsService

    @Test
    fun `track will delegate to all delegates in order`() {
        // GIVEN
        val delegate = AnalyticsServiceDelegate(listOf(analyticsServiceA, analyticsServiceB, analyticsServiceC))
        val event = mockk<AnalyticsEvent>()

        // WHEN
        delegate.track(event)

        // THEN
        verifyOrder {
            analyticsServiceA.track(event)
            analyticsServiceB.track(event)
            analyticsServiceC.track(event)
        }
        confirmVerified(analyticsServiceA, analyticsServiceB, analyticsServiceC)
    }

    @Test
    fun `all delegates will be triggered even in the case of a failure`() {
        // GIVEN
        val delegate = AnalyticsServiceDelegate(listOf(analyticsServiceA, analyticsServiceB, analyticsServiceC))
        val event = mockk<AnalyticsEvent>()

        // SETUP
        every { analyticsServiceA.track(event) } throws RuntimeException("TEST A")

        // WHEN
        shouldThrowAny {
            delegate.track(event)
        }

        // THEN
        verifyOrder {
            analyticsServiceA.track(event)
            analyticsServiceB.track(event)
            analyticsServiceC.track(event)
        }
        confirmVerified(analyticsServiceA, analyticsServiceB, analyticsServiceC)
    }
}

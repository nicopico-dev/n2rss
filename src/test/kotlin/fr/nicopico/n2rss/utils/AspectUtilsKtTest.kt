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

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Test

class AspectUtilsKtTest {

    @Test
    fun `ProceedingJoinPoint-proceed should defer to trackSuccess on success`() {
        // GIVEN
        val joinPoint: ProceedingJoinPoint = mockk()
        val trackSuccess: (JoinPoint) -> Unit = mockk(relaxed = true)
        val trackError: (JoinPoint, Exception) -> Unit = mockk(relaxed = true)
        val result = "indicating"

        // SETUP
        every { joinPoint.proceed() } returns result

        // WHEN
        val actual = joinPoint.proceed(trackSuccess, trackError)

        // THEN
        actual shouldBe result
        verify { trackSuccess(joinPoint) }
        verify(exactly = 0) { trackError(joinPoint, any()) }
    }

    @Test
    fun `ProceedingJoinPoint-proceed should defer to trackError on error`() {
        // GIVEN
        val joinPoint: ProceedingJoinPoint = mockk()
        val trackSuccess: (JoinPoint) -> Unit = mockk(relaxed = true)
        val trackError: (JoinPoint, Exception) -> Unit = mockk(relaxed = true)
        val error = RuntimeException("TEST")

        // SETUP
        every { joinPoint.proceed() } throws error

        // WHEN
        val actual = shouldThrowAny {
            joinPoint.proceed(trackSuccess, trackError)
        }

        // THEN
        actual shouldBe error
        verify { trackError(joinPoint, error) }
        verify(exactly = 0) { trackSuccess(joinPoint) }
    }
}

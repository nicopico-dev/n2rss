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

package fr.nicopico.n2rss.mail

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.scheduling.TaskScheduler
import java.time.Instant

class CheckEmailOnStartTest {

    @MockK(relaxed = true)
    private lateinit var taskScheduler: TaskScheduler
    @MockK(relaxed = true)
    private lateinit var emailChecker: EmailChecker

    private lateinit var checkEmailOnStart: CheckEmailOnStart

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        checkEmailOnStart = CheckEmailOnStart(taskScheduler, emailChecker)
    }

    @Test
    fun `should schedule a check on launch`() {
        // WHEN
        checkEmailOnStart.checkEmailsOnStart()

        // THEN (matcher does not work on method reference)
        val taskSlot = slot<Runnable>()
        verify { taskScheduler.schedule(capture(taskSlot), any<Instant>()) }

        verify(exactly = 0) { emailChecker.savePublicationsFromEmails() }
        taskSlot.captured.run()
        verify { emailChecker.savePublicationsFromEmails() }
    }
}

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
package fr.nicopico.n2rss.controller.maintenance

import fr.nicopico.n2rss.config.N2RssProperties
import jakarta.servlet.http.HttpServletResponse
import org.jetbrains.annotations.VisibleForTesting
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import kotlin.concurrent.thread

@Controller
class MaintenanceController(
    private val applicationContext: ApplicationContext,
    private val properties: N2RssProperties.MaintenanceProperties,
) {
    @PostMapping("/stop", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun stopServer(
        @RequestHeader("X-Secret-Key") secretKey: String,
        response: HttpServletResponse,
    ) {
        if (secretKey != properties.secretKey) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized")
            return
        }

        val context = applicationContext as ConfigurableApplicationContext
        response.status = HttpServletResponse.SC_OK
        response.writer.write("Bye!")
        thread {
            Thread.sleep(RESTART_DELAY_MS)
            SpringApplication.exit(context)
        }
    }

    companion object {
        @VisibleForTesting
        const val RESTART_DELAY_MS = 1000L
    }
}

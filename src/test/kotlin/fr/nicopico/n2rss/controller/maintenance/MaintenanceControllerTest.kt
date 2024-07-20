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

import fr.nicopico.n2rss.analytics.AnalyticService
import fr.nicopico.n2rss.config.N2RssProperties
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.AdditionalInterface
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext

// TODO Test MaintenanceController.notifyRelease() endpoint
class MaintenanceControllerTest {

    @MockK
    @AdditionalInterface(ConfigurableApplicationContext::class)
    private lateinit var applicationContext: ApplicationContext
    @MockK
    private lateinit var analyticService: AnalyticService
    @MockK
    private lateinit var maintenanceProps: N2RssProperties.MaintenanceProperties

    private lateinit var controller: MaintenanceController

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        controller = MaintenanceController(applicationContext, analyticService, maintenanceProps)

        mockkStatic(SpringApplication::class)
        every { SpringApplication.exit(any()) } returns 0
    }

    @Test
    fun `stop should stop the application after a delay if the secret key is correct`() {
        // GIVEN
        every { maintenanceProps.secretKey } returns "secret"
        val response: HttpServletResponse = mockk(relaxed = true)

        // WHEN
        controller.stopServer("secret", response)

        // THEN
        verify { response.status = 200 }
        verify(exactly = 0) { SpringApplication.exit(applicationContext) }
        Thread.sleep(MaintenanceController.RESTART_DELAY_MS)
        verify(exactly = 1) { SpringApplication.exit(applicationContext) }
    }

    @Test
    fun `stop should respond with 403 if the secret key is incorrect`() {
        // GIVEN
        every { maintenanceProps.secretKey } returns "secret"
        val response: HttpServletResponse = mockk(relaxed = true)

        // WHEN
        controller.stopServer("another", response)

        // THEN
        verify { response.sendError(403, any()) }
        verify(exactly = 0) { SpringApplication.exit(applicationContext) }
    }
}

package fr.nicopico.n2rss.controller.maintenance

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

class MaintenanceControllerTest {

    @MockK
    @AdditionalInterface(ConfigurableApplicationContext::class)
    private lateinit var applicationContext: ApplicationContext
    @MockK
    private lateinit var maintenanceProps: N2RssProperties.MaintenanceProperties

    private lateinit var controller: MaintenanceController

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        val props = mockk<N2RssProperties> {
            every { maintenance } returns maintenanceProps
        }
        controller = MaintenanceController(applicationContext, props)

        mockkStatic(SpringApplication::class)
        every { SpringApplication.exit(any()) } returns 0
    }

    @Test
    fun `stop should stop the application if the secret key is correct`() {
        // GIVEN
        every { maintenanceProps.secretKey } returns "secret"
        val response: HttpServletResponse = mockk(relaxed = true)

        // WHEN
        controller.stopServer("secret", response)

        // THEN
        verify { response.status = 200 }
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

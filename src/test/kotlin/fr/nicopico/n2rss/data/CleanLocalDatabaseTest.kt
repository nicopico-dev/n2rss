package fr.nicopico.n2rss.data

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.event.ContextRefreshedEvent

class CleanLocalDatabaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var publicationRepository: PublicationRepository

    private lateinit var cleanLocalDatabase: CleanLocalDatabase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        cleanLocalDatabase = CleanLocalDatabase(publicationRepository)
    }

    @Test
    fun `all publications will be deleted when the application context is refreshed`() {
        // GIVEN
        val anyEvent = mockk<ContextRefreshedEvent>()

        // WHEN
        cleanLocalDatabase.onApplicationEvent(anyEvent)

        // THEN
        verify(exactly = 1) { publicationRepository.deleteAll() }
        confirmVerified(publicationRepository)
    }
}

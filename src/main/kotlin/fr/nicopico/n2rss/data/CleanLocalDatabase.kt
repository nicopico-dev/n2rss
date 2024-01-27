package fr.nicopico.n2rss.data

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val LOG = LoggerFactory.getLogger(CleanLocalDatabase::class.java)

@Profile("local")
@Component
class CleanLocalDatabase(
    private val publicationRepository: PublicationRepository,
) {
    @EventListener
    fun onApplicationEvent(event: ContextRefreshedEvent) {
        LOG.info("Clean-up local database...")
        publicationRepository.deleteAll()
    }
}

package fr.nicopico.n2rss.data

import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.models.Publication
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface PublicationRepository : MongoRepository<Publication, UUID> {
    fun findByNewsletter(newsletter: Newsletter): List<Publication>
}

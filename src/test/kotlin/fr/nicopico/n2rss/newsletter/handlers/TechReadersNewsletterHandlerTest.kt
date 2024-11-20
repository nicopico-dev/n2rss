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

package fr.nicopico.n2rss.newsletter.handlers

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.newsletter.models.Article
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class TechReadersNewsletterHandlerTest : BaseNewsletterHandlerTest<TechReadersNewsletterHandler>(
    handlerProvider = ::TechReadersNewsletterHandler,
    stubsFolder = "Tech Readers",
) {

    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should not handle Tech Readers communication emails`() {
            // GIVEN
            val emails = loadEmails("stubs/emails/Tech Readers - Communication")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe false
        }

        @Test
        fun `should extract articles from Tech Readers email #116`() {
            // GIVEN
            val email: Email = loadEmail("stubs/emails/Tech Readers/Tech Readers #116 _ Manager par les valeurs.eml")

            // WHEN
            val articles = handler.extractArticles(email)

            // THEN
            val expected = listOf(
                Article(
                    title = "4 Traps to Avoid as You Transition into a Leadership Role",
                    link = URL("https://google.com"),
                    description = """
                        8 minutes, proposé par Antonin Gaunand
                        Ce que vous accomplissez avant de prendre un rôle de leadership est déterminant pour réussir dans les 90 premiers jours de votre prise de poste, et au-delà. S’il peut être tentant de s'appuyer sur des expériences passées et des stratégies éprouvées, cela risque de vous mener à des conclusions précipitées et à des faux pas. Il est donc essentiel de vous préparer soigneusement, de vous accorder du temps pour vous ressourcer, tant personnellement que professionnellement, et de bien comprendre les clés pour réussir dans votre nouvelle fonction.
                    """.trimIndent(),
                ),
                Article(
                    title = "Détection des signaux faible et leadership, les leçons de BlaBlaCar",
                    link = URL("https://google.com"),
                    description = """
                        4 minutes, proposé par Antonin Gaunand
                        Lors du Tech.Rocks Summit 2023, Olivier Bonnet, CTO de BlaBlaCar, et Antonin Gaunand, expert en leadership, ont partagé des réflexions sur l'évolution du leadership face à la croissance rapide et au travail à distance. Leur échange a exploré des enjeux cruciaux pour les leaders technologiques, notamment comment manager par les valeurs plutôt que par les process.
                    """.trimIndent(),
                ),
                Article(
                    title = "La culture Netflix : Le meilleur de nous-mêmes",
                    link = URL("https://google.com"),
                    description = """
                        9 minutes, proposé par Antonin Gaunand
                        Il est facile de parler de valeurs. Les appliquer l'est un peu moins. Cette page du site de Netflix présente les valeurs actualisées de Netflix, accompagnées d’exemples concrets, comme par exemple « le désaccord puis l'engagement », qui permet à la fois de confronter les idées et opinions, tout en s’alignant lorsque la décision a été prise.
                    """.trimIndent(),
                ),
                Article(
                    title = "Promesses et divination – la malédiction de la grosse release",
                    link = URL("https://google.com"),
                    description = """
                        6 minutes, proposé par Dorra Bartaguiz
                        L'article emploie un ton à la fois satirique et didactique. Antoine l'auteur utilise l'humour, la conversation fictive et des exemples de la vie courante pour critiquer la gestion des projets informatiques, en particulier les gros projets et les deadlines non réalistes. Le ton encourage la réflexion sur des méthodes plus agiles et itératives, tout en exposant les absurdités des approches traditionnelles avec une touche de légèreté et de pragmatisme.
                    """.trimIndent(),
                ),
                Article(
                    title = "La qualité logicielle, une affaire de code ?",
                    link = URL("https://google.com"),
                    description = """
                        3 minutes, proposé par Matthieu Eveillard
                        Simple, efficace, droit au but ! Une liste qui prouve que la qualité est l’affaire de tous les postes, tous les rôles, et pas seulement de ceux qui produisent. 
                    """.trimIndent(),
                ),
                Article(
                    title = "Vague de fuites de données en France : ça va durer encore longtemps ? On a demandé à un expert",
                    link = URL("https://google.com"),
                    description = """
                        7 minutes, proposé par Florian Fesseler
                        Une enquête pertinente qui tente d’expliquer la récente recrudescence et les effets toujours plus désastreux des fuites de données en France. L’autrice donne également des pistes pour tenter de lutter. Un point de vue très documenté et qui reste accessible, que vous pourrez partager autour de vous sans hésiter.
                    """.trimIndent(),
                ),
                Article(
                    title = "Le feature flip pour réussir à avoir du flow",
                    link = URL("https://google.com"),
                    description = """
                        47 minutes, proposée par Dorra Bartaguiz
                        Vous utilisez les features flippers dans vos projets et vous trouvez cette technique géniale pour livrer des user-stories en continue. Méfiez-vous des inconvénients qu'elle apporte. Dans cette présentation, je présente les défauts de cette technique et des alternatives pour mieux améliorer votre flow. Ces améliorations portent à la fois sur l'organisation, sur une priorisation différente et un design par l'injection ou un design pour un slicing plus malin.
                    """.trimIndent(),
                ),
                Article(
                    title = "IFTTD #291 - Micro front-end : Un patchwork efficace avec Fabien Brunet",
                    link = URL("https://google.com"),
                    description = """
                        58 minutes, proposé par Loïc Calvy
                        Vous avez aimé les micro-services, vous allez adorer les micro front-ends ! En tout cas, pour vous faire votre opinion vous aurez besoin de connaître et de comprendre ce concept. Cet épisode vous y aidera, et vous donnera très certainement envie de creuser le sujet.
                    """.trimIndent(),
                ),
                Article(
                    title = "Plus que 2 mois avant le Tech.Rocks Summit !",
                    link = URL("https://google.com"),
                    description = """
                        2 & 3 décembre 2024 • Théâtre de Paris
                        Pendant ces 2 jours, découvrez comment les leaders du domaine peuvent non seulement faire face, mais également s'épanouir dans un environnement en perpétuelle mutation. Explorez la résilience individuelle, systémique et sociale, et apprenez comment créer des environnements propices à l'adaptation et à la croissance. Rejoignez-nous pour un événement incontournable mêlant conférences inspirantes et networking avec des expert•e•s de renom, des praticien•ne•s et des leaders d'opinion. Réserver votre billet ici !
                    """.trimIndent(),
                ),
            )

            articles[0].description shouldBe expected[0].description
            articles[1].description shouldBe expected[1].description

            assertSoftly {
                withClue("title") {
                    articles.map { it.title } shouldBe expected.map { it.title }
                }
//                withClue("link") {
//                    articles.map { it.link } shouldBe expected.map { it.link }
//                }
                withClue("description") {
                    articles.map { it.description } shouldBe expected.map { it.description }
                }
            }
        }
    }
}

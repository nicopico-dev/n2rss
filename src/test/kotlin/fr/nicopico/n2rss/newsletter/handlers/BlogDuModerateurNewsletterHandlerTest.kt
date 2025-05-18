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
package fr.nicopico.n2rss.newsletter.handlers

import fr.nicopico.n2rss.mail.models.Email
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.kotlinx.datetime.shouldHaveSameDayAs
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BlogDuModerateurNewsletterHandlerTest : BaseNewsletterHandlerTest<BlogDuModerateurNewsletterHandler>(
    handlerProvider = ::BlogDuModerateurNewsletterHandler,
    stubsFolder = "Blog du Modérateur"
) {
    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract articles from email about ChatGPT and Meta AI`() {
            // GIVEN
            val email: Email =
                loadEmail("stubs/emails/Blog du Modérateur/Générateur d'images de ChatGPT, retour aux sources pour Facebook, Meta AI sur WhatsApp, TikTok Shop.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("publication title") {
                    title shouldBe "Générateur d'images de ChatGPT, retour aux sources pour Facebook, Meta AI sur WhatsApp, TikTok Shop"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Blog du Modérateur"
                }
            }

            println(publication.articles.map { it.title }.joinToString("\n"))

            withClue("article titles") {
                publication.articles.map { it.title } shouldBe listOf(
                    "ChatGPT abandonne DALL-E et se dote enfin d’un bon générateur d’images",
                    "Facebook lance un fil uniquement pour les amis : un retour aux sources ?",
                    "Comment choisir son hébergement web : les conseils de o2switch",
                    "La Sphère de Las Vegas, une aberrante prouesse technologique ?",
                    "Réseaux sociaux : les étapes pour devenir une marque mature",
                    "Meta AI sur WhatsApp : un assistant IA encombrant et décevant",
                    "IA, tendances TikTok, insights consommateurs : quelles innovations social media en 2025 ?",
                    "Mode vocal avancé sur ChatGPT : 5 cas d’usages",
                    "Pourquoi le social listening doit être multi-canal",
                    "Étude : 45 % des cadres ne maîtrisent pas les outils numériques en 2025",
                    "Les tendances réseaux sociaux 2025 pour une stratégie efficace",
                    "TikTok Shop arrive en France : les marques doivent-elles miser sur le social commerce ?",
                    "5 formations pour apprendre le marketing d’influence",
                    "IA, régulation et protection des données : la vision de Zendesk",
                    "Test automatisé et test manuel : quelles différences ?",
                    "« X va tanguer mais sera difficile à déloger » : We Are Social refait le match entre X, Threads et Bluesky",
                    "11 événements web à ne pas manquer en avril 2025",
                    "Google lance Gemini 2.5 pour concurrencer la série de modèles « o » d’OpenAI",
                    "5 applications mobiles autrefois incontournables mais tombées dans l’oubli",
                    "SEO : comment la presse s’empare de l’IA pour améliorer son référencement",
                    "Webinar : comment maîtriser le marketing digital dans la beauté et les cosmétiques ?",
                )
            }
        }

        @Test
        fun `should extract articles from email about Canva and WordPress`() {
            // GIVEN
            val email: Email =
                loadEmail("stubs/emails/Blog du Modérateur/Nouveautés Canva, le nouveau site builder par IA de WordPress, ChatGPT améliore sa mémoire.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("publication title") {
                    title shouldBe "Nouveautés Canva, le nouveau site builder par IA de WordPress, ChatGPT améliore sa mémoire..."
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Blog du Modérateur"
                }
            }

            TODO("Implement rest of the test")
        }
    }
}

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

import fr.nicopico.n2rss.STUBS_EMAIL_ROOT_FOLDER
import fr.nicopico.n2rss.mail.models.Email
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
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
                loadEmail("$STUBS_EMAIL_ROOT_FOLDER/Blog du Modérateur/Générateur d'images de ChatGPT, retour aux sources pour Facebook, Meta AI sur WhatsApp, TikTok Shop.eml")

            // WHEN
            val publications = handler.process(email)

            // THEN
            publications shouldHaveSize 2

            // Articles publication
            val articlesPublication = publications[0]
            assertSoftly(articlesPublication) {
                title shouldBe "Générateur d'images de ChatGPT, retour aux sources pour Facebook, Meta AI sur WhatsApp, TikTok Shop"
                date shouldHaveSameDayAs (email.date)
                newsletter.code shouldBe "bdm"
            }

            withClue("article titles") {
                articlesPublication.articles.map { it.title } shouldBe listOf(
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

            withClue("article descriptions") {
                articlesPublication.articles.map { it.description } shouldBe listOf(
                    "OpenAI lance 4o Image Generation, un nouveau modèle de génération d'images. Celui-ci remplace désormais DALL-E dans ChatGPT. Voici ce qu'il vaut !",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "Rendez-vous le 8 avril pour un webinar dédié à cette industrie en pleine croissance et qui offre de belles opportunités de carrière.",
                )
            }

            // Tools Publication
            val toolsPublication = publications[1]
            assertSoftly(toolsPublication) {
                title shouldBe "Générateur d'images de ChatGPT, retour aux sources pour Facebook, Meta AI sur WhatsApp, TikTok Shop"
                date shouldHaveSameDayAs (email.date)
                newsletter.code shouldBe "bdm-tools"
            }

            withClue("tool titles") {
                toolsPublication.articles.map { it.title } shouldBe listOf(
                    "Evoliz : un outil pour gérer ses factures, devis et bons de commande",
                    "Studio Creatio : un outil pour créer des applis et automatiser les workflows",
                    "Sage 50 : un logiciel comptable avec des options de gestion d'entreprise",
                    "Gouti : une plateforme de planification des ressources d'entreprise",
                    "Metricool : une solution pour gérer vos réseaux sociaux et analyser vos performances",
                    "Gestion de projet : 72 outils pour gérer et planifier ses tâches",
                )
            }
        }
    }
}

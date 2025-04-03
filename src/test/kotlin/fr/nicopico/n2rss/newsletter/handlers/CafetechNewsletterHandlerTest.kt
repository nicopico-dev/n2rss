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
import fr.nicopico.n2rss.newsletter.models.Article
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class CafetechNewsletterHandlerTest : BaseNewsletterHandlerTest<CafetechNewsletterHandler>(
    handlerProvider = ::CafetechNewsletterHandler,
    stubsFolder = "Cafétech",
) {

    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract articles from Cafetech email - Apple condamne en France`() {
            // GIVEN
            val email: Email =
                loadEmail("stubs/emails/Cafétech/Apple condamné en France pour abus de position dominante.eml")

            // WHEN
            val articles = handler.extractArticles(email)

            // THEN
            articles shouldNot beEmpty()

            val expected = listOf(
                Article(
                    title = "Apple condamné à une amende en France à cause de ses règles publicitaires",
                    link = URL("https://cafetech.substack.com/p/apple-condamne-en-france-pour-abus"),
                    description = """
                        Apple ne semble pas enclin à changer ses pratiques sur le pistage publicitaire. Pourtant, le groupe à la pomme a été condamné lundi par l'autorité de la concurrence à une amende de 150 millions d'euros. Il a été reconnu coupable d'abus de position dominante dans la distribution d'applications mobiles, en raison de la mise en place d'une nouvelle fenêtre de consentement. Se disant "déçu" de ce verdict sans grande surprise, il se contente de souligner que le gendarme antitrust français "n'a pas exigé de changements spécifiques". Autrement dit: Apple n'a aucune raison de modifier un système qui recueille un "fort soutien" de la part des consommateurs et des défenseurs de la vie privée. "Si aucun changement n'est apporté, l'illégalité persiste", répondent les quatre organismes, représentant l'industrie publicitaire, à l'origine de l'affaire.
                    """.trimIndent(),
                ),
                Article(
                    title = "Après son rachat, BeReal veut accélérer dans la publicité",
                    link = URL("https://cafetech.substack.com/p/apple-condamne-en-france-pour-abus"),
                    description = """
                        "C'est une cible particulièrement difficile à toucher pour les annonceurs". D'emblée, Anas Nadifi plante le décor. Cet ancien de Google et de TF1 vient de prendre les commandes de la régie publicitaire française de BeReal. Sa mission: imposer le réseau social tricolore dans le paysage publicitaire. Son principal argument de vente: la Gen Z. Cette catégorie d'âge, qui correspond aux personnes âgées de 13 à 28 ans, représente environ 70% de l'audience. "En France, nous touchons un utilisateur Gen Z sur deux", souligne le responsable. Depuis ses premiers pas sur le marché l'été dernier, BeReal assure avoir réalisé plus de 200 campagnes publicitaires dans le monde, notamment au Japon et au Royaume-Uni. Seulement un début alors que son nouveau propriétaire, l'éditeur de jeux vidéo mobiles Voodoo, affiche de grandes ambitions.
                    """.trimIndent(),
                ),
            )

            assertSoftly {
                withClue("title") {
                    articles.map { it.title } shouldBe expected.map { it.title }
                }
                withClue("link") {
                    articles.map { it.link } shouldBe expected.map { it.link }
                }
                withClue("description") {
                    articles.map { it.description } shouldBe expected.map { it.description }
                }
            }
        }
    }
}
